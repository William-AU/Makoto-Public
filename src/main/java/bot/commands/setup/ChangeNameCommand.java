package bot.commands.setup;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ChangeNameCommand implements ICommand {
    @Autowired
    private GuildService guildService;

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.reactNegative();
            return;
        }
        String[] split = ctx.getMessage().getContentRaw().split(" ");
        if (split.length != 3) {
            ctx.sendMessageInChannel("Invalid input use `!changeClanName <oldName> <newName>`");
            ctx.reactNegative();
            return;
        }
        String oldName = split[1];
        String newName = split[2];
        guildService.setClanName(ctx.getGuildId(), oldName, newName);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("changeclanname");
    }
}
