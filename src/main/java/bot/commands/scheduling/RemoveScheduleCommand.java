package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RemoveScheduleCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public RemoveScheduleCommand(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        if (!scheduleStrategy.hasActiveSchedule(ctx.getGuildId())) {
            ctx.sendError("There is no schedule for the specified boss");
            return;
        }
        scheduleStrategy.deleteSchedule(ctx);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("removeschedule");
    }
}
