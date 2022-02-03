package bot.commands.framework;

import java.util.List;

public interface ICommand {
    void handle(CommandContext ctx);

    List<String> getIdentifiers();
}
