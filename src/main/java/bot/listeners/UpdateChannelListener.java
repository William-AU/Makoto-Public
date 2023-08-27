package bot.listeners;

import bot.commands.framework.ManualCommandContext;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.BotConstants;
import bot.services.GuildService;
import bot.services.ScheduleService;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class UpdateChannelListener extends ListenerAdapter {
    private final ScheduleStrategy scheduleStrategy;
    private final ScheduleService scheduleService;
    private final GuildService guildService;

    public UpdateChannelListener(ScheduleStrategy scheduleStrategy, ScheduleService scheduleService, GuildService guildService) {
        this.scheduleStrategy = scheduleStrategy;
        this.scheduleService = scheduleService;
        this.guildService = guildService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        if (event.isFromThread()) {
            String expectedChannelID = guildService.getUpdatesChannelId(event.getGuild().getId());
            if (expectedChannelID == null) return;
            if (!event.getChannel().getId().equals(expectedChannelID) && !event.isFromThread()) return;
            boolean shouldListenToThreads = guildService.getUseThreads(event.getGuild().getId());
            if (!shouldListenToThreads) return;
            boolean foundThread = false;
            TextChannel expectedChannel = event.getGuild().getTextChannelById(expectedChannelID);
            if (expectedChannel == null) {
                System.out.println("FATAL: EXPECTED CHANNEL ID IS SET BUT THE CHANNEL COULD NOT BE FOUND");
                return;
            }
            for (ThreadChannel tc : expectedChannel.getThreadChannels()) {
                if (tc.getId().equals(event.getChannel().asThreadChannel().getId())) {
                    foundThread = true;
                }
            }
            if (!foundThread) return;
        }
        try {
            Integer.parseInt(event.getMessage().getContentRaw());
        } catch (Exception e) {
            return;
        }
        // We look at up to the last 10 messages
        // When we are dealing with either threads or channels, we want to find 2 messages
        // The embed message and the mention method,
        System.out.println("GETTING HISTORY updateChannelListener");
        //System.out.println("CAUSED BY MESSAGE: " + event.getMessage().getContentRaw() + " in server: " + event.getGuild().getName());
        List<Message> history = event.getChannel().getHistory().retrievePast(10).complete();
        for (int i = 0; i < history.size(); i++) {
            Message message = history.get(i);
            Mentions mentions = message.getMentions();
            if (mentions.getUsers().size() != 1) {
                //System.out.println("Message did not have mention");
                continue;
            }
            String mention = mentions.getUsers().get(0).getAsMention();            // format: <@9999>
            String id = mention.substring(2, mention.length() - 1);     // format: 9999
            if (!id.equals(event.getMessage().getAuthor().getId())) {
                //System.out.println("id (" + id + ") did not match author:  " + event.getMessage().getAuthor().getId());
                continue;
            }
            // When we find a mention, we need to find the nearest embed (ideally this is always at most 1 above, but
            // since people can accidentally add messages in between we give it a little leeway)
            MessageEmbed nearestEmbed = findNearestEmbed(history, i, 4);
            if (nearestEmbed == null || nearestEmbed.getFooter() == null) return;
            switch (nearestEmbed.getFooter().getText()) {
                case BotConstants
                        .ASK_FOR_DAMAGE_ID -> handleAskForDamage(event, nearestEmbed);
            }
            return;
        }
    }

    private void handleAskForDamage(MessageReceivedEvent event, MessageEmbed previousEmbed) {
        System.out.println("HandleAskForDamage");
        int damage;
        try {
            damage = Integer.parseInt(event.getMessage().getContentRaw());
        } catch (Exception e) {
            event.getChannel().sendMessage("Unable to parse damage, please only reply witha  number").queue();
            event.getChannel().sendMessageEmbeds(previousEmbed).queue();
            event.getChannel().sendMessage("<@" + event.getAuthor().getId() + ">").queue();
            return;
        }
        // FIXME: Code duplication
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
        event.getChannel().sendMessage("Thank you").queue();
    }

    private MessageEmbed findNearestEmbed(List<Message> history, int startIndex, int maxTries) {
        for (int i = startIndex + 1; i <= startIndex + maxTries; i++) {
            if (i > history.size() - 1) {
                return null;
            }
            Message message = history.get(i);
            if (message.getEmbeds().size() == 1) {
                return message.getEmbeds().get(0);
            }
        }
        return null;
    }
}
