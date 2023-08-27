package bot.commands.configCommands;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SetAskForDamageCommand implements ICommand {
    private final GuildService guildService;

    @Autowired
    public SetAskForDamageCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }

        String[] content = ctx.getMessage().getContentRaw().split(" ");
        if (content.length != 2) {
            ctx.sendError("Incorrect syntax, please use `!askForDamage <true/false>`");
            return;
        }
        boolean flag;
        try {
            flag = Boolean.parseBoolean(content[1].toLowerCase());
        } catch (Exception e) {
            ctx.sendError("Incorrect syntax, please use `!askForDamage <true/false>`");
            return;
        }

        guildService.setAskForDamage(ctx.getGuildId(), flag);
        ctx.reactPositive();
    }

    /**
     * The list of identifiers used to call the command, this list must contain at least one element,
     * if the command is a slash command, the first identifier is used because of discord global command limits
     *
     * @return the non-empty list of identifiers for the command
     */
    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("askfordamage");
        }};
    }
}
