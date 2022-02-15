package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.exceptions.ScheduleDoesNotExistException;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.ScheduleService;
import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.models.ScheduleEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleStrategy {
    private final ScheduleService scheduleService;
    private final GuildService guildService;
    private final BossService bossService;

    @Autowired
    public ScheduleStrategy(ScheduleService scheduleService, GuildService guildService, BossService bossService) {
        this.scheduleService = scheduleService;
        this.guildService = guildService;
        this.bossService = bossService;
    }

    public int parseBoss(String[] content) throws IllegalArgumentException {
        String command = content[0];
        if (content.length != 2) throw new IllegalArgumentException("Incorrect number of arguments, please use `" + command + " <Boss position>`");
        try {
            int result = Integer.parseInt(content[1]);
            if (result < 1 || result > 5) throw new IllegalArgumentException("Boss position must be between 1-5");
            return result;
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Boss position must be an integer");
        }
    }

    public boolean hasActiveSchedule(String guildId, int bossPosition) {
        int bossId = bossService.getBossIdFromBossPosition(guildId, bossPosition);
        return scheduleService.hasActiveScheduleForBoss(guildId, bossId);
    }

    private MessageEmbed createEmbed(String guildId, int bossId, List<String> attacking, List<String> attacked) {
        // TODO: There are A LOT of database queries here, at some point we should probably consider caching most of this
        BossEntity boss = bossService.getBossFromId("" + bossId);
        GuildEntity guild = guildService.getGuild(guildId);
        boolean isCurrentBoss = guild.getBoss().equals(boss);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Scheduling for " + boss.getName());
        // IMPORTANT: When changing this, just extract it to a field, this is effectively also used as an ID, and all the code will break if only this is changed
        eb.addField("Attacks ready", formatUserList(attacking), true);
        eb.addField("Have attacked", formatUserList(attacked), true);
        if (isCurrentBoss) {
            eb.addField("Boss health", formatBossField(guild.getCurrentHealth(), boss.getTotalHealth()), false);
        } else {
            eb.addField("Boss health", "Boss health will be shown when boss is active", false);
        }
        eb.setFooter("An admin can set the expected number of attacks for this schedule using `!setattacks " + boss.getPosition() + " <attacks>`");
        return eb.build();
    }

    private List<String> extractUserList(String fieldContent) {
        String[] rawUsers = fieldContent.split("\n");
        List<String> result = new ArrayList<>();
        // TODO: Add support for strikethrough names
        for (String user : rawUsers) {
            String[] split = user.split(" - ");
            result.add(split[1]);
        }
        return result;
    }

    private String formatBossField(int currentHealth, int maxHealth) {
        StringBuilder sb = new StringBuilder();
        sb.append(currentHealth)
                .append(" / ")
                .append(maxHealth)
                .append("\n");
        double ratio = (currentHealth / (double) maxHealth) * 10.0;
        for (int i = 0; i < 10; i++) {
            if (i < ratio) {
                sb.append("\uD83D\uDFE9");
            }
            else {
                sb.append("\uD83D\uDFE5");
            }
        }
        return sb.toString();
    }

    private String formatUserList(List<String> userList) {
        if (userList.isEmpty()) return "\u200B"; // Field cannot be empty

        StringBuilder sb = new StringBuilder();
        Collections.sort(userList);
        for (int i = 0 ; i < userList.size() ; i++) {
            String newName = i + " - " + userList.get(i);
            userList.set(i, newName);
        }
        String prefix = "";
        for (String attacker : userList) {
            // This check is needed because discord cannot accept empty fields
            // Please fire whoever decided that, thanks
            if (!attacker.equals("\u200B")) {
                sb.append(prefix).append(attacker);
                prefix = "\n";
            }
        }
        // Same issue :)
        if (sb.toString().equals("")) return "\u200B";
        return sb.toString();
    }

    public void createSchedule(CommandContext ctx, int bossPosition) {
        int bossId = bossService.getBossIdFromBossPosition(ctx.getGuildId(), bossPosition);
        scheduleService.createScheduleForGuildAndBoss(ctx.getGuildId(), bossId);
        ctx.getChannel().sendMessageEmbeds(createEmbed(ctx.getGuildId(), bossId, new ArrayList<>(), new ArrayList<>()))
                .queue(message -> {
                    try {
                        scheduleService.setChannelId(ctx.getGuildId(), bossId, ctx.getChannel().getId());
                        scheduleService.setMessageId(ctx.getGuildId(), bossId, message.getId());
                    } catch (ScheduleDoesNotExistException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void deleteSchedule(CommandContext ctx, int bossPosition) {
        // TODO: Throw exception if schedule doesn't exist
        int bossId = bossService.getBossIdFromBossPosition(ctx.getGuildId(), bossPosition);
        ScheduleEntity scheduleEntity = scheduleService.getScheduleByGuildIdAndBossId(ctx.getGuildId(), bossId);
        String channelId = scheduleEntity.getChannelId();
        String messageId = scheduleEntity.getMessageId();
        ctx.getGuild().getTextChannelById(channelId).deleteMessageById(messageId).queue();
    }

    public void updateSchedule(JDA jda, String guildId, int bossPosition) {
        if (!hasActiveSchedule(guildId, bossPosition)) return;

        int bossId = bossService.getBossIdFromBossPosition(guildId, bossPosition);
        ScheduleEntity scheduleEntity = scheduleService.getScheduleByGuildIdAndBossId(guildId, bossId);
        String channelId = scheduleEntity.getChannelId();
        String messageId = scheduleEntity.getMessageId();
        TextChannel channel = jda.getGuildById(guildId).getTextChannelById(channelId);
        channel.retrieveMessageById(messageId).queue(message -> {
            MessageEmbed oldEmbed = message.getEmbeds().get(0);
            List<MessageEmbed.Field> fields = oldEmbed.getFields();
            List<String> attackingList = new ArrayList<>();
            List<String> attackedList = new ArrayList<>();
            for (MessageEmbed.Field field : fields) {
                if (field.getName().equals("Attacks ready")) {
                    attackingList = extractUserList(field.getValue());
                }
                if (field.getName().equals("Have attacked")) {
                    attackedList = extractUserList(field.getValue());
                }
            }
            MessageEmbed newEmbed = createEmbed(guildId, bossId, attackingList, attackedList);
            channel.editMessageEmbedsById(messageId, newEmbed).queue();
        });
    }
}
