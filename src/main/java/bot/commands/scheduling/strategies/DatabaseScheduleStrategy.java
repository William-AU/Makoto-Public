package bot.commands.scheduling.strategies;

import bot.commands.framework.ICommandContext;
import bot.common.BotConstants;
import bot.common.CBUtils;
import bot.exceptions.*;
import bot.exceptions.schedule.ScheduleException;
import bot.services.BossService;
import bot.services.DatabaseScheduleService;
import bot.services.GuildService;
import bot.storage.models.BossEntity;
import bot.storage.models.DBScheduleEntity;
import bot.storage.models.GuildEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatabaseScheduleStrategy implements ScheduleStrategy{
    private final DatabaseScheduleService scheduleService;
    private final GuildService guildService;
    private final BossService bossService;

    public DatabaseScheduleStrategy(DatabaseScheduleService scheduleService, GuildService guildService, BossService bossService) {
        this.scheduleService = scheduleService;
        this.guildService = guildService;
        this.bossService = bossService;
    }

    /**
     * Check if a guild already has a schedule
     *
     * @param guildId String ID of the guild
     * @return true if a schedule exists, false otherwise
     */
    @Override
    public boolean hasActiveSchedule(String guildId) {
        // There will always be a schedule, it is just sometimes empty, but that doesn't matter
        return true;
    }


    /**
     * Creates a new schedule, this will remove any previous schedule WITHOUT WARNING
     *
     * @param ctx The context of the command call
     */
    @Override
    public void createSchedule(ICommandContext ctx) throws ScheduleException {
        // Treat this as a reset
        scheduleService.reset(ctx.getGuildId());
        bossService.initNewBoss(ctx.getGuildId());

        if (channelsDoNotExist(ctx)) {
            createChannels(ctx);
        } else {
            clearChannels(ctx);
        }
        initScheduleChannel(ctx);
        initBossChannels(ctx);
    }

    private void initScheduleChannel(ICommandContext ctx) {
        Category category = ctx.getGuild().getCategoriesByName(BotConstants.SCHEDULING_CATEGORY_NAME, false).get(0);
        List<TextChannel> channels = category.getTextChannels();
        for (TextChannel channel : channels) {
            if (channel.getName().equals(BotConstants.SCHEDULING_CHANNEL_NAME)) {
                channel.sendMessageEmbeds(createScheduleEmbeds(ctx, 1, 2)).queue();
                return;
            }
        }
    }

    private List<MessageEmbed> createScheduleEmbeds(ICommandContext ctx, int startingLap, int numberOfLaps) {
        List<MessageEmbed> result = new ArrayList<>();
        EmbedBuilder title = new EmbedBuilder();
        GuildEntity guildEntity = guildService.getGuild(ctx.getGuildId());
        title.setTitle("Scheduling for " + ctx.getGuild().getName());
        title.setDescription("This channel will contain an overview of all current bosses, as well as what " +
                "members plan to attack or have alreadt attacked." +
                "To queue up for an attack, use the generated channels `#boss_1` to `#boss_5`." +
                "Admins can manually add or remove members using the commands `!addspot <@user> <position> <lap>, " +
                "`!removespot <@user> <position> <lap>`, and `!completespot <@user> <position> <lap>`." +
                "The schedule does not automatically update, and must be manually updated using `!nextboss`");

        result.add(title.build());
        for (int i = 0; i < numberOfLaps; i++) {
            EmbedBuilder lap = new EmbedBuilder();
            for (int j = 0; j < 5; j++) {
                BossEntity currentBoss = guildEntity.getBoss();
                StringBuilder titleString = new StringBuilder();
                BossEntity boss = bossService.getBossFromLapAndPosition(startingLap + i, j + 1);
                titleString.append(boss.getName());
                if (startingLap + i == guildEntity.getLap() && currentBoss.getPosition() == (j + 1)) {
                    titleString.append(" __Current boss__");
                }
                List<DBScheduleEntity.ScheduleUser> usersForBoss = scheduleService.getUsersForBoss(ctx.getGuildId(), startingLap + i, j);
                StringBuilder attacking = new StringBuilder();
                StringBuilder attacked = new StringBuilder();
                String attackingPrefix = "";
                String attackedPrefix = "";
                for (DBScheduleEntity.ScheduleUser user : usersForBoss) {
                    if (user.isHasAttacked()) {
                        attacked.append(attackedPrefix)
                                .append("~~")
                                .append(user.getUserNick())
                                .append("~~");
                        attackedPrefix = ", ";
                    } else {
                        attacking.append(attackingPrefix)
                                .append(user.getUserNick());
                        attackingPrefix = ", ";
                    }
                }
                StringBuilder fullBody = new StringBuilder();
                fullBody.append("Attacking\n")
                        .append(attacking.toString())
                        .append("\n")
                        .append("Attacked\n")
                        .append(attacked.toString());
                lap.addField(titleString.toString(), fullBody.toString(), false);
            }
            result.add(lap.build());
        }
        return result;
    }

    private void initBossChannels(ICommandContext ctx) {
        Category category = ctx.getGuild().getCategoriesByName(BotConstants.SCHEDULING_CATEGORY_NAME, false).get(0);
        List<TextChannel> channels = category.getTextChannels();
        for (int i = 1; i <= 5; i++) {
            channels.get(i).sendMessageEmbeds(createBossEmbed(ctx, i, 1)).queue();
            channels.get(i).sendMessageEmbeds(createBossEmbed(ctx, i, 2)).queue();
        }
    }

    @NotNull
    private MessageEmbed createBossEmbed(ICommandContext ctx, int bossPosition, int lap) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Lap: " + lap);
        List<DBScheduleEntity.ScheduleUser> users = scheduleService.getUsersForBoss(ctx.getGuildId(), lap, bossPosition);
        StringBuilder attackersSB = new StringBuilder();
        StringBuilder attackedSB = new StringBuilder();
        String attackingPrefix = "";
        String attackedPrefix = "";
        for (DBScheduleEntity.ScheduleUser user : users) {
            if (user.isHasAttacked()) {
                attackedSB.append(attackingPrefix).append(user.getUserNick());
                attackingPrefix = ", ";
            } else {
                attackersSB.append(attackedPrefix).append(user.getUserNick());
                attackedPrefix = ", ";
            }
        }
        eb.addField("Attacking", attackersSB.toString(), false);
        eb.addField("Attacked", attackedSB.toString(), false);
        return eb.build();
    }

    private void clearChannels(ICommandContext ctx) {
        Category category = ctx.getGuild().getCategoriesByName(BotConstants.SCHEDULING_CATEGORY_NAME, false).get(0);
        category.getTextChannels().forEach(channel ->
                channel.getHistory()
                        .retrievePast(100)
                        .queue(messages ->
                                messages.forEach(message ->
                                        message.delete().queue())));
    }

    private boolean channelsDoNotExist(ICommandContext ctx) {
        List<Category> categories = ctx.getGuild().getCategories();
        for (Category category : categories) {
            if (category.getName().equals(BotConstants.SCHEDULING_CATEGORY_NAME)) return false;
        }
        return true;
    }

    private void createChannels(ICommandContext ctx) {
        Category category = ctx.getGuild().createCategory(BotConstants.SCHEDULING_CATEGORY_NAME).complete();
        category.createTextChannel(BotConstants.SCHEDULING_CHANNEL_NAME).complete();
        category.createTextChannel("boss_1").complete();
        category.createTextChannel("boss_2").complete();
        category.createTextChannel("boss_3").complete();
        category.createTextChannel("boss_4").complete();
        category.createTextChannel("boss_5").complete();

        /* Removed for now because of race condition, kept since the complete() method could potentially make things annoying later on
        ctx.getGuild().createCategory(BotConstants.SCHEDULING_CATEGORY_NAME).queue(category -> {
            category.createTextChannel(BotConstants.SCHEDULING_CHANNEL_NAME).queue();
            category.createTextChannel("boss_1").queue();
            category.createTextChannel("boss_2").queue();
            category.createTextChannel("boss_3").queue();
            category.createTextChannel("boss_4").queue();
            category.createTextChannel("boss_5").queue();
        });
         */
    }

    @Override
    public void deleteSchedule(ICommandContext ctx) {
        if (channelsDoNotExist(ctx)) return;;
        Category category = ctx.getGuild().getCategoriesByName(BotConstants.SCHEDULING_CATEGORY_NAME, true).get(0);
        category.getTextChannels().forEach(channel -> channel.delete().queue());
        category.delete().queue();
    }

    @Override
    public void updateSchedule(JDA jda, String guildId, boolean bossDead) {

    }

    private DBScheduleEntity.ScheduleUser getUserFromName(JDA jda, String guildId, String effectiveName) {
        DBScheduleEntity.ScheduleUser result = new DBScheduleEntity.ScheduleUser();
        Member discordMember = jda.getGuildById(guildId).getMembersByEffectiveName(effectiveName, false).get(0);
        result.setUserId(discordMember.getId());
        result.setUserNick(discordMember.getNickname());
        return result;
    }

    @Override
    public void addAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberAlreadyExistsException, MemberHasAlreadyAttackedException {
        DBScheduleEntity.ScheduleUser user = getUserFromName(jda, guildId, name);
        user.setHasAttacked(false);
        scheduleService.addUserToBoss(guildId, lap, position, user);
    }

    @Override
    public void removeAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberIsNotAttackingException {
        DBScheduleEntity.ScheduleUser user = getUserFromName(jda, guildId, name);
        scheduleService.removeUserFromBoss(guildId, lap, position, user);
    }

    @Override
    public void markFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasAlreadyAttackedException, MemberIsNotAttackingException {
        List<DBScheduleEntity.ScheduleUser> usersForBoss = scheduleService.getUsersForBoss(guildId, lap, position);
        DBScheduleEntity.ScheduleUser newUser = getUserFromName(jda, guildId, name);
        Boolean hasAttacked = null; // Sneaky ternary :)
        for (DBScheduleEntity.ScheduleUser user : usersForBoss) {
            if (user.getUserId().equals(newUser.getUserId())) {
                hasAttacked = user.isHasAttacked();
            }
        }
        if (hasAttacked == null) throw new MemberIsNotAttackingException();
        if (hasAttacked) throw new MemberHasAlreadyAttackedException();
        scheduleService.toggleUserAttack(guildId, position, lap, newUser);
    }

    @Override
    public void unMarkFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasNotAttackedException {
        List<DBScheduleEntity.ScheduleUser> usersForBoss = scheduleService.getUsersForBoss(guildId, lap, position);
        DBScheduleEntity.ScheduleUser newUser = getUserFromName(jda, guildId, name);
        Boolean hasAttacked = null; // Sneaky ternary :)
        for (DBScheduleEntity.ScheduleUser user : usersForBoss) {
            if (user.getUserId().equals(newUser.getUserId())) {
                hasAttacked = user.isHasAttacked();
            }
        }
        if (hasAttacked == null) throw new MemberHasNotAttackedException();
        if (!hasAttacked) throw new MemberHasNotAttackedException();
        scheduleService.toggleUserAttack(guildId, position, lap, newUser);
    }

    /**
     * Checks if a given user is currently signed up for the current boss of the guild
     * @deprecated only used for old input based attack validation, this is no longer used and no method should ever call this
     *
     * @param jda     The discord JDA
     * @param guildId GuildID of the user
     * @param name    The effective name of the user
     * @return true if the user is signed up for the current boss and has not completed their attack
     */
    @Override
    @Deprecated
    public boolean isAttackingCurrentBoss(JDA jda, String guildId, String name) {
        return false;
    }
}
