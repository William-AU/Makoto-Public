package bot.commands.scheduling.strategies;

import bot.services.DatabaseScheduleService;

@Deprecated
public class DBExpectedAttackStrategy implements ExpectedAttackStrategy {
    private final DatabaseScheduleService scheduleService;


    public DBExpectedAttackStrategy(DatabaseScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Override
    public void setExpectedAttacks(String guildId, int pos, int expected) {
        scheduleService.setExpectedAttacks(guildId, pos, expected);
    }
}
