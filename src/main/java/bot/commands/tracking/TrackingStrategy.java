package bot.commands.tracking;

import bot.commands.framework.CommandContext;
import bot.services.BossService;
import bot.services.GuildService;
import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrackingStrategy {
    private final GuildService guildService;
    private final BossService bossService;

    @Autowired
    public TrackingStrategy(GuildService guildService, BossService bossService) {
        this.guildService = guildService;
        this.bossService = bossService;
    }

    public void startTracking(CommandContext ctx) {
        if (guildService.hasActiveBos(ctx.getGuildId())) {
            deleteExistingMessage(ctx);
        }
        bossService.initNewBoss(ctx.getGuildId());
        postMessage(ctx.getGuildId(), ctx.getChannel());
    }

    private void deleteExistingMessage(CommandContext ctx) {
        GuildEntity guild = guildService.getGuild(ctx.getGuildId());
        String channelId = guild.getBossChannelId();
        String messageId = guild.getBossMessageId();
        ctx.getJDA().getGuildById(ctx.getGuildId()).getTextChannelById(channelId).deleteMessageById(messageId).queue();

    }

    public void updateData(JDA jda, String guildId, boolean bossDead) {
        GuildEntity guild = guildService.getGuild(guildId);
        String channelId = guild.getBossChannelId();
        String messageId = guild.getBossMessageId();

        MessageChannel channel = jda.getGuildById(guildId).getTextChannelById(channelId);
        channel.editMessageEmbedsById(messageId, createEmbed(guildId, channel)).queue();
    }

    public void updateTracking(CommandContext ctx) {
        deleteExistingMessage(ctx);
        postMessage(ctx.getGuildId(), ctx.getChannel());
    }

    private MessageEmbed createEmbed(String guildId, MessageChannel channel) {
        GuildEntity guild = guildService.getGuild(guildId);
        BossEntity boss = guild.getBoss();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("CB Tracking Data");
        eb.addField("Current boss", boss.getName(), true);
        eb.addField("Boss health", guild.getCurrentHealth() + " / " + boss.getTotalHealth(), true);
        eb.addField("Stage", boss.getStage() + "", true);
        eb.setFooter("This data is only accurate if everyone inputs their damage on time", null);
        return eb.build();
    }

    private void postMessage(String guildId, MessageChannel channel) {
        channel.sendMessageEmbeds(createEmbed(guildId, channel)).queue(message -> {
            guildService.setBossTrackerChannel(guildId, channel.getId());
            guildService.setBossTrackerMessage(guildId, message.getId());
        });
    }
}
