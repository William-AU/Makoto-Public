package bot.tracking;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UpdateTrackingCommand implements ICommand {
    @Autowired
    private TrackingStrategy trackingStrategy;

    @Autowired
    private GuildService guildService;

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        if (!guildService.hasActiveBos(ctx.getGuildId())) {
            ctx.sendError("A CB has not yet been started, use `!startcb` to start tracking a new CB");
            return;
        }
        trackingStrategy.updateTracking(ctx);
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("updatetracking");
    }
}
