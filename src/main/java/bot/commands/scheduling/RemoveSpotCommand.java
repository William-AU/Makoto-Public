package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.exceptions.MemberIsNotAttackingException;
import bot.services.GuildService;
import bot.storage.models.GuildEntity;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RemoveSpotCommand implements ICommand {
    private final GuildService guildService;
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public RemoveSpotCommand(GuildService guildService, ScheduleStrategy scheduleStrategy) {
        this.guildService = guildService;
        this.scheduleStrategy = scheduleStrategy;
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
            scheduleStrategy.validateArguments(ctx, "removespot");
            name = scheduleStrategy.parseName(ctx);
            position = scheduleStrategy.parsePosition(ctx, "removespot");
            lap = scheduleStrategy.parseLap(ctx, "removespot", currentLap);
        } catch (Exception e) {
            ctx.sendError(e.getMessage());
            return;
        }

        try {
            if (lap == currentLap) {
                scheduleStrategy.removeAttacker(ctx.getJDA(), ctx.getGuildId(), position, name);
            }
            else {
                scheduleStrategy.removeAttacker(ctx.getJDA(), ctx.getGuildId(), position + 5, name);
            }
        } catch (MemberIsNotAttackingException e) {
            ctx.sendError("User is not registered to this schedule");
            return;
        }
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("removespot");
    }
}
