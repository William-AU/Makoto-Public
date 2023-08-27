package bot.commands.scheduling.strategies;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommandContext;
import bot.commands.framework.ManualCommandContext;
import bot.common.BotConstants;
import bot.exceptions.MemberAlreadyExistsException;
import bot.exceptions.MemberHasAlreadyAttackedException;
import bot.exceptions.MemberHasNotAttackedException;
import bot.exceptions.MemberIsNotAttackingException;
import bot.exceptions.schedule.ScheduleException;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.ScheduleService;
import bot.storage.models.ScheduleEntryEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class SelectMenuScheduleStrategy implements ScheduleStrategy {
    // Something something too many dependencies is bad
    private final GuildService guildService;
    private final BossService bossService;
    private final ScheduleService scheduleService;
    private final String SCHEDULING_CATEGORY_BASE = BotConstants.SCHEDULING_CATEGORY_NAME;
    private final String SCHEDULING_CHANNEL_NAME = BotConstants.SCHEDULING_CHANNEL_NAME;
    // Slight pattern abuse
    private final PureDBScheduleStrategy delegate;


    public SelectMenuScheduleStrategy(@Lazy GuildService guildService, @Lazy BossService bossService, @Lazy ScheduleService scheduleService) {
        this.guildService = guildService;
        this.bossService = bossService;
        this.scheduleService = scheduleService;
        delegate = new PureDBScheduleStrategy(guildService, bossService, scheduleService);
    }

    /**
     * Check if a guild already has a schedule
     *
     * @param jda
     * @param guildId String ID of the guild
     * @return true if a schedule exists, false otherwise
     */
    @Override
    public boolean hasActiveSchedule(JDA jda, String guildId) {
        return delegate.hasActiveSchedule(jda, guildId);
    }

    @Override
    public boolean hasActiveSchedule(JDA jda, String guildId, String name) {
        return delegate.hasActiveSchedule(jda, guildId, name);
    }

    /**
     * Creates a new schedule, this will remove any previous schedule WITHOUT WARNING
     *
     * @param ctx The context of the command call
     */
    @Override
    public void createSchedule(ICommandContext ctx) throws ScheduleException {
        createSchedule(ctx, "base");
    }

    @Override
    public void createSchedule(ICommandContext ctx, String name) throws ScheduleException {
        String categoryToFind;
        if (name.equals("") || name.equals("base")) {
            categoryToFind = SCHEDULING_CATEGORY_BASE;
        } else {
            categoryToFind = SCHEDULING_CATEGORY_BASE + "-" + name;
        }
        List<Category> categoryList = ctx.getGuild().getCategoriesByName(categoryToFind, true);
        if (categoryList.isEmpty()) {
            createScheduleChannel(ctx, categoryToFind);
        }
        scheduleService.createNewScheduleForGuild(ctx.getGuildId(), name);

        initScheduleChannel(ctx, categoryToFind, name);
    }

    private void createScheduleChannel(ICommandContext ctx, String fullCategoryName) {
        Category category = ctx.getGuild().createCategory(fullCategoryName)
                .addPermissionOverride(ctx.getGuild().getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND))
                .addPermissionOverride(ctx.getGuild().getRoleByBot("811219718584270868"), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                .complete();
        category.createTextChannel(BotConstants.SCHEDULING_CHANNEL_NAME).complete();
    }

    private void initScheduleChannel(ICommandContext ctx, String categoryName, String scheduleName) {
        Category category = ctx.getGuild().getCategoriesByName(categoryName, true).get(0);
        for (TextChannel channel : category.getTextChannels()) {
            if (channel.getName().equals(BotConstants.SCHEDULING_CHANNEL_NAME)) {
                int lapsToGenerate = guildService.getMessagesToDisplay(ctx.getGuildId());
                sendScheduleEmbeds(ctx, 1, lapsToGenerate, scheduleName, channel);
                return;
            }
        }
    }

    private List<MessageEmbed> createScheduleTitleEmbed(ICommandContext ctx, int startingLap, int lapsToGenerate, String scheduleName) {
        List<MessageEmbed> result = new ArrayList<>();
        EmbedBuilder title = new EmbedBuilder();
        title.setTitle("Scheduling for " + ctx.getGuild().getName());
        title.setDescription("WIP schedule to fix rate limits on discord API");
        result.add(title.build());
        List<MessageEmbed> laps = delegate.createEmbedsForLaps(ctx, startingLap, lapsToGenerate, scheduleName);
        result.addAll(laps);
        return result;
    }

    private List<SelectOption> getOptionsForAttackLap(ICommandContext ctx, int lap, String name, boolean firstLap) {
        List<SelectOption> result = new ArrayList<>();
        result.add(SelectOption.of("__LAP " + lap + " BOSSES__", BotConstants.STRING_MENU_NULL_VALUE + "-" + UUID.randomUUID())
                .withDefault(firstLap).withEmoji(Emoji.fromUnicode("⚠️")));

        for (int i = 1; i <= 5; i++) {
            String bossName = getBossName(ctx.getGuildId(), lap, i, name);
            result.add(SelectOption.of(bossName, BotConstants.STRING_MENU_ATTACK
                    + "-"
                    + i
                    + "-"
                    + lap
                    + "-"
                    + ctx.getGuildId()
                    + "-"
                    + name
            ).withEmoji(getEmojiForBoss(i)).withDefault(false));
        }

        return result;
    }

    private List<SelectOption> getOptionsForOTLap(ICommandContext ctx, int lap, String name, boolean firstLap) {
        List<SelectOption> result = new ArrayList<>();
        result.add(SelectOption.of("__LAP " + lap + " BOSSES__", BotConstants.STRING_MENU_NULL_VALUE + "-" + UUID.randomUUID())
                .withDefault(firstLap).withEmoji(Emoji.fromUnicode("⚠️")));

        for (int i = 1; i <= 5; i++) {
            String bossName = getBossName(ctx.getGuildId(), lap, i, name);
            result.add(SelectOption.of(bossName, BotConstants.STRING_MENU_ATTACK_OT
                    + "-"
                    + i
                    + "-"
                    + lap
                    + "-"
                    + ctx.getGuildId()
                    + "-"
                    + name
            ).withEmoji(getEmojiForBoss(i)).withDefault(false));
        }

        return result;
    }

    private List<SelectOption> getOptionsForLeaveLap(ICommandContext ctx, int lap, String name, boolean firstLap) {
        List<SelectOption> result = new ArrayList<>();
        result.add(SelectOption.of("__LAP " + lap + " BOSSES__", BotConstants.STRING_MENU_NULL_VALUE + "-" + UUID.randomUUID())
                .withDefault(firstLap).withEmoji(Emoji.fromUnicode("⚠️")));

        for (int i = 1; i <= 5; i++) {
            String bossName = getBossName(ctx.getGuildId(), lap, i, name);
            result.add(SelectOption.of(bossName, BotConstants.STRING_MENU_LEAVE
                    + "-"
                    + i
                    + "-"
                    + lap
                    + "-"
                    + ctx.getGuildId()
                    + "-"
                    + name
            ).withEmoji(getEmojiForBoss(i)).withDefault(false));
        }

        return result;
    }


    private List<StringSelectMenu> createBossAttackMenu(ICommandContext ctx, int startingLap, int lapsToGenerate, String name) {
        // Note that discord enforces a maximum of 25 options per menu, thus we want to create a separate schedule for each of those options
        int schedulesToCreate = ((lapsToGenerate - 1) / 3) + 1;
        List<StringSelectMenu.Builder> menus = new ArrayList<>();
        for (int i = 0; i < schedulesToCreate; i++) {
            String stringI = "";
            if (i > 0) {
                stringI = "-" + i;
            }
            StringSelectMenu.Builder schedule = StringSelectMenu.create(BotConstants.STRING_MENU_PREFIX
                    + "-"
                    + BotConstants.STRING_MENU_ATTACK
                    + "-"
                    + ctx.getGuildId()
                    + "-"
                    + name
                    + stringI
            );
            menus.add(schedule);
        }
        boolean firstLap = true;
        for (int i = 0; i < lapsToGenerate; i++) {
            if (i % 3 == 0) firstLap = true;
            List<SelectOption> options = getOptionsForAttackLap(ctx, startingLap + i, name, firstLap);
            firstLap = false;
            // No need for -1 or +1 here because we are 0 indexed
            int scheduleToGet = i / 3;
            menus.get(scheduleToGet).addOptions(options);
        }
        return new ArrayList<>() {{
            for (StringSelectMenu.Builder builder : menus) {
                add(builder.build());
            }
        }};
    }

    private List<StringSelectMenu> createOTMenu(ICommandContext ctx, int startingLap, int lapsToGenerate, String name) {
        // Note that discord enforces a maximum of 25 options per menu, thus we want to create a separate schedule for each of those options
        int schedulesToCreate = ((lapsToGenerate - 1) / 3) + 1;
        List<StringSelectMenu.Builder> menus = new ArrayList<>();
        for (int i = 0; i < schedulesToCreate; i++) {
            String stringI = "";
            if (i > 0) {
                stringI = "-" + i;
            }
            StringSelectMenu.Builder schedule = StringSelectMenu.create(BotConstants.STRING_MENU_PREFIX
                    + "-"
                    + BotConstants.STRING_MENU_ATTACK_OT
                    + "-"
                    + ctx.getGuildId()
                    + "-"
                    + name
                    + stringI
            );
            menus.add(schedule);
        }
        boolean firstLap = true;
        for (int i = 0; i < lapsToGenerate; i++) {
            if (i % 3 == 0) firstLap = true;
            List<SelectOption> options = getOptionsForOTLap(ctx, startingLap + i, name, firstLap);
            firstLap = false;
            // No need for -1 or +1 here because we are 0 indexed
            int scheduleToGet = i / 3;
            menus.get(scheduleToGet).addOptions(options);
        }
        return new ArrayList<>() {{
            for (StringSelectMenu.Builder builder : menus) {
                add(builder.build());
            }
        }};
    }

    private List<StringSelectMenu> createLeaveMenu(ICommandContext ctx, int startingLap, int lapsToGenerate, String name) {
        // Note that discord enforces a maximum of 25 options per menu, thus we want to create a separate schedule for each of those options
        int schedulesToCreate = ((lapsToGenerate - 1) / 3) + 1;
        List<StringSelectMenu.Builder> menus = new ArrayList<>();
        for (int i = 0; i < schedulesToCreate; i++) {
            String stringI = "";
            if (i > 0) {
                stringI = "-" + i;
            }
            StringSelectMenu.Builder schedule = StringSelectMenu.create(BotConstants.STRING_MENU_PREFIX
                    + "-"
                    + BotConstants.STRING_MENU_LEAVE
                    + "-"
                    + ctx.getGuildId()
                    + "-"
                    + name
                    + stringI
            );
            menus.add(schedule);
        }
        boolean firstLap = true;
        for (int i = 0; i < lapsToGenerate; i++) {
            if (i % 3 == 0) firstLap = true;
            List<SelectOption> options = getOptionsForLeaveLap(ctx, startingLap + i, name, firstLap);
            firstLap = false;
            // No need for -1 or +1 here because we are 0 indexed
            int scheduleToGet = i / 3;
            menus.get(scheduleToGet).addOptions(options);
        }
        return new ArrayList<>() {{
            for (StringSelectMenu.Builder builder : menus) {
                add(builder.build());
            }
        }};
    }

    private String getBossName(String guildId, int lap, int pos, String name) {
        int expectedAttacks = scheduleService.getExpectedAttacks(guildId, name, pos);

        String lapStr = "";
        if (lap < 10) {
            lapStr = "Lap0" + lap;
        } else {
            lapStr = "Lap" + lap;
        }

        String bossName = bossService.getBossFromLapAndPosition(lap, pos).getName();
        String expectedString = "";
        if (expectedAttacks > 0) {
            expectedString = "" + expectedAttacks;
        } else {
            expectedString = "?";
        }

        // This is going to be pretty slow, but we aren't rate limited on the database so who cares!!
        List<ScheduleEntryEntity> entries = scheduleService.getScheduleEntitiesForLapAndPos(guildId, name, lap, pos);
        int attackers = entries.size();
        return lapStr + " " + bossName + " (" + attackers + "/" + expectedString + ")";

    }

    private Emoji getEmojiForBoss(int bossPos) {
        return switch (bossPos) {
            case 1 -> Emoji.fromUnicode("1️⃣");
            case 2 -> Emoji.fromUnicode("2️⃣");
            case 3 -> Emoji.fromUnicode("3️⃣");
            case 4 -> Emoji.fromUnicode("4️⃣");
            case 5 -> Emoji.fromUnicode("5️⃣");
            default -> null;
        };
    }


    private void sendScheduleEmbeds(ICommandContext ctx, int startingLap, int lapsToGenerate, String name, TextChannel toSendIn) {
        toSendIn.sendMessageEmbeds(createScheduleTitleEmbed(ctx, startingLap, lapsToGenerate, name)).queue();
        EmbedTuple attackEmbed = createAttackEmbed(ctx, startingLap, lapsToGenerate, name);
        toSendIn.sendMessageEmbeds(attackEmbed.embed).setComponents(attackEmbed.actionRows).queue();
        EmbedTuple attackOTEmbed = createAttackOTEmbed(ctx, startingLap, lapsToGenerate, name);
        toSendIn.sendMessageEmbeds(attackOTEmbed.embed).setComponents(attackOTEmbed.actionRows).queue();
        EmbedTuple leaveEmbed = createLeaveEmbed(ctx, startingLap, lapsToGenerate, name);
        toSendIn.sendMessageEmbeds(leaveEmbed.embed).setComponents(leaveEmbed.actionRows).queue();
    }

    private EmbedTuple createAttackEmbed(ICommandContext ctx, int startingLap, int lapsToGenerate, String name) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Attack boss");
        builder.setDescription("Pick a boss below to attack");
        List<StringSelectMenu> menus = createBossAttackMenu(ctx, startingLap, lapsToGenerate, name);

        return new EmbedTuple(builder.build(), menus);
    }

    private EmbedTuple createAttackOTEmbed(ICommandContext ctx, int startingLap, int lapsToGenerate, String name) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Attack boss (OT)");
        builder.setDescription("Pick a boss below to attack if you are using an overtime attack");

        List<StringSelectMenu> menus = createOTMenu(ctx, startingLap, lapsToGenerate, name);
        return new EmbedTuple(builder.build(), menus);
    }

    private EmbedTuple createLeaveEmbed(ICommandContext ctx, int startingLap, int lapsToGenerate, String name) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Leave boss");
        builder.setDescription("Leave a boss if you are currently attacking it");

        List<StringSelectMenu> menus = createLeaveMenu(ctx, startingLap, lapsToGenerate, name);
        return new EmbedTuple(builder.build(), menus);
    }


    @Override
    public void resetSchedule(ICommandContext ctx) {
        resetSchedule(ctx, delegate.getOnlySpreadSheetName(ctx));
    }

    private String getCategoryName(ICommandContext ctx, String name) {
        String res = "";
        if (name.equals("") || name.equals("base")) {
            res = SCHEDULING_CATEGORY_BASE;
        } else {
            res = SCHEDULING_CATEGORY_BASE + "-" + name;
        }
        return res;
    }

    @Override
    public void resetSchedule(ICommandContext ctx, String name) {
        String categoryName = getCategoryName(ctx, name);
        scheduleService.resetSchedule(ctx.getGuildId(), name);
        clearScheduleChannel(ctx, categoryName);
        initScheduleChannel(ctx, categoryName, name);
    }


    @Override
    public void deleteSchedule(ICommandContext ctx) {
        delegate.deleteSchedule(ctx);
    }

    @Override
    public void deleteSchedule(ICommandContext ctx, String name) {
        delegate.deleteSchedule(ctx, name);
    }

    @Override
    public void updateSchedule(JDA jda, String guildId, boolean bossDead) {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        updateSchedule(jda, guildId, bossDead, delegate.getOnlySpreadSheetName(ctx));
    }

    @Override
    public void updateSchedule(JDA jda, String guildId, boolean bossDead, String name) {
        for (EmbedType type : EmbedType.values()) {
            updateMenu(jda, guildId, name, type);
        }
        updateScheduleEmbeds(jda, guildId, name);
    }

    private void updateScheduleEmbeds(JDA jda, String guildId, String scheduleName) {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        String categoryName = getCategoryName(ctx, scheduleName);
        Category category = ctx.getGuild().getCategoriesByName(categoryName, true).get(0);
        TextChannel channel = category.getTextChannels().get(0);
        Message titleMessage = null;
        for (Message message : channel.getHistory().retrievePast(10).complete()) {
            if (message.getEmbeds().size() > 1 && message.getComponents().size() == 0) {
                titleMessage = message;
                break;
            }
        }
        int lap = scheduleService.getCurrentLap(guildId, scheduleName);
        int lapsToGenerate = guildService.getMessagesToDisplay(guildId);
        List<MessageEmbed> newEmbeds = createScheduleTitleEmbed(ctx, lap, lapsToGenerate, scheduleName);
        if (titleMessage == null) return;
        titleMessage.editMessageEmbeds(newEmbeds).queue();
    }

    private void updateMenu(JDA jda, String guildId, String scheduleName, EmbedType type) {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        String categoryName = getCategoryName(ctx, scheduleName);
        // Haha very funny null pointer galore
        TextChannel channel = jda.getGuildById(guildId).getCategoriesByName(categoryName, true).get(0).getTextChannels().get(0);
        List<Message> history = channel.getHistory().retrievePast(10).complete();
        Message message = switch (type) {
            case ATTACK -> findMessageByTitle(history, "Attack boss");
            case ATTACK_OT -> findMessageByTitle(history, "Attack boss (OT)");
            case LEAVE -> findMessageByTitle(history, "Leave boss");
        };
        if (message == null) return;
        int currentLap = scheduleService.getCurrentLap(guildId, scheduleName);
        int lapsToShow = guildService.getMessagesToDisplay(guildId);
        List<StringSelectMenu> menus = switch (type) {
            case ATTACK -> new ArrayList<>(createBossAttackMenu(ctx, currentLap, lapsToShow, scheduleName));
            case ATTACK_OT -> new ArrayList<>(createOTMenu(ctx, currentLap, lapsToShow, scheduleName));
            case LEAVE -> new ArrayList<>(createLeaveMenu(ctx, currentLap, lapsToShow, scheduleName));
        };
        List<ActionRow> rows = new ArrayList<>() {{
            for (StringSelectMenu menu : menus) {
                add(ActionRow.of(menu));
            }
        }};
        message.editMessageComponents(rows).queue();
    }

    private Message findMessageByTitle(List<Message> toSearch, String title) {
        for (Message message : toSearch) {
            List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds.isEmpty()) continue;
            if (embeds.get(0).getTitle().equalsIgnoreCase(title)) return message;
        }
        return null;
    }

    private void clearScheduleChannel(ICommandContext ctx, String categoryName) {
        List<Category> categories = ctx.getGuild().getCategoriesByName(categoryName, true);
        if (categories.isEmpty()) return;
        List<TextChannel> channels = categories.get(0).getTextChannels();
        if (channels.isEmpty()) return;
        List<Message> messages = channels.get(0).getHistory().retrievePast(100).complete(); // Must complete here so we are sure we snapshot messages
        for (Message message : messages) {
            message.delete().queue(); // At this point queuing shouldn't change the control flow of the program
        }
    }

    @Override
    public void setExpectedAttacks(CommandContext ctx, int pos, int expected) {
        setExpectedAttacks(ctx, pos, expected, delegate.getOnlySpreadSheetName(ctx));
    }

    @Override
    public void setExpectedAttacks(CommandContext ctx, int pos, int expected, String scheduleName) {
        if (scheduleName.equals("")) scheduleName = "base";
        scheduleService.setExpectedAttacks(ctx.getGuildId(), scheduleName, pos, expected);
        updateSchedule(ctx.getJDA(), ctx.getGuildId(), false, scheduleName);
    }

    @Override
    public void addAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberAlreadyExistsException, MemberHasAlreadyAttackedException {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        addAttacker(jda, guildId, position, lap, name, delegate.getOnlySpreadSheetName(ctx), false);
    }

    @Override
    public void removeAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberIsNotAttackingException {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        removeAttacker(jda, guildId, position, lap, name, delegate.getOnlySpreadSheetName(ctx));
    }

    @Override
    public void markFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasAlreadyAttackedException, MemberIsNotAttackingException {

    }

    @Override
    public void unMarkFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasNotAttackedException {

    }

    @Override
    public void addAttacker(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName, boolean isOvertime) throws MemberAlreadyExistsException, MemberHasAlreadyAttackedException {
        System.out.println("Adding attacker: " + name + " to pos: " + position + " lap: " + lap + " OT: " + isOvertime);
        Member member = jda.getGuildById(guildId).getMembersByEffectiveName(name, false).get(0);
        String nickname = member.getNickname();
        if (nickname == null) {
            nickname = name;
        }
        if (scheduleService.userIsAttackingBoss(guildId, scheduleName, member.getId(), lap, position)) throw new MemberAlreadyExistsException();
        scheduleService.addAttacker(guildId, scheduleName, lap, position, member.getId(), nickname, isOvertime);
        //updateSchedule(jda, guildId, false, scheduleName);
        EmbedType type = isOvertime ? EmbedType.ATTACK_OT : EmbedType.ATTACK;
        //updateMenu(jda, guildId, scheduleName, type);
        updateScheduleEmbeds(jda, guildId, scheduleName);
        delegate.askForDamage(jda, guildId, member.getId(), position, lap, scheduleName);
    }



    @Override
    public void removeAttacker(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName) throws MemberIsNotAttackingException {
        System.out.println("Removing attacker: " + name + " to pos: " + position + " lap: " + lap );
        Member member = jda.getGuildById(guildId).getMembersByEffectiveName(name, false).get(0);
        boolean removed = scheduleService.removeUserFromBoss(guildId, scheduleName, member.getId(), lap, position);
        if (!removed) throw new MemberIsNotAttackingException();
        //updateSchedule(jda, guildId, false, scheduleName);
        updateScheduleEmbeds(jda, guildId, scheduleName);
        //updateMenu(jda, guildId, scheduleName, EmbedType.LEAVE);
    }

    /**
     * Checks if a given user is currently signed up for the current boss of the guild
     *
     * @param jda     The discord JDA
     * @param guildId GuildID of the user
     * @param user    The display name of the user
     * @return true if the user is signed up for the current boss and has not completed their attack
     */
    @Override
    public boolean isAttackingCurrentBoss(JDA jda, String guildId, String user) {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        return isAttackingCurrentBoss(jda, guildId, user, delegate.getOnlySpreadSheetName(ctx));
    }

    @Override
    public boolean isAttackingCurrentBoss(JDA jda, String guildId, String user, String scheduleName) {
        int currentLap = scheduleService.getCurrentLap(guildId, scheduleName);
        int currentPos = scheduleService.getCurrentPos(guildId, scheduleName);
        return scheduleService.userIsAttackingBoss(guildId, scheduleName, user, currentLap, currentPos);
    }

    @Override
    public void setNextBoss(CommandContext ctx) {
        setNextBoss(ctx, delegate.getOnlySpreadSheetName(ctx));
    }

    @Override
    public boolean hasMoreThanOneSchedule(CommandContext ctx) {
        return delegate.hasMoreThanOneSchedule(ctx);
    }

    @Override
    public void setNextBoss(CommandContext ctx, String scheduleName) {
        int currentPos = scheduleService.getCurrentPos(ctx.getGuildId(), scheduleName);
        int currentLap = scheduleService.getCurrentLap(ctx.getGuildId(), scheduleName);
        if (currentPos == 5) {
            scheduleService.setLapAndPosition(ctx.getGuildId(), currentLap + 1, 1, scheduleName);
            updateSchedule(ctx.getJDA(), ctx.getGuildId(), false, scheduleName);
        } else {
            scheduleService.setLapAndPosition(ctx.getGuildId(), currentLap, currentPos + 1, scheduleName);
            updateScheduleEmbeds(ctx.getJDA(), ctx.getGuildId(), scheduleName);
        }
    }

    @Override
    public void updateDamage(ICommandContext ctx, String scheduleName, String effectiveName, int pos, int lap, int expectedDamage) {
        Member member = ctx.getGuild().getMembersByEffectiveName(effectiveName, true).get(0);
        scheduleService.setExpectedDamage(ctx.getGuildId(), scheduleName, pos, lap, member.getId(), expectedDamage);
        //updateSchedule(ctx.getJDA(), ctx.getGuildId(), false, scheduleName);
        updateScheduleEmbeds(ctx.getJDA(), ctx.getGuildId(), scheduleName);
    }

    private static class EmbedTuple {
        private final MessageEmbed embed;
        private final ItemComponent actionRow;
        private final List<LayoutComponent> actionRows;

        public EmbedTuple(MessageEmbed embed, ItemComponent actionRow) {
            this.embed = embed;
            this.actionRow = actionRow;
            this.actionRows = new ArrayList<>() {{
                add(ActionRow.of(actionRow));
            }};
        }

        public EmbedTuple(MessageEmbed embed, List<? extends ItemComponent> actionRows) {
            this.embed = embed;
            actionRow = null;
            this.actionRows = new ArrayList<>() {{
                for (ItemComponent row : actionRows) {
                    add(ActionRow.of(row));
                }
            }};
        }

        public MessageEmbed embed() {
            return embed;
        }
    }

    private enum EmbedType {
        ATTACK,
        ATTACK_OT,
        LEAVE
    }
}
