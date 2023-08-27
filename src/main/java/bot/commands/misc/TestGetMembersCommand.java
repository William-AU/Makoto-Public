package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TestGetMembersCommand implements ICommand {
    @Autowired
    private SheetService sheetService;
    @Autowired
    private GuildService guildService;

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            return;
        }
        String id = guildService.getSpreadSheetId(ctx.getGuildId());
        System.out.println(sheetService.getUserIDsFromSheet(id));
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("printmembers");
    }
}
