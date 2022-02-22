package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.common.CBUtils;
import bot.exceptions.*;
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

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ScheduleStrategy {
    // TODO: Currently does not automatically update the next schedule with boss info when a boss dies
    private final ScheduleService scheduleService;
    private final GuildService guildService;
    private final BossService bossService;
    // To prevent errors
    private final String ATTACKING = "attacking";
    private final String ATTACKED = "attacked";

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

    public boolean hasActiveSchedule(String guildId) {
        return scheduleService.hasActiveScheduleForBoss(guildId);
    }

    private MessageEmbed createEmbed(String guildId, Map<Integer, List<String>> positionAttackingMap, Map<Integer, List<String>> positionAttackedMap) {
        GuildEntity guild = guildService.getGuild(guildId);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("**Lap " + guild.getLap() + "**", "\u200B", false);

        for (int i = 1; i <= 10; i++) {
            if (i == 6) {
                eb.addField("**Lap " + (guild.getLap() + 1) + "**", "Some info \u200B", true);
                eb.addBlankField(true);
                eb.addBlankField(false);
            }

            // A bit of a magical condition here, but this essentially just inserts a space between each column,
            // since each lap has an uneven number of bosses, this has to switch after the first lap
            if ((i % 2 == 0 && i < 6) || (i % 2 == 1 && i > 6)) {
                eb.addBlankField(true);
            }
            List<String> attacking = positionAttackingMap.getOrDefault(i, new ArrayList<>());
            List<String> attacked = positionAttackedMap.getOrDefault(i, new ArrayList<>());
            StringBuilder memberFieldContent = new StringBuilder();
            String prefix = "";
            for (String member : attacking) {
                memberFieldContent.append(prefix).append(member);
                prefix = "\n";
            }
            for (String member : attacked) {
                memberFieldContent.append(prefix)
                        .append("~~")
                        .append(member)
                        .append("~~");
                prefix = "\n";
            }
            BossEntity boss = schedule.getPositionBossIdMap().get(i);
            String bossInfo = "";
            if (guild.getBoss().equals(boss) && i < 6) {
                bossInfo = formatBossField(guild.getCurrentHealth(), boss.getTotalHealth());
            }
            eb.addField(boss.getName(), bossInfo + "\n" + memberFieldContent.toString(), true);
            if (i == 5) {
                eb.addBlankField(true);
                eb.addBlankField(false);
            }
        }
        eb.setFooter("Some info about how to use it here");
        return eb.build();
    }

    private List<String> extractUserList(String fieldContent) {
        String[] rawUsers = fieldContent.split("\n");
        List<String> result = new ArrayList<>();
        // TODO: Add support for strikethrough names
        // Something something discord doesn't like empty fields
        if (rawUsers[0].equals("\u200B")) return result;
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

    // Very scuffed return value, but this is essentially "just" a tuple of two maps, the keys for the outer map will ALWAYS be either "attacking" or "attacked"
    public Map<String, Map<Integer, List<String>>> extractMembers(JDA jda, String guildId) {
        String rawContent = extractAllContent(jda, guildId);
        String[] fieldArr = rawContent.split(";");
        Map<String, Map<Integer, List<String>>> result = new HashMap<>();
        Map<Integer, List<String>> attackingMap = new HashMap<>();
        Map<Integer, List<String>> attackedMap = new HashMap<>();
        GuildEntity guild = guildService.getGuild(guildId);

        for (String field : fieldArr) {
            String[] content = field.split("-");
            String fieldName = content[0];
            String fieldContent = content[1];
            boolean isEmptyField = (int) fieldName.charAt(0) == 8206; // I have no idea why discord does this, and why java has such a hard time finding this char, but oh well
            if (!fieldName.contains("Lap") && !isEmptyField) {
                List<String> attackingMembers = new ArrayList<>();
                List<String> attackedMembers = new ArrayList<>();
                String[] allMembers = fieldContent.split("\n");
                for (String member : allMembers) {
                    // Filter out boss health message
                    if (!member.contains("/") && !member.contains("\uD83D\uDFE9")) {
                        if (member.contains("~~")) {
                            // Attacked members have strikethrough, represented as ~~name~~
                            attackedMembers.add(member.substring(2, member.length() - 2));
                        }
                        else {
                            // Attacking members do not
                            attackingMembers.add(member);
                        }
                    }
                }

                int bossPosition = bossService.getPositionFromBossName(fieldName);
                if (attackingMap.containsKey(bossPosition) || attackedMap.containsKey(bossPosition)) {
                    //result
                    attackingMap.put(bossPosition + 5, attackingMembers);
                    attackedMap.put(bossPosition + 5, attackedMembers);
                }
                else {
                    attackingMap.put(bossPosition, attackingMembers);
                    attackedMap.put(bossPosition, attackedMembers);
                }
            }
        }
        result.put(ATTACKING, attackingMap);
        result.put(ATTACKED, attackedMap);
        System.out.println(result);
        return result;
    }

    private String extractAllContent(JDA jda, String guildId) {
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        List<MessageEmbed> embeds = jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).retrieveMessageById(schedule.getMessageId()).complete().getEmbeds();
        StringBuilder sb = new StringBuilder();
        for (MessageEmbed embed : embeds) {
            String prefix = "";
            for (MessageEmbed.Field field : embed.getFields()) {
                char[] ch = new char[field.getName().length()];
                for (int i = 0; i < field.getName().length(); i++) {
                    ch[i] = field.getName().charAt(i);
                }
                sb.append(prefix)
                        .append(field.getName())
                        .append("-")
                        .append(field.getValue());
                prefix = ";";
            }
        }
        return sb.toString();
    }

    public void createSchedule(CommandContext ctx) {
        GuildEntity guild = guildService.getGuild(ctx.getGuildId());
        // If schedule exists, first delete it from the DB and remove the previous message
        if (guild.getSchedule() != null) {
            ScheduleEntity oldSchedule = guild.getSchedule();
            String oldChannelId = oldSchedule.getChannelId();
            String oldMessageId = oldSchedule.getMessageId();
            System.out.println("TRYING TO DELETE MESSAGE WITH ID: " + oldMessageId);
            ctx.getGuild().getTextChannelById(oldChannelId).deleteMessageById(oldMessageId).queue();
            scheduleService.deleteSchedule(ctx.getGuildId());
        }

        // Create new schedule
        scheduleService.createScheduleForGuild(ctx.getGuildId());
        ctx.getChannel().sendMessageEmbeds(createEmbed(ctx.getGuildId(), new HashMap<>() {{put(5, new ArrayList<>(){{add("Test user"); add("user2"); add("user3"); add("user4"); add("user5");}});}},new HashMap<>() {{put(1, new ArrayList<>(){{add("Test user");}});}}))
                .queue(message -> {
                    try {
                        scheduleService.setChannelId(ctx.getGuildId(), ctx.getChannel().getId());
                        scheduleService.setMessageId(ctx.getGuildId(), message.getId());
                    } catch (ScheduleDoesNotExistException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void deleteSchedule(CommandContext ctx) {
        // TODO: Throw exception if schedule doesn't exist
        ScheduleEntity scheduleEntity = scheduleService.getScheduleByGuildId(ctx.getGuildId());
        String channelId = scheduleEntity.getChannelId();
        String messageId = scheduleEntity.getMessageId();
        ctx.getGuild().getTextChannelById(channelId).deleteMessageById(messageId).queue();
    }

    public void updateSchedule(JDA jda, String guildId) {
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        if (schedule == null) return;
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageEmbedsById(schedule.getMessageId(), createEmbed(guildId, attackers, attacked)).queue();
    }

    public void addAttacker(JDA jda, String guildId, Integer position, String name) throws MemberAlreadyExistsException {
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        if (attackers.get(position).contains(name)) throw new MemberAlreadyExistsException();
        attackers.get(position).add(name);
        Map<Integer, List<String>> attacked = allMembers.get("attacked");
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageEmbedsById(schedule.getMessageId(), createEmbed(guildId, attackers, attacked)).queue();
    }

    public void removeAttacker(JDA jda, String guildId, Integer position, String name) throws MemberIsNotAttackingException {
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        if (!(attackers.get(position).contains(name) || attacked.get(position).contains(name))) throw new MemberIsNotAttackingException();
        attackers.get(position).remove(name);
        attacked.get(position).remove(name);
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageEmbedsById(schedule.getMessageId(), createEmbed(guildId, attackers, attacked)).queue();
    }

    public void markFinished(JDA jda, String guildId, Integer position, String name) throws MemberHasAlreadyAttackedException, MemberIsNotAttackingException {
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        if (attacked.get(position).contains(name)) throw new MemberHasAlreadyAttackedException();
        if (!attackers.get(position).contains(name)) throw new MemberIsNotAttackingException();
        attackers.get(position).remove(name);
        attacked.get(position).add(name);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageEmbedsById(schedule.getMessageId(), createEmbed(guildId, attackers, attacked)).queue();
    }

    public void unMarkFinished(JDA jda, String guildId, Integer position, String name) throws MemberHasNotAttackedException {
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        if (!attacked.get(position).contains(name)) throw new MemberHasNotAttackedException();
        attacked.get(position).remove(name);
        attackers.get(position).add(name);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageEmbedsById(schedule.getMessageId(), createEmbed(guildId, attackers, attacked)).queue();
    }

    public void validateArguments(CommandContext ctx, String command) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        if (content.length != 4) {
            throw new Exception("Incorrect arguments, please use `!" + command + " <@user> <position> <lap>`");
        }
    }

    public String parseName(CommandContext ctx) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        if (!content[1].contains("<@!")) {
            throw new Exception("User must be a mention");
        }
        try {
            String id = content[1].substring(3, content[1].length() - 1);
            Long.parseLong(id);
            return ctx.getGuild().getMemberById(id).getEffectiveName();
        } catch (NumberFormatException e) {
            throw new Exception("User must be a mention");
        }
    }

    public int parsePosition(CommandContext ctx, String command) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        int position;
        try {
            position = Integer.parseInt(content[2]);
        } catch (NumberFormatException e) {
            throw new Exception("Position and Lap must be numbers, please use `!" + command + " <@user> <position> <lap>`");
        }
        if (position < 1 || position > 5) {
            throw new Exception("The position must be between 1 and 5");
        }
        return position;
    }

    public int parseLap(CommandContext ctx, String command, int currentLap) throws Exception {
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        int lap;
        try {
            lap = Integer.parseInt(content[3]);
        } catch (NumberFormatException e) {
            throw new Exception("Position and Lap must be numbers, please use `!" + command + " <@user> <position> <lap>`");
        }
        if (lap != currentLap && lap != (currentLap + 1)) {
            throw new Exception("The lap must be either the current lap or the next lap, scheduling further than the next lap is not currently supported");
        }
        return lap;
    }

    /**
     * Checks if a given user is currently signed up for the current boss of the guild
     * @param jda The discord JDA
     * @param guildId GuildID of the user
     * @param user The display name of the user
     * @return true if the user is signed up for the current boss and has not completed their attack
     */
    public boolean isAttackingCurrentBoss(JDA jda, String guildId, String user) {
        GuildEntity guild = guildService.getGuild(guildId);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        Map<Integer, List<String>> attackingMap = extractMembers(jda, guildId).get(ATTACKING);
        int position = guild.getBoss().getPosition();
        return attackingMap.get(position).contains(user);
    }
}
