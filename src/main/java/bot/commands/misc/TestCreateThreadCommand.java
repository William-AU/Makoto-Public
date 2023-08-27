package bot.commands.misc;


import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.PermissionsUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TestCreateThreadCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        ctx.getTextChannel().createThreadChannel("Test").queue();

    }

    /**
     * The list of identifiers used to call the command, this list must contain at least one element,
     * if the command is a slash command, the first identifier is used because of discord global command limits
     *
     * @return the non-empty list of identifiers for the command
     */
    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("testthread");
    }
}
