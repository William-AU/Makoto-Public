package bot.commands.scheduling.strategies;

import bot.services.DatabaseScheduleService;

public class DBExpectedAttackStrategy implements ExpectedAttackStrategy {
    private final DatabaseScheduleService scheduleService;


    public DBExpectedAttackStrategy(DatabaseScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Override
    public void setExpectedAttacks(String guildId, int pos, int lap, int expected) {
        scheduleService.setExpectedAttacks(guildId, pos, lap, expected);
    }
}
