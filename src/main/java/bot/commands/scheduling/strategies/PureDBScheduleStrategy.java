package bot.commands.scheduling.strategies;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommandContext;
import bot.commands.framework.ManualCommandContext;
import bot.common.BotConstants;
import bot.common.ScheduleButtonType;
import bot.exceptions.MemberAlreadyExistsException;
import bot.exceptions.MemberHasAlreadyAttackedException;
import bot.exceptions.MemberHasNotAttackedException;
import bot.exceptions.MemberIsNotAttackingException;
import bot.exceptions.schedule.ScheduleException;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.ScheduleService;
import bot.storage.models.BossEntity;
import bot.storage.models.ScheduleEntryEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PureDBScheduleStrategy implements ScheduleStrategy {
    private final GuildService guildService;
    private final BossService bossService;
    private final ScheduleService scheduleService;
    private final String SCHEDULING_CATEGORY_BASE = BotConstants.SCHEDULING_CATEGORY_NAME;
    private final String SCHEDULING_CHANNEL_NAME = BotConstants.SCHEDULING_CHANNEL_NAME;

    public PureDBScheduleStrategy(@Lazy GuildService guildService, @Lazy BossService bossService, @Lazy ScheduleService scheduleService) {
        this.guildService = guildService;
        this.bossService = bossService;
        this.scheduleService = scheduleService;
        System.out.println("Bean of PureDBScheduleStrategy created");
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
        List<String> schedules = scheduleService.getScheduleNamesForGuild(guildId);
        System.out.println("HasActiveSchedule found schedules: " + schedules + " with size: " + schedules.size());
        return schedules.size() != 0;
    }

    @Override
    public boolean hasActiveSchedule(JDA jda, String guildId, String name) {
        return hasActiveSchedule(jda, guildId);
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

    protected String getOnlySpreadSheetName(ICommandContext ctx) {
        return scheduleService.getScheduleNamesForGuild(ctx.getGuildId()).get(0);
    }

    @Override
    public void createSchedule(ICommandContext ctx, String name) throws ScheduleException {
        System.out.println("PureDBScheduleStrategy in createschedule with name: " + name);
        String categoryToFind;
        if (name.equals("") || name.equals("base")) {
            categoryToFind = SCHEDULING_CATEGORY_BASE;
        } else {
            categoryToFind = SCHEDULING_CATEGORY_BASE + "-" + name;
        }
        List<Category> categories = ctx.getGuild().getCategoriesByName(categoryToFind, true);
        if (categories.size() == 0) {
            System.out.println("PDBSS: Creating channels");
            createChannels(ctx, categoryToFind);
        }
        System.out.println("PDBSS: Asking schedule service to create schedule for guild");
        scheduleService.createNewScheduleForGuild(ctx.getGuildId(), name);
        System.out.println("PDBSS: Calling InitScheduleChannel");
        initScheduleChannel(ctx, categoryToFind, name);
        System.out.println("PDBSS: Calling InitBossChannels");
        initBossChannels(ctx, categoryToFind, name);
    }

    private void createChannels(ICommandContext ctx, String fullCategoryName) {
        /* Async method needs to queue next methods itself, otherwise we have problems, so rewritten to use complete for now
        TODO: Make this actally async :)
        ctx.getGuild().createCategory(fullCategoryName)
                .addPermissionOverride(ctx.getGuild().getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND))
                // TODO: Fix hard coded bot id
                .addPermissionOverride(ctx.getGuild().getRoleByBot("811219718584270868"), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                .queue(category -> {
                    category.createTextChannel("schedule").queue();
                    category.createTextChannel("boss_1").queue();
                    category.createTextChannel("boss_2").queue();
                    category.createTextChannel("boss_3").queue();
                    category.createTextChannel("boss_4").queue();
                    category.createTextChannel("boss_5").queue();
        });
         */
        Category category = ctx.getGuild().createCategory(fullCategoryName)
                .addPermissionOverride(ctx.getGuild().getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND))
                .addPermissionOverride(ctx.getGuild().getRoleByBot("811219718584270868"), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                .complete();
        category.createTextChannel("schedule").complete();
        category.createTextChannel("boss_1").complete();
        category.createTextChannel("boss_2").complete();
        category.createTextChannel("boss_3").complete();
        category.createTextChannel("boss_4").complete();
        category.createTextChannel("boss_5").complete();
    }

    private void initScheduleChannel(ICommandContext ctx, String categoryName, String scheduleName) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Category category = ctx.getGuild().getCategoriesByName(categoryName, true).get(0);
        List<TextChannel> channels = category.getTextChannels();
        System.out.println("Found channels: " + channels);
        for (TextChannel channel : channels) {
            if (channel.getName().equals(BotConstants.SCHEDULING_CHANNEL_NAME)) {
                int lapsToGenerate = guildService.getMessagesToDisplay(ctx.getGuildId());
                channel.sendMessageEmbeds(createScheduleEmbeds(ctx, 1, lapsToGenerate, scheduleName)).queue();
                return;
            }
        }
    }

    private void initBossChannels(ICommandContext ctx, String fullCategoryName, String scheduleName) {
        Category category = ctx.getGuild().getCategoriesByName(fullCategoryName, true).get(0);
        List<TextChannel> channels = category.getTextChannels();
        for (int i = 1; i <= 5; i++) {
            int lapsToGenerate = guildService.getMessagesToDisplay(ctx.getGuildId());
            for (int j = 0; j < lapsToGenerate; j++) {
                channels.get(i).sendMessageEmbeds(createBossEmbed(ctx, scheduleName, i, j + 1))
                        .setActionRow(createButtons(ctx.getGuildId(), i, j + 1, scheduleName)).queue();
            }
        }
    }

    private List<MessageEmbed> createScheduleEmbeds(ICommandContext ctx, int startingLap, int lapsToGenerate, String scheduleName) {
        List<MessageEmbed> result = new ArrayList<>();
        EmbedBuilder title = new EmbedBuilder();
        title.setTitle("Scheduling for " + ctx.getGuild().getName());
        title.setDescription("This channel will contain an overview of all current bosses, as well as what " +
                "members plan to attack or have already attacked." +
                "To queue up for an attack, use the generated channels `#boss_1` to `#boss_5`." +
                "Admins can manually add or remove members using the commands `!addspot <@user> <position> <lap>`." +
                "To change the current boss shown use `!nextboss`." +
                "The schedule does not automatically update, and must be manually updated using `!nextboss`." +
                "\nTo set the expected number of attacks for a boss use `!expected <position> <expected attacks>`." +
                "\nThe amount of laps shown at once in this channel can be changed using `!lapsToShow <amount>`, " +
                "this should never be changed in the middle of a CB. After setting the laps to show, please use `!resetschedule`" +
                "to avoid unexpected behaviour");

        result.add(title.build());

        List<MessageEmbed> laps = createEmbedsForLaps(ctx, startingLap, lapsToGenerate, scheduleName);
        result.addAll(laps);
        return result;
    }

    protected List<MessageEmbed> createEmbedsForLaps(ICommandContext ctx, int startingLap, int lapsToGenerate, String scheduleName) {
        List<MessageEmbed> result = new ArrayList<>();
        for (int i = 0; i < lapsToGenerate; i++) {
            EmbedBuilder lap = new EmbedBuilder();
            lap.setTitle("Lap " + (startingLap + i));
            for (int j = 0; j < 5; j++) {
                //BossEntity currentBoss = scheduleService.getCurrentBoss(ctx.getGuildId(), scheduleName);
                StringBuilder titleString = new StringBuilder();
                BossEntity boss = bossService.getBossFromLapAndPosition(startingLap + i, j + 1);
                titleString.append(boss.getName());
                int currentLap = scheduleService.getCurrentLap(ctx.getGuildId(), scheduleName);
                int currentPos = scheduleService.getCurrentPos(ctx.getGuildId(), scheduleName);

                if (startingLap + i == currentLap && currentPos == (j + 1)) {
                    titleString.append(" __Current Boss__");
                }

                int expectedAttacks = scheduleService.getExpectedAttacks(ctx.getGuildId(), scheduleName, j + 1);
                String expectedString = expectedAttacks + "";
                if (expectedAttacks == -1) {
                    expectedString = "?";
                }
                List<ScheduleEntryEntity> entries = scheduleService.getScheduleEntitiesForLapAndPos(ctx.getGuildId(), scheduleName, startingLap + i, j + 1);
                titleString.append(" (")
                        .append(entries.size())
                        .append("/")
                        .append(expectedString)
                        .append(") ");
                titleString.append(" HP: ")
                        .append(boss.getTotalHealth());
                StringBuilder infoBuilder = new StringBuilder();
                String prefix = "";
                for (ScheduleEntryEntity entry : entries) {
                    infoBuilder.append(prefix);
                    if (entry.isOvertime()) {
                        infoBuilder.append("__OT__    ");
                    }
                    Member member = ctx.getGuild().getMemberById(entry.getAttackerId());
                    User user = ctx.getGuild().getJDA().getUserById(member.getId());
                    infoBuilder.append(user.getName());
                    if (member.getNickname() != null) {
                        infoBuilder
                                .append(" (")
                                .append(member.getNickname())
                                .append(") ");
                    }
                    if (entry.getExpectedDamage() != 0) {
                        infoBuilder
                                .append("    **")
                                .append(entry.getExpectedDamage())
                                .append("**");
                    }

                    prefix = "\n";
                }
                lap.addField(titleString.toString(), infoBuilder.toString(), false);
            }
            result.add(lap.build());
        }
        return result;
    }

    private MessageEmbed createBossEmbed(ICommandContext ctx, String scheduleName, int bossPosition, int lap) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Lap: " + lap);
        StringBuilder attacking = new StringBuilder();
        List<ScheduleEntryEntity> entries = scheduleService.getScheduleEntitiesForLapAndPos(ctx.getGuildId(), scheduleName, lap, bossPosition);
        String prefix = "";
        for (ScheduleEntryEntity entry : entries) {
            attacking.append(prefix)
                    .append(entry.getAttackerNick());
            prefix = ", ";
        }
        eb.addField("Attacking", attacking.toString(), false);
        return eb.build();
    }

    private List<Button> createButtons(String guildId, int bossPosition, int lap, String scheduleName) {
        if (scheduleName.equals("")) scheduleName = "base";
        String idSuffix = "-" + guildId + "-" + lap + "-" + bossPosition + "-";
        Button joinButton = Button.success(ScheduleButtonType.JOIN + idSuffix + "noovertime-" + scheduleName, "Join");
        Button joinOTButton = Button.primary(ScheduleButtonType.JOIN + idSuffix + "overtime-" + scheduleName, "Join (OT)");
        Button leaveButton = Button.danger(ScheduleButtonType.LEAVE + idSuffix + "filler-" + scheduleName, "Leave");
        return new ArrayList<>() {{
            add(joinButton);
            add(joinOTButton);
            add(leaveButton);
        }};
    }


    @Override
    public void resetSchedule(ICommandContext ctx) {
        resetSchedule(ctx, getOnlySpreadSheetName(ctx));
    }

    @Override
    public void resetSchedule(ICommandContext ctx, String name) {
        String categoryName = "";
        if (name.equals("") || name.equals("base")) {
            categoryName = SCHEDULING_CATEGORY_BASE;
        } else {
            categoryName = SCHEDULING_CATEGORY_BASE + "-" + name;
        }
        scheduleService.resetSchedule(ctx.getGuildId(), name);
        clearChannels(ctx, categoryName);
        initScheduleChannel(ctx, categoryName, name);
        initBossChannels(ctx, categoryName, name);
    }

    protected void clearChannels(ICommandContext ctx, String fullCategoryName) {
        List<Category> categories = ctx.getGuild().getCategoriesByName(fullCategoryName, false);
        if (categories.isEmpty()) return;
        Category category = categories.get(0);
        System.out.println("GETTING A LOT OF HISTORY IN CLEAR CHANNELS");
        category.getTextChannels().forEach(channel ->
                channel.getHistory()
                        .retrievePast(100)
                        .queue(messages ->
                                messages.forEach(message ->
                                        message.delete().queue())));
    }

    @Override
    public void deleteSchedule(ICommandContext ctx) {
        deleteSchedule(ctx, getOnlySpreadSheetName(ctx));
    }

    @Override
    public void deleteSchedule(ICommandContext ctx, String name) {
        String categoryName = "";
        if (name.equals("") || name.equals("base")) {
            categoryName = SCHEDULING_CATEGORY_BASE;
        } else {
            categoryName = SCHEDULING_CATEGORY_BASE + "-" + name;
        }
        if (!hasActiveSchedule(ctx.getJDA(), ctx.getGuildId(), name)) return;
        scheduleService.deleteSchedule(ctx.getGuildId(), name);
        List<Category> categories = ctx.getGuild().getCategoriesByName(categoryName, true);
        if (categories.isEmpty()) {
            return;
        }
        Category category = categories.get(0);
        category.getTextChannels().forEach(channel -> channel.delete().complete());
        category.delete().complete();
    }

    @Override
    public void updateSchedule(JDA jda, String guildId, boolean bossDead) {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
       updateSchedule(jda, guildId, bossDead, getOnlySpreadSheetName(ctx));
    }

    @Override
    public void updateSchedule(JDA jda, String guildId, boolean bossDead, String name) {
        System.out.println("DEBUG UPDATING SCHEDULE");
        String categoryName = "";
        if (name.equals("") || name.equals("base")) {
            categoryName = SCHEDULING_CATEGORY_BASE;
        } else {
            categoryName = SCHEDULING_CATEGORY_BASE + "-" + name;
        }
        Category category = jda.getGuildById(guildId).getCategoriesByName(categoryName, true).get(0);
        List<TextChannel> channels = category.getTextChannels();
        int lap = scheduleService.getCurrentLap(guildId, name);
        ICommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        int lapsToGenerate = guildService.getMessagesToDisplay(ctx.getGuildId());
        System.out.println("Known channels: " + channels);

        for (TextChannel channel : channels) {
            if (channel.getName().equals(SCHEDULING_CHANNEL_NAME)) {
                System.out.println("GETTING HISTORY IN UPDATESCHEDULE");
                channel.getHistory().retrievePast(10).queue(messages -> {
                    messages.get(0).editMessageEmbeds(createScheduleEmbeds(ctx, lap, lapsToGenerate, name)).queue();
                });
            }
            if (channel.getName().contains("boss_")) {
                String[] nameContent = channel.getName().split("_");
                int channelPos = Integer.parseInt(nameContent[1]);
                System.out.println("GETTING HISTORY IN UPDATE SCHEDULE");
                channel.getHistory().retrievePast(10).queue(messages -> {
                    int highestLap = 0;
                    for (Message message : messages) {
                        MessageEmbed embed = message.getEmbeds().get(0);
                        String lapString = embed.getTitle().split(":")[1].strip();
                        int bossMessageLap = Integer.parseInt(lapString);
                        if (bossMessageLap > highestLap) highestLap = bossMessageLap;
                        message.editMessageEmbeds(createBossEmbed(ctx, name, channelPos, bossMessageLap)).queue();
                    }
                    //System.out.println("Highest lap: " + highestLap);
                    //System.out.println("Current lap: " + lap);
                    //System.out.println("Laps to generate: " + lapsToGenerate);
                    if (highestLap < (lap + lapsToGenerate - 1)) {
                        for (int i = highestLap + 1; i < lap + lapsToGenerate; i++) {
                            channel.sendMessageEmbeds(createBossEmbed(ctx, name, channelPos, i))
                                    .setActionRow(createButtons(guildId, channelPos, i, name))
                                    .queue();
                        }
                    }
                });
            }

        }
        System.out.println("DEBUG DONE UPDATING SCHEDULE");
    }

    @Override
    public void addAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberAlreadyExistsException, MemberHasAlreadyAttackedException {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
        addAttacker(jda, guildId, position, lap, name, getOnlySpreadSheetName(ctx), false);
    }

    @Override
    public void addAttacker(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName, boolean isOvertime) throws MemberAlreadyExistsException, MemberHasAlreadyAttackedException {
        System.out.println("Adding attacker with effective name: " + name);
        Member user = jda.getGuildById(guildId).getMembersByEffectiveName(name, false).get(0);
        System.out.println("Found user: " + user + " with nickname: " + user.getNickname());
        String nickname = user.getNickname();
        if (nickname == null) {
            nickname = name;
        }
        String categoryName = "";
        if (name.equals("") || name.equals("base")) {
            categoryName = SCHEDULING_CATEGORY_BASE;
        } else {
            categoryName = SCHEDULING_CATEGORY_BASE + "-" + scheduleName;
        }
        if (scheduleService.userIsAttackingBoss(guildId, scheduleName, user.getId(), lap, position)) throw new MemberAlreadyExistsException();
        scheduleService.addAttacker(guildId, scheduleName, lap, position, user.getId(), nickname, isOvertime);
        updateSchedule(jda, guildId, false, scheduleName);
        askForDamage(jda, guildId, user.getId(), position, lap, scheduleName);
        //updateEmbed(jda, guildId, categoryName, scheduleName, lap, position);
    }

    @Override
    public void removeAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberIsNotAttackingException {
        ManualCommandContext ctx = new ManualCommandContext(jda.getGuildById(guildId), guildId, jda);
       removeAttacker(jda, guildId, position, lap, name, getOnlySpreadSheetName(ctx));
    }

    @Override
    public void removeAttacker(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName) throws MemberIsNotAttackingException {
        String categoryName = "";
        if (scheduleName.equals("") || scheduleName.equals("base")) {
            categoryName = SCHEDULING_CATEGORY_BASE;
        } else {
            categoryName = SCHEDULING_CATEGORY_BASE + "- " + scheduleName;
        }
        Member user = jda.getGuildById(guildId).getMembersByEffectiveName(name, false).get(0);
        boolean removed = scheduleService.removeUserFromBoss(guildId, scheduleName, user.getId(), lap, position);
        if (!removed) throw new MemberIsNotAttackingException();
        //updateEmbed(jda, guildId, categoryName, scheduleName, lap, position);
        updateSchedule(jda, guildId, false, scheduleName);
    }

    @Override
    public void markFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasAlreadyAttackedException, MemberIsNotAttackingException {

    }

    @Override
    public void unMarkFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasNotAttackedException {

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
        return isAttackingCurrentBoss(jda, guildId, user, getOnlySpreadSheetName(ctx));
    }

    @Override
    public boolean isAttackingCurrentBoss(JDA jda, String guildId, String user, String scheduleName) {
        int currentLap = scheduleService.getCurrentLap(guildId, scheduleName);
        int currentPos = scheduleService.getCurrentPos(guildId, scheduleName);
        return scheduleService.userIsAttackingBoss(guildId, scheduleName, user, currentLap, currentPos);
    }

    protected void askForDamage(JDA jda, String guildId, String userID, int pos, int lap, String scheduleName) {
        User user = jda.getUserById(userID);
        MessageEmbed embed = createAskForDamageEmbed(pos, lap, scheduleName, guildId);
        user.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed))
                .onErrorFlatMap(throwable -> {
                    TextChannel channel = getUpdateChannel(jda, guildId);
                    if (channel == null) {
                        System.out.println("__________PANIC__________");
                        System.out.println("ASKING FOR DAMAGE WITH DMS CLOSED AND NO UPDATECHANNEL IS SET");
                        System.out.println("WE HAVE TO CONTINUE BUT THIS IS FATAL AND SHOULD BE IMPOSSIBLE");
                        System.out.println("__________PANIC__________");
                    }
                    boolean canUseThreads = guildService.getUseThreads(guildId);
                    if (!canUseThreads) {
                        channel.sendMessageEmbeds(embed).queue();
                        return channel.sendMessage("<@" + user.getId() + ">");
                    }
                    String updateChannelId = guildService.getUpdatesChannelId(guildId);
                    ThreadChannel threadChannel = findThreadChannel(jda, guildId, updateChannelId, user.getName());
                    if (threadChannel == null) {
                        return jda.getGuildById(guildId)
                                .getTextChannelById(updateChannelId)
                                .createThreadChannel(user.getName())
                                .flatMap(c -> {
                                    c.sendMessageEmbeds(embed).queue();
                                    return c.sendMessage("<@" + user.getId() + ">");
                                });
                    }
                    threadChannel.sendMessageEmbeds(embed).queue();
                    return threadChannel.sendMessage("<@" + user.getId() + ">");
                }).queue();

    }

    private ThreadChannel findThreadChannel(JDA jda, String guildId, String updateChannelId, String name) {
        if (updateChannelId == null) return null;
        List<ThreadChannel> channels = jda.getGuildById(guildId).getTextChannelById(updateChannelId).getThreadChannels();
        for (ThreadChannel channel : channels) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return null;
    }

    private TextChannel getUpdateChannel(JDA jda, String guildId) {
        String id = guildService.getUpdatesChannelId(guildId);
        return jda.getGuildById(guildId).getTextChannelById(id);

    }

    private MessageEmbed createAskForDamageEmbed(int pos, int lap, String scheduleName, String guildId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Lap: " + lap + " Boss: " + pos + " Schedule: " + scheduleName + " Guild: " + guildId);
        eb.setDescription("Please reply below with the damage you expect to deal on your attack for boss " + pos + " in lap " + lap);
        eb.setFooter(BotConstants.ASK_FOR_DAMAGE_ID);
        return eb.build();
    }

    @Override
    public void setNextBoss(CommandContext ctx) {
        setNextBoss(ctx, getOnlySpreadSheetName(ctx));
    }

    @Override
    public void setNextBoss(CommandContext ctx, String scheduleName) {
        System.out.println("DEBUG SETTING NEXT BOS");
        int currentPos = scheduleService.getCurrentPos(ctx.getGuildId(), scheduleName);
        System.out.println("CURRENT POS: " + currentPos);
        int currentLap = scheduleService.getCurrentLap(ctx.getGuildId(), scheduleName);
        System.out.println("CURRENT LAP: " + currentLap);
        if (currentPos == 5) {
            System.out.println("LAST POS OF LAP");
            scheduleService.setLapAndPosition(ctx.getGuildId(), currentLap + 1, 1, scheduleName);
        } else {
            scheduleService.setLapAndPosition(ctx.getGuildId(), currentLap, currentPos + 1, scheduleName);
        }
        updateSchedule(ctx.getJDA(), ctx.getGuildId(), false, scheduleName);
    }

    @Override
    public void setExpectedAttacks(CommandContext ctx, int pos, int expected) {
        setExpectedAttacks(ctx, pos, expected, getOnlySpreadSheetName(ctx));
    }

    @Override
    public void setExpectedAttacks(CommandContext ctx, int pos, int expected, String scheduleName) {
        if (scheduleName.equals("")) scheduleName = "base";
        scheduleService.setExpectedAttacks(ctx.getGuildId(), scheduleName, pos, expected);
        updateSchedule(ctx.getJDA(), ctx.getGuildId(), false, scheduleName);
    }

    @Override
    public boolean hasMoreThanOneSchedule(CommandContext ctx) {
        List<String> schedules = scheduleService.getScheduleNamesForGuild(ctx.getGuildId());
        return  schedules.size() > 1;
    }

    @Override
    public void updateDamage(ICommandContext ctx, String scheduleName, String effectiveName, int pos, int lap, int expectedDamage) {
        Member member = ctx.getGuild().getMembersByEffectiveName(effectiveName, true).get(0);
        scheduleService.setExpectedDamage(ctx.getGuildId(), scheduleName, pos, lap, member.getId(), expectedDamage);
        updateSchedule(ctx.getJDA(), ctx.getGuildId(), false, scheduleName);
    }
}
