package bot.listeners;

import bot.commands.framework.ICommand;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;

public class SlashCommandListener extends ListenerAdapter {
    private final Map<String, ICommand> commands;

    public SlashCommandListener(Map<String, ICommand> commands) {
        this.commands = commands;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
    }
}
