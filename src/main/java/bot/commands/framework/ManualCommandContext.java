package bot.commands.framework;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Increadibly unreasonably dangerous class that will destroy everything if not handled very carefully
 * please don't use unless you know exactly what you are doing,
 * there are ALWAYS better ways to do something than use this class
 */
public class ManualCommandContext implements ICommandContext{
    /**
     * Returns the {@link MessageReceivedEvent message event} that was received for this instance
     *
     * @return the {@link MessageReceivedEvent message event} that was received for this instance
     */
    @Override
    public MessageReceivedEvent getEvent() {
        return null;
    }
    private final Guild guild;
    private final String guildId;
    private final JDA jda;

    public ManualCommandContext(Guild guild, String guildId, JDA jda) {
        this.guild = guild;
        this.guildId = guildId;
        this.jda = jda;
    }

    /**
     * Returns the {@link Guild} for the current command/event
     *
     * @return the {@link Guild} for this command/event
     */
    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public String getGuildId() {
        return guildId;
    }

    /**
     * Returns the current {@link JDA jda} instance
     *
     * @return the current {@link JDA jda} instance
     */
    @Override
    public JDA getJDA() {
        return jda;
    }
}
