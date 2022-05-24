package bot.commands.scheduling.strategies;

public interface ExpectedAttackStrategy {
    void setExpectedAttacks(String guildId, int pos, int expected);
}
