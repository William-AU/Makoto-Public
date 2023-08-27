package bot.commands.configCommands;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetUpdateChannelCommand implements ICommand {
    private final GuildService guildService;

    @Autowired
    public SetUpdateChannelCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }

        guildService.setUpdatesChannelId(ctx.getGuildId(), ctx.getChannel().getId());
        ctx.sendMessageInChannel("This channel will now be used for bot notifications in case DM's are closed");
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
            add("updatechannel");
            add("setupdatechannel");
            add("setchannel");
        }};
    }
}
