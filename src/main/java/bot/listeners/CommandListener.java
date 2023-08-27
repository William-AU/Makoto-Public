package bot.listeners;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.common.BotConstants;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandListener extends ListenerAdapter {
    private final Map<String, ICommand> commands;

    public CommandListener(Map<String, ICommand> commands) {
        this.commands = commands;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getAuthor();

        if (user.isBot() || event.isWebhookMessage()) return;
        if (event.isFromType(ChannelType.PRIVATE)) return;

        String[] raw = event.getMessage().getContentRaw().split(" ");
        String rawCommand = raw[0];

        boolean isCommand = rawCommand.startsWith(BotConstants.PREFIX);
        if (!isCommand) return;

        String strippedCommand = rawCommand.substring(BotConstants.PREFIX.length());

        boolean commandExists = commands.containsKey(strippedCommand);
        if (!commandExists) return;

        List<String> args = new ArrayList<>() {{
            for (int i = 1; i < raw.length; i++) {
                add(raw[i]);
            }
        }};

        ICommand command = commands.get(strippedCommand);
        CommandContext ctx = new CommandContext(event, args);
        command.handle(ctx);
    }
}
