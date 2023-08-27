package bot.commands.tracking;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResetCBCommand implements ICommand {
    @Autowired
    private TrackingStrategy trackingStrategy;

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        trackingStrategy.startTracking(ctx);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("resetcb");
            add("restartcb");
        }};
    }
}
