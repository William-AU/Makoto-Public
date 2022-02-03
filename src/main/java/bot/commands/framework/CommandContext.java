package bot.commands.framework;

import bot.common.BotConstants;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandContext implements ICommandContext{
    private final MessageReceivedEvent event;
    private final List<String> args;

    public CommandContext(MessageReceivedEvent event, List<String> args) {
        this.event = event;
        this.args = args;
    }

    /**
     * Returns the {@link MessageReceivedEvent message event} that was received for this instance
     *
     * @return the {@link MessageReceivedEvent message event} that was received for this instance
     */
    @Override
    public MessageReceivedEvent getEvent() {
        return event;
    }

    public List<String> getArgs() {
        return args;
    }

    public void sendMessageInChannel(String message) {
        getChannel().sendMessage(message).queue();
    }

    public void reactPositive() {
        getMessage().addReaction(BotConstants.REACT_POSITIVE).queue();
    }

    public void reactNegative() {
        getMessage().addReaction(BotConstants.REACT_NEGATIVE).queue();
    }

    public void reactWIP() {
        getMessage().addReaction(BotConstants.REACT_WIP).queue();
    }
}