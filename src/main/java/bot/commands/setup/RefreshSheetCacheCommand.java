package bot.commands.setup;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.CachedSpreadSheetUtils;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RefreshSheetCacheCommand implements ICommand {
    private final CachedSpreadSheetUtils cachedSpreadSheetUtils;

    @Autowired
    public RefreshSheetCacheCommand(CachedSpreadSheetUtils cachedSpreadSheetUtils) {
        this.cachedSpreadSheetUtils = cachedSpreadSheetUtils;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        cachedSpreadSheetUtils.refreshCache(ctx.getGuildId());
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("invalidate");
            add("refreshcache");
            add("refreshsheet");
        }};
    }
}
