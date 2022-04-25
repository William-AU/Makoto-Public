package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.tracking.StartCBCommand;
import bot.exceptions.ScheduleException;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ScheduleCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;
    private final StartCBCommand startCBCommand;

    @Autowired
    public ScheduleCommand(ScheduleStrategy scheduleStrategy, StartCBCommand startCBCommand) {
        this.scheduleStrategy = scheduleStrategy;
        this.startCBCommand = startCBCommand;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        if (scheduleStrategy.hasActiveSchedule(ctx.getGuildId())) {
            ctx.sendError("A schedule is already tracking this boss, use `!resetschedule`");
            return;
        }
        try {
            scheduleStrategy.createSchedule(ctx);
            ctx.reactPositive();
        } catch (ScheduleException e) {
            ctx.sendError("Scheduling requires that the bot has permission to manage text channels in order to create the needed coordination channels");
        } catch (NullPointerException startCBNotInitialized) {
            startCBCommand.handle(ctx);
            try {
                scheduleStrategy.createSchedule(ctx);
            } catch (ScheduleException e) {
                ctx.sendError("Error creating schedule, bot could not execute startCB command, please manually use `!startcb` before starting the schedule");
            }
        }
    }



    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("schedule");
    }
}
