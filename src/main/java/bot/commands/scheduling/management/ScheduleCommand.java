package bot.commands.scheduling.management;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.commands.tracking.StartCBCommand;
import bot.exceptions.schedule.ScheduleException;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ScheduleCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;
    private final StartCBCommand startCBCommand;
    private final GuildService guildService;

    @Autowired
    public ScheduleCommand(ScheduleStrategy scheduleStrategy, StartCBCommand startCBCommand, GuildService guildService) {
        this.scheduleStrategy = scheduleStrategy;
        this.startCBCommand = startCBCommand;
        this.guildService = guildService;
    }

    @Override
    public void handle(CommandContext ctx) {
        System.out.println("Schedule Command Found");
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        boolean hasGuild = guildService.getGuild(ctx.getGuildId()) != null;

        if (scheduleStrategy.hasActiveSchedule(ctx.getJDA(), ctx.getGuildId())) {
            ctx.getChannel().sendMessage("This server already has an active schedule, if you want to reset the schedule use `!resetSchedule` or `!deleteSchedule`, otherwise you can create a new schedule with `!newSchedule <name>` or rename a schedule with `!renameSchedule <oldName> <newName>`")
                    .queue();
            ctx.reactWIP();
            return;
        }
        try {
            System.out.println("Calling scheduleStrategy.createSchedule");
            System.out.println("REFLECTION: We are using the scheduleStrategy: " + scheduleStrategy.getClass().getName());
            scheduleStrategy.createSchedule(ctx);
            ctx.reactPositive();
        } catch (ScheduleException e) {
            ctx.sendError("Scheduling requires that the bot has permission to manage text channels in order to create the needed coordination channels");
        } catch (NullPointerException startCBNotInitialized) {
            startCBCommand.handle(ctx);
            try {
                scheduleStrategy.createSchedule(ctx);
            } catch (ScheduleException e) {
                System.out.println("Caught exception: " + e.getMessage());
                ctx.sendError("Error creating schedule, bot could not execute startCB command, please manually use `!startcb` before starting the schedule");
            }
        }
    }



    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("schedule");
    }
}
