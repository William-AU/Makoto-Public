package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.exceptions.MemberAlreadyExistsException;
import bot.exceptions.MemberHasAlreadyAttackedException;
import bot.exceptions.MemberIsNotAttackingException;
import bot.services.GuildService;
import bot.storage.models.GuildEntity;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CompleteSpotCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;
    private final GuildService guildService;

    @Autowired
    public CompleteSpotCommand(ScheduleStrategy scheduleStrategy, GuildService guildService) {
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
            scheduleStrategy.validateArguments(ctx, "completespot");
            name = scheduleStrategy.parseName(ctx);
            position = scheduleStrategy.parsePosition(ctx, "completespot");
            lap = scheduleStrategy.parseLap(ctx, "completespot", currentLap);
        } catch (Exception e) {
            ctx.sendError(e.getMessage());
            return;
        }

        try {
            if (lap == currentLap) {
                scheduleStrategy.markFinished(ctx.getJDA(), ctx.getGuildId(), position, name);
            }
            else {
                scheduleStrategy.markFinished(ctx.getJDA(), ctx.getGuildId(), position + 5, name);
            }
        } catch (MemberHasAlreadyAttackedException e) {
            ctx.sendError("Member has already attacked");
            return;
        } catch (MemberIsNotAttackingException e) {
            ctx.sendError("Member is not attacking");
            return;
        }
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("completespot");
    }
}
