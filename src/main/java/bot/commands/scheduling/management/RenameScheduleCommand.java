package bot.commands.scheduling.management;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RenameScheduleCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        ctx.sendMessageInChannel("Currently not implemented, use `!deleteSchedule <oldName>` and `!newSchedule <newName>` as a temporary alternative.");
        ctx.reactWIP();
    }

    /**
     * The list of identifiers used to call the command, this list must contain at least one element,
     * if the command is a slash command, the first identifier is used because of discord global command limits
     *
     * @return the non-empty list of identifiers for the command
     */
    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("renameschedule");
    }
}
