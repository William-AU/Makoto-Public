package bot.commands.scheduling.strategies;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommandContext;
import bot.exceptions.*;
import bot.exceptions.schedule.ScheduleException;
import net.dv8tion.jda.api.JDA;

public interface ScheduleStrategy {
    /**
     * Extracts the boss position from a text input
     * @param content full message content
     * @return the boss position
     * @throws IllegalArgumentException if it goes wrong :)
     */
    default int parseBoss(String[] content) throws IllegalArgumentException {
        String command = content[0];
        if (content.length != 2) throw new IllegalArgumentException("Incorrect number of arguments, please use `" + command + " <Boss position>`");
        try {
            int result = Integer.parseInt(content[1]);
            if (result < 1 || result > 5) throw new IllegalArgumentException("Boss position must be between 1-5");
            return result;
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Boss position must be an integer");
        }
    };

    /**
     * Check if a guild already has a schedule
     * @param guildId String ID of the guild
     * @return true if a schedule exists, false otherwise
     */
    boolean hasActiveSchedule(JDA jda, String guildId);

    default boolean hasActiveSchedule(JDA jda, String guildId, String name) {
        return hasActiveSchedule(jda, guildId);
    }

    /**
     * Creates a new schedule, this will remove any previous schedule WITHOUT WARNING
     * @param ctx The context of the command call
     */
    void createSchedule(ICommandContext ctx) throws ScheduleException;

    default void createSchedule(ICommandContext ctx, String name) throws ScheduleException {
        createSchedule(ctx);
    }

    void resetSchedule(ICommandContext ctx);

    default void resetSchedule(ICommandContext ctx, String name) {
        resetSchedule(ctx);
    };

    void deleteSchedule(ICommandContext ctx);

    default void deleteSchedule(ICommandContext ctx, String name) {
        deleteSchedule(ctx);
    }

    void updateSchedule(JDA jda, String guildId, boolean bossDead);

    default void updateSchedule(JDA jda, String guildId, boolean bossDead, String name) {
        updateSchedule(jda, guildId, bossDead);
    }

    void addAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberAlreadyExistsException, MemberHasAlreadyAttackedException;
    default void addAttacker(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName, boolean isOvertime) throws MemberAlreadyExistsException, MemberHasAlreadyAttackedException {
        addAttacker(jda, guildId, position, lap, name);
    };

    void removeAttacker(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberIsNotAttackingException;
    default void removeAttacker(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName) throws MemberIsNotAttackingException {
        removeAttacker(jda, guildId, position, lap, name);
    };

    void markFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasAlreadyAttackedException, MemberIsNotAttackingException;
    default void markFinished(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName) throws MemberHasAlreadyAttackedException, MemberIsNotAttackingException {
        markFinished(jda, guildId, position, lap, name);
    };

    void unMarkFinished(JDA jda, String guildId, Integer position, Integer lap, String name) throws MemberHasNotAttackedException;
    default void unMarkFinished(JDA jda, String guildId, Integer position, Integer lap, String name, String scheduleName) throws MemberHasNotAttackedException {
        unMarkFinished(jda, guildId, position, lap, name);
    };

    /**
     * Validates the arguments in a generic schedule command
     * @param ctx command context (this includes the content)
     * @param command the command to validate
     * @throws Exception generic error
     */
    default void validateArguments(CommandContext ctx, String command) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        if (content.length != 4) {
            throw new Exception("Incorrect arguments, please use `!" + command + " <@user> <position> <lap>`");
        }
    };

    /**
     * Ensures a name is a valid mention
     * @param ctx command context
     * @return The effective name of a member   All methods should manually find the nickname if needed!
     * @throws Exception Throws exception if name is not a mention, or member is not found
     */
    default String parseName(CommandContext ctx) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        if (!content[1].contains("<@")) {
            throw new Exception("User must be a mention");
        }
        try {
            String id = content[1].substring(2, content[1].length() - 1);
            Long.parseLong(id);
            return ctx.getGuild().getMemberById(id).getEffectiveName();
        } catch (NumberFormatException e) {
            throw new Exception("User must be a mention");
        }
    };

    default int parsePosition(CommandContext ctx, String command) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        int position;
        try {
            position = Integer.parseInt(content[2]);
        } catch (NumberFormatException e) {
            throw new Exception("Position and Lap must be numbers, please use `!" + command + " <@user> <position> <lap>`");
        }
        if (position < 1 || position > 5) {
            throw new Exception("The position must be between 1 and 5");
        }
        return position;
    }

    default int parseLap(CommandContext ctx, String command, int currentLap) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        int lap;
        try {
            lap = Integer.parseInt(content[3]);
        } catch (NumberFormatException e) {
            throw new Exception("Position and Lap must be numbers, please use `!" + command + " <@user> <position> <lap>`");
        }
        /* DEPRECATED
        if (lap != currentLap && lap != (currentLap + 1)) {
            throw new Exception("The lap must be either the current lap or the next lap, scheduling further than the next lap is not currently supported");
        }
         */
        return lap;
    }
    /**
     * Checks if a given user is currently signed up for the current boss of the guild
     * @param jda The discord JDA
     * @param guildId GuildID of the user
     * @param user The display name of the user
     * @return true if the user is signed up for the current boss and has not completed their attack
     */
    boolean isAttackingCurrentBoss(JDA jda, String guildId, String user);

    default boolean isAttackingCurrentBoss(JDA jda, String guildId, String user, String scheduleName) {
        return isAttackingCurrentBoss(jda, guildId, user);
    }

    void setNextBoss(CommandContext ctx);

    default void setExpectedAttacks(CommandContext ctx, int pos, int expected) {

    };

    default void setExpectedAttacks(CommandContext ctx, int pos, int expected, String scheduleName) {

    };

    default boolean hasMoreThanOneSchedule(CommandContext ctx) {
        return false;
    }

    default void setNextBoss(CommandContext ctx, String scheduleName) {
        setNextBoss(ctx);
    }

    default void updateDamage(ICommandContext ctx, String scheduleName, String effectiveName, int pos, int lap, int expectedDamage) {}
}
