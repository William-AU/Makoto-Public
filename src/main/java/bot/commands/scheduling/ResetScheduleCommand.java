package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResetScheduleCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public ResetScheduleCommand(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }


    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        scheduleStrategy.createSchedule(ctx);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("resetschedule");
            add("restartschedule");
        }};
    }
}
