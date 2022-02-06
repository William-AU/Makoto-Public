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
        int bossPosition;
        try {
            bossPosition = scheduleStrategy.parseBoss(ctx.getMessage().getContentRaw().split(" "));
        } catch (IllegalArgumentException e) {
            ctx.sendError(e.getMessage());
            return;
        }
        if (!scheduleStrategy.hasActiveSchedule(ctx.getGuildId(), bossPosition)) {
            ctx.sendError("There is no schedule for the specified boss");
            return;
        }
        scheduleStrategy.deleteSchedule(ctx, bossPosition);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("removeschedule");
    }
}
