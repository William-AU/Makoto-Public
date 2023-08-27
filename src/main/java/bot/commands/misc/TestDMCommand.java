package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TestDMCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) return;
        User user = ctx.getAuthor();
        boolean hasOpenChannel = user.hasPrivateChannel();
        System.out.println("Open channel? " + hasOpenChannel);
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage("This is a test DM"))
                .onErrorFlatMap(throwable -> {
                    // Some return value here?
                    return ctx.getChannel().sendMessage("DM failed :(");
                })
                .queue();
    }

    /**
     * The list of identifiers used to call the command, this list must contain at least one element,
     * if the command is a slash command, the first identifier is used because of discord global command limits
     *
     * @return the non-empty list of identifiers for the command
     */
    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("dm");
    }
}
