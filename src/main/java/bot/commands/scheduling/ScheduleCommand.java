package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ScheduleCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public ScheduleCommand(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        if (scheduleStrategy.hasActiveSchedule(ctx.getGuildId())) {
            ctx.sendError("A schedule is already tracking this boss, use `!resetschedule <Boss position>` or `!removeschedule <Boss position>`");
            return;
        }
        scheduleStrategy.createSchedule(ctx);
        ctx.reactPositive();
    }



    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("schedule");
    }
}
