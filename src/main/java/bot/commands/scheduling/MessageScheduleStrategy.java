package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.exceptions.*;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.ScheduleService;
import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.models.ScheduleEntity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;

public class MessageScheduleStrategy implements ScheduleStrategy {
    private final ScheduleService scheduleService;
    private final GuildService guildService;
    private final BossService bossService;

    private final String ATTACKING = "attacking";
    private final String ATTACKED = "attacked";

    public MessageScheduleStrategy(ScheduleService scheduleService, GuildService guildService, BossService bossService) {
        this.scheduleService = scheduleService;
        this.guildService = guildService;
        this.bossService = bossService;
    }

    /**
     * Check if a guild already has a schedule
     *
     * @param guildId String ID of the guild
     * @return true if a schedule exists, false otherwise
     */
    @Override
    public boolean hasActiveSchedule(String guildId) {
        return scheduleService.hasActiveScheduleForBoss(guildId);
    }

    @Override
    // Note: This can NEVER be called in a queue callback, because it uses .complete()
    public Map<String, Map<Integer, List<String>>> extractMembers(JDA jda, String guildId) {
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        Message message = jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).retrieveMessageById(schedule.getMessageId()).complete();
        String[] fullMessage = message.getContentRaw().split("\n");
        System.out.println(Arrays.toString(fullMessage));
        Map<String, Map<Integer, List<String>>> result = new HashMap<>();
        Map<Integer, List<String>> attackingBossMap = new HashMap<>();
        Map<Integer, List<String>> attackedBossMap = new HashMap<>();
        for (int i = 0; i < fullMessage.length; i++) {
            switch (i) {
                case 0, 1, 2 -> {} // Title, lap, first boss
                case 3, 6, 9, 12, 15, 20, 23, 26, 29, 32 -> {
                    // These will be the attackers for each boss in order
                    int boss = i / 3;   // It turns out that this will always give the correct boss position, even for the numbers 20 - 31, but only because integer division rounds down, yes it's ugly but it should work
                    String[] rawUsers = fullMessage[i].split(", ");
                    List<String> attackers = new ArrayList<>();
                    for (String user : rawUsers) {
                        if (!user.trim().equals("")) {
                            attackers.add(user);
                        }
                    }
                    attackingBossMap.put(boss, attackers);
                }
                case 4, 7, 10, 16, 21, 24, 27, 33 -> {
                    // These will be the finised attackers for each boss in order
                    int boss = (i - 1) / 3; // Same logic as above, except we need to subtract one or the values don't work for 21 - 32
                    String[] rawUsers = fullMessage[i].split(", ");
                    List<String> attacked = new ArrayList<>();
                    for (String user : rawUsers) {
                        // Remove the "~~" at the start and end of user
                        if (!user.trim().equals("")) {
                            String newUser = user.substring(2, user.length() - 2);
                            attacked.add(newUser);
                        }
                    }
                    attackedBossMap.put(boss, attacked);
                }
                default -> {} // Ignore everything else
            }
        }
        result.put(ATTACKING, attackingBossMap);
        result.put(ATTACKED, attackedBossMap);
        return result;
    }

    @Override
    public void createSchedule(CommandContext ctx) {
        GuildEntity guild = guildService.getGuild(ctx.getGuildId());
        // Delete old schedule if it exists
        if (guild.getSchedule() != null) {
            ScheduleEntity oldSchedule = guild.getSchedule();
            String oldChannelID = oldSchedule.getChannelId();
            String oldMessageID = oldSchedule.getMessageId();
            ctx.getGuild().getTextChannelById(oldChannelID).deleteMessageById(oldMessageID).queue();
            scheduleService.deleteSchedule(ctx.getGuildId());
        }

        // Create new schedule
        scheduleService.createScheduleForGuild(ctx.getGuildId());
        ctx.getChannel().sendMessage(createMessage(ctx.getGuildId(), ctx.getGuild().getName(), new HashMap<>(), new HashMap<>()))
                .queue(message -> {
                    try {
                        scheduleService.setChannelId(ctx.getGuildId(), ctx.getChannel().getId());
                        scheduleService.setMessageId(ctx.getGuildId(), message.getId());
                    } catch (ScheduleDoesNotExistException ignored) {  } // We literally just created this schedule, if it doesn't exist something is seriously wrong
                });
    }

    private String createMessage(String guildId, String guildName, Map<Integer, List<String>> positionAttackingMap, Map<Integer, List<String>> positionAttackedMap) {
        StringBuilder sb = new StringBuilder();
        GuildEntity guild = guildService.getGuild(guildId);

        int currentLap = guild.getLap();
        sb.append("Scheduling for " + guildName).append("\n");
        sb.append("__**Lap ").append(currentLap).append("**__").append("\n");
        BossEntity currentBoss = guild.getBoss();
        Map<Integer, BossEntity> bossMap = guild.getSchedule().getPositionBossIdMap();
        for (int i = 1; i <= 10; i++) {
            if (i == 6) {
                sb.append("\n");
                sb.append("__**Lap ").append(currentLap + 1).append("**__").append("\n");
            }
            if (i == currentBoss.getPosition()) {
                sb.append("**").append(bossMap.get(i).getName()).append(" current boss: (").append(guild.getCurrentHealth()).append("/").append(currentBoss.getTotalHealth()).append(")").append("**").append("\n");
            } else {
                sb.append("**").append(bossMap.get(i).getName()).append("**").append("\n");
            }
            String prefix = "";
            // Get attackers for current boss
            for (String attacker : positionAttackingMap.getOrDefault(i, new ArrayList<>())) {
                sb.append(prefix).append(attacker);
                prefix = ", ";
            }
            sb.append("\n");

            prefix = "";
            // Get attacked for current boss
            for (String attacked : positionAttackedMap.getOrDefault(i, new ArrayList<>())) {
                sb.append(prefix).append("~~").append(attacked).append("~~");
                prefix = ", ";
            }
            sb.append("\n");
        }
        sb.append("Some info here");

        return sb.toString();
    }

    @Override
    public void deleteSchedule(CommandContext ctx) {
        ScheduleEntity scheduleEntity = scheduleService.getScheduleByGuildId(ctx.getGuildId());
        String channelId = scheduleEntity.getChannelId();
        String messageId = scheduleEntity.getMessageId();
        ctx.getGuild().getTextChannelById(channelId).deleteMessageById(messageId).queue();
    }

    @Override
    public void updateSchedule(JDA jda, String guildId, boolean bossDead) {
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        if (schedule == null) return;
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        // Note that if a boss is dead, we can only query the NEW boss, not the old one
        if (bossDead) {
            BossEntity newBoss = guildService.getGuild(guildId).getBoss();
            if (newBoss.getPosition() == 1) {
                // This means we are dealing with a new lap
                // Some weird inner class rules java has? No clue why this is needed tbh
                Map<Integer, List<String>> finalAttackers = attackers;
                attackers = new HashMap<>() {{
                   put(1, finalAttackers.getOrDefault(6, new ArrayList<>()));
                   put(2, finalAttackers.getOrDefault(7, new ArrayList<>()));
                   put(3, finalAttackers.getOrDefault(8, new ArrayList<>()));
                   put(4, finalAttackers.getOrDefault(9, new ArrayList<>()));
                   put(5, finalAttackers.getOrDefault(10, new ArrayList<>()));
                }};

                Map<Integer, List<String>> finalAttacked = attacked;
                attacked = new HashMap<>() {{
                    put(1, finalAttacked.getOrDefault(6, new ArrayList<>()));
                    put(2, finalAttacked.getOrDefault(7, new ArrayList<>()));
                    put(3, finalAttacked.getOrDefault(8, new ArrayList<>()));
                    put(4, finalAttacked.getOrDefault(9, new ArrayList<>()));
                    put(5, finalAttacked.getOrDefault(10, new ArrayList<>()));
                }};
            }
        }
        String guildName = jda.getGuildById(guildId).getName();
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageById(schedule.getMessageId(), createMessage(guildId, guildName, attackers, attacked)).queue();
    }

    @Override
    public void addAttacker(JDA jda, String guildId, Integer position, String name) throws MemberAlreadyExistsException {
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        if (attackers.get(position).contains(name)) throw new MemberAlreadyExistsException();
        attackers.get(position).add(name);
        Map<Integer, List<String>> attacked = allMembers.get("attacked");
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        String guildName = jda.getGuildById(guildId).getName();
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageById(schedule.getMessageId(), createMessage(guildId, guildName, attackers, attacked)).queue();
    }

    @Override
    public void removeAttacker(JDA jda, String guildId, Integer position, String name) throws MemberIsNotAttackingException {
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        if (!(attackers.get(position).contains(name) || attacked.get(position).contains(name))) throw new MemberIsNotAttackingException();
        attackers.get(position).remove(name);
        attacked.get(position).remove(name);
        String guildName = jda.getGuildById(guildId).getName();
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageById(schedule.getMessageId(), createMessage(guildId, guildName, attackers, attacked)).queue();
    }

    @Override
    public void markFinished(JDA jda, String guildId, Integer position, String name) throws MemberHasAlreadyAttackedException, MemberIsNotAttackingException {
        String guildName = jda.getGuildById(guildId).getName();
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        if (attacked.get(position).contains(name)) throw new MemberHasAlreadyAttackedException();
        if (!attackers.get(position).contains(name)) throw new MemberIsNotAttackingException();
        attackers.get(position).remove(name);
        attacked.get(position).add(name);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageById(schedule.getMessageId(), createMessage(guildId, guildName, attackers, attacked)).queue();
    }

    @Override
    public void unMarkFinished(JDA jda, String guildId, Integer position, String name) throws MemberHasNotAttackedException {
        String guildName = jda.getGuildById(guildId).getName();
        Map<String, Map<Integer, List<String>>> allMembers = extractMembers(jda, guildId);
        Map<Integer, List<String>> attackers = allMembers.get(ATTACKING);
        Map<Integer, List<String>> attacked = allMembers.get(ATTACKED);
        if (!attacked.get(position).contains(name)) throw new MemberHasNotAttackedException();
        attacked.get(position).remove(name);
        attackers.get(position).add(name);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        jda.getGuildById(guildId).getTextChannelById(schedule.getChannelId()).editMessageById(schedule.getMessageId(), createMessage(guildId, guildName, attackers, attacked)).queue();
    }

    @Override
    public boolean isAttackingCurrentBoss(JDA jda, String guildId, String user) {
        GuildEntity guild = guildService.getGuild(guildId);
        ScheduleEntity schedule = scheduleService.getScheduleByGuildId(guildId);
        Map<Integer, List<String>> attackingMap = extractMembers(jda, guildId).get(ATTACKING);
        int position = guild.getBoss().getPosition();
        return attackingMap.get(position).contains(user);
    }
}
