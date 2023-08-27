package bot.commands.framework;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public interface ICommand {
    void handle(CommandContext ctx);

    /**
     * The list of identifiers used to call the command, this list must contain at least one element,
     * if the command is a slash command, the first identifier is used because of discord global command limits
     * @return the non-empty list of identifiers for the command
     */
    List<String> getIdentifiers();

    default String getDescription() {
        return null;
    }

    default boolean isSlashCommand() {
        return false;
    }

    default boolean isTextCommand() {
        return true;
    }

    default SlashCommandData getSlashCommandData() {
        return null;
    }
}
