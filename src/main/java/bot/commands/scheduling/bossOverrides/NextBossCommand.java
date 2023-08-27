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
        /*
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            System.out.println("Permissions check failed");
            return;
        }

         */
        if (scheduleStrategy.hasMoreThanOneSchedule(ctx)) {
            System.out.println("DEBUG HAS MORE THAN ONE SCHEDULE");
            String[] content = ctx.getMessage().getContentRaw().split(" ");
            if (content.length != 2) {
                ctx.sendError("This server has more than one schedule, please use `!nextboss <name>` instead");
                return;
            }
            String name = content[1];
            scheduleStrategy.setNextBoss(ctx, name);
            ctx.reactPositive();
            return;
        }
        System.out.println("DEBUG DOES NOT HAVE MORE THAN ONE SCHEDULE");
        scheduleStrategy.setNextBoss(ctx);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("nextboss");
    }
}
