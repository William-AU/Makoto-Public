package bot.commands.framework;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

public interface ICommandContext {
    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild} for the current command/event
     *
     * @return the {@link net.dv8tion.jda.api.entities.Guild} for this command/event
     */
    default Guild getGuild() {
        return this.getEvent().getGuild();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent message event} that was received for this instance
     *
     * @return the {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent message event} that was received for this instance
     */
    MessageReceivedEvent getEvent();

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.TextChannel channel} that the message for this event was send in
     *
     * @return the {@link net.dv8tion.jda.api.entities.TextChannel channel} that the message for this event was send in
     */
    default MessageChannel getChannel() {
        return this.getEvent().getChannel();
    }

    default String getAuthorID() {
        return getAuthor().getId();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Message message} that triggered this event
     *
     * @return the {@link net.dv8tion.jda.api.entities.Message message} that triggered this event
     */
    default Message getMessage() {
        return this.getEvent().getMessage();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.User author} of the message as user
     *
     * @return the {@link net.dv8tion.jda.api.entities.User author} of the message as user
     */
    default User getAuthor() {
        return this.getEvent().getAuthor();
    }
    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Member author} of the message as member
     *
     * @return the {@link net.dv8tion.jda.api.entities.Member author} of the message as member
     */
    default Member getMember() {
        return this.getEvent().getMember();
    }

    /**
     * Returns the current {@link net.dv8tion.jda.api.JDA jda} instance
     *
     * @return the current {@link net.dv8tion.jda.api.JDA jda} instance
     */
    default JDA getJDA() {
        return this.getEvent().getJDA();
    }

    /**
     * Returns the current {@link net.dv8tion.jda.api.sharding.ShardManager} instance
     *
     * @return the current {@link net.dv8tion.jda.api.sharding.ShardManager} instance
     */
    default ShardManager getShardManager() {
        return this.getJDA().getShardManager();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.User user} for the currently logged in account
     *
     * @return the {@link net.dv8tion.jda.api.entities.User user} for the currently logged in account
     */
    default User getSelfUser() {
        return this.getJDA().getSelfUser();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Member member} in the guild for the currently logged in account
     *
     * @return the {@link net.dv8tion.jda.api.entities.Member member} in the guild for the currently logged in account
     */
    default Member getSelfMember() {
        return this.getGuild().getSelfMember();
    }

    default String getGuildId() {
        return getGuild().getId();
    }

}