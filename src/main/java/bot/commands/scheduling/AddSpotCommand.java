package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.exceptions.MemberAlreadyExistsException;
import bot.services.GuildService;
import bot.storage.models.GuildEntity;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AddSpotCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;
    private final GuildService guildService;

    @Autowired
    public AddSpotCommand(ScheduleStrategy scheduleStrategy, GuildService guildService) {
        this.scheduleStrategy = scheduleStrategy;
        this.guildService = guildService;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }

        GuildEntity guild = guildService.getGuild(ctx.getGuildId());
        int currentLap = guild.getLap();
        int position;
        int lap;
        String name;
        try {
            scheduleStrategy.validateArguments(ctx, "addspot");
            name = scheduleStrategy.parseName(ctx);
            position = scheduleStrategy.parsePosition(ctx, "addspot");
            lap = scheduleStrategy.parseLap(ctx, "addspot", currentLap);
        } catch (Exception e) {
            ctx.sendError(e.getMessage());
            return;
        }

        try {
            if (lap == currentLap) {
                scheduleStrategy.addAttacker(ctx.getJDA(), ctx.getGuildId(), position, name);
            }
            else {
                scheduleStrategy.addAttacker(ctx.getJDA(), ctx.getGuildId(), position + 5, name);
            }
        } catch (MemberAlreadyExistsException e) {
            ctx.sendError("This member is already attacking on this schedule");
            return;
        }
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("addspot");
    }
}
