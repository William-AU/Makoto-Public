package bot.listeners;

import bot.commands.framework.ManualCommandContext;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.BotConstants;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrivateMessageListener extends ListenerAdapter {
    private final ScheduleStrategy scheduleStrategy;

    public PrivateMessageListener(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        if (!event.isFromType(ChannelType.PRIVATE)) return;
        System.out.println("Found private message");
        List<Message> history = event.getChannel().asPrivateChannel().getHistory().retrievePast(2).complete();
        Message previousMessage = history.get(1);
        System.out.println("Previous message: " + previousMessage);
        if (!previousMessage.getAuthor().isBot()) return;
        System.out.println("Previous message was from a bot");
        if (previousMessage.getEmbeds().size() != 1) return;
        System.out.println("Previous message had exactly one embed");
        MessageEmbed embed = previousMessage.getEmbeds().get(0);
        String id = embed.getFooter().getText();
        System.out.println("Found ID: " + id);
        switch (id) {
            case BotConstants.ASK_FOR_DAMAGE_ID -> handleAskForDamage(event, embed);
        }
    }

    private void handleAskForDamage(MessageReceivedEvent event, MessageEmbed previousEmbed) {
        System.out.println("Handling ask for damage");
        int damage;
        try {
            damage = Integer.parseInt(event.getMessage().getContentRaw());
        } catch (Exception e) {
            event.getChannel().asPrivateChannel().sendMessage("Unable to parse damage, please only reply with a number").queue();
            event.getChannel().asPrivateChannel().sendMessageEmbeds(previousEmbed).queue();
            return;
        }
        String title = previousEmbed.getTitle();
        String[] titleContent = title.split(": | ");
        int lap = Integer.parseInt(titleContent[1].trim());
        int pos = Integer.parseInt(titleContent[3].trim());
        String scheduleName = titleContent[5].trim();
        String guildID = titleContent[7].trim();
        String userID = event.getMessage().getAuthor().getId();
        Guild guild = event.getJDA().getGuildById(guildID);
        Member member = guild.getMemberById(userID);
        ManualCommandContext ctx = new ManualCommandContext(guild, guildID, event.getJDA());
        scheduleStrategy.updateDamage(ctx, scheduleName, member.getEffectiveName(), pos, lap, damage);
        event.getChannel().asPrivateChannel().sendMessage("Thank you").queue();
    }
}
