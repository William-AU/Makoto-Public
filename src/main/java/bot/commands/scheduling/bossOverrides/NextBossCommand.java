package bot.commands.scheduling.bossOverrides;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.CBUtils;
import bot.utils.PermissionsUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class NextBossCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;

    public NextBossCommand(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }


    @Override
    public void handle(CommandContext ctx) {
        System.out.println("In next boss");
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            System.out.println("Permissions check failed");
            return;
        }
        scheduleStrategy.setNextBoss(ctx);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("nextboss");
    }
}
