package bot.commands.scheduling.management;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.exceptions.schedule.ScheduleException;
import bot.services.GuildService;
import bot.services.ScheduleService;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class NewScheduleCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;
    private final ScheduleService scheduleService;
    private final GuildService guildService;

    @Autowired
    public NewScheduleCommand(ScheduleStrategy scheduleStrategy, ScheduleService scheduleService, GuildService guildService) {
        this.scheduleStrategy = scheduleStrategy;
        this.scheduleService = scheduleService;
        this.guildService = guildService;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        List<String> knownSchedules = scheduleService.getScheduleNamesForGuild(ctx.getGuildId());
        Message message = ctx.getMessage();
        String content = message.getContentRaw();
        String scheduleName = "";

        String updateChannelId = guildService.getUpdatesChannelId(ctx.getGuildId());
        boolean askForDamage = guildService.getAskForDamage(ctx.getGuildId());

        if (askForDamage && updateChannelId == null) {
            ctx.sendError("The scheduling now supports asking users for damage, in order for this to work you must first set a channel for the bot to freely write updates in," +
                    "use `!setUpdateChannel` in the channel you wish to use, if you instead do not want this feature, you can still use the schedule by first using the command" +
                    "`!askForDamage false`");
            return;
        }

        try {
            scheduleName = content.split(" ")[1];
        } catch (Exception e) {
            ctx.sendError("Incorrect format, please use `!newSchedule <name>`");
            return;
        }
        if (knownSchedules.contains(scheduleName)) {
            ctx.sendError("A schedule with this name already exists");
            return;
        }
        try {
            scheduleStrategy.createSchedule(ctx, scheduleName);
        } catch (ScheduleException e) {
            ctx.sendError("Error creating schedule, make sure the bot has sufficient permissions");
        }
    }

    /**
     * The list of identifiers used to call the command, this list must contain at least one element,
     * if the command is a slash command, the first identifier is used because of discord global command limits
     *
     * @return the non-empty list of identifiers for the command
     */
    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("newschedule");
    }
}
