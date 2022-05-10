package bot.commands.scheduling.strategies;

import bot.commands.framework.ICommandContext;
import bot.common.BotConstants;
import bot.exceptions.*;
import bot.exceptions.schedule.ScheduleException;
import bot.services.BossService;
import bot.services.DatabaseScheduleService;
import bot.services.GuildService;
import bot.storage.models.DBScheduleEntity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;

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

        if (!channelsExist(ctx)) {
            createChannels(ctx);
        }
    }

    private boolean channelsExist(ICommandContext ctx) {
        List<Category> categories = ctx.getGuild().getCategories();
        for (Category category : categories) {
            if (category.getName().equals(BotConstants.SCHEDULING_CATEGORY_NAME)) return true;
        }
        return false;
    }

    private void createChannels(ICommandContext ctx) {
        ctx.getGuild().createCategory(BotConstants.SCHEDULING_CATEGORY_NAME).queue(category -> {
            category.createTextChannel(BotConstants.SCHEDULING_CHANNEL_NAME).queue();
            category.createTextChannel("boss_1").queue();
            category.createTextChannel("boss_2").queue();
            category.createTextChannel("boss_3").queue();
            category.createTextChannel("boss_4").queue();
            category.createTextChannel("boss_5").queue();
        });
    }

    @Override
    public void deleteSchedule(ICommandContext ctx) {
        if (!channelsExist(ctx)) return;;
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
