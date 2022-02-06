package bot.commands.tracking;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.BossService;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class StartCBCommand implements ICommand {
    @Autowired
    private GuildService guildService;

    @Autowired
    private BossService bossService;

    @Autowired
    private TrackingStrategy trackingStrategy;


    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        if (guildService.hasActiveBos(ctx.getGuildId())) {
            ctx.sendError("A CB is already in progress, if you wish to update the tracking message location, use `!updatetracking`, " +
                    "if you wish to start a new CB, instead use `!resetcb`");
            return;
        }
        trackingStrategy.startTracking(ctx);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("startcb");
    }
}
