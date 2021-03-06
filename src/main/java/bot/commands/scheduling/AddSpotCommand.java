package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.exceptions.MemberAlreadyExistsException;
import bot.exceptions.MemberHasAlreadyAttackedException;
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
            scheduleStrategy.addAttacker(ctx.getJDA(), ctx.getGuildId(), position, lap, name);
        } catch (MemberAlreadyExistsException e) {
            ctx.sendError("This member is already attacking on this schedule");
            return;
        } catch (MemberHasAlreadyAttackedException e) {
            ctx.sendError("This member has already attacked, please first unmark the attack");
        }
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("addspot");
    }
}
