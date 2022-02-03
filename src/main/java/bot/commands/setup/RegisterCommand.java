package bot.commands.setup;

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
public class RegisterCommand implements ICommand {
    @Autowired
    private GuildService guildService;

    @Autowired
    private SheetService sheetService;

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.reactNegative();
            return;
        }
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        if (content.length != 2) {
            ctx.reactNegative();
            return;
        }
        try {
            guildService.addSpreadsheetId(ctx.getGuild().getId(), extractId(content[1]));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            ctx.reactNegative();
            return;
        }
        ctx.reactPositive();
    }

    private String extractId(String url) {
        // Haven't tested, taken directly from old version
        return url.split("/")[5];
    }


    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("register");
    }
}
