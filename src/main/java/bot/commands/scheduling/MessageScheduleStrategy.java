package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.common.ScheduleButtonType;
import bot.exceptions.*;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.ScheduleService;
import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.models.ScheduleEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

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

    // Define this as a constant to avoid typos and to allow renaming the category name
    private final String SCHEDULING_CATEGORY_NAME = "makoto-scheduling";
    private final String SCHEDULING_CHANNEL_NAME = "schedule";
    private boolean hasScheduleChannels(CommandContext ctx) {
        // TODO: Remove, only for testing
        System.out.println("CHECKING IF SCHEDULES EXIST");
        List<Category> categories = ctx.getGuild().getCategoriesByName(SCHEDULING_CATEGORY_NAME, true);
        System.out.println(categories.size());
        if (categories.size() == 1) {
            System.out.println("DELETING CATEGORY");
            for (TextChannel channel : categories.get(0).getTextChannels()) {
                channel.delete().reason("Test").queue();
            }
            categories.get(0).delete().reason("test").queue();
        }

        List<TextChannel> channels = ctx.getGuild().getTextChannels();
        List<String> neededChannels = new ArrayList<>() {{
            // The channels that are expected to exist, note that these should not be changed unless absolutely necessary since it will mess up peoples channels
            add(SCHEDULING_CHANNEL_NAME);
            add("boss_1");
            add("boss_2");
            add("boss_3");
            add("boss_4");
            add("boss_5");
        }};
        List<String> foundChannels = new ArrayList<>();
        for (TextChannel channel : channels) {
            foundChannels.add(channel.getName());
        }
        return foundChannels.containsAll(neededChannels);
    }

    private void createScheduleChannels(CommandContext ctx) throws ScheduleException {
        ctx.getGuild().createCategory(SCHEDULING_CATEGORY_NAME)
                // Very messy permissions override, this seems to be the easiest way to make the channel effectively read only, note that all text channels inherit from the category
                .addPermissionOverride(ctx.getGuild().getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND)).queue(category -> {
            // Potential for a lot of race conditions here if we start working on these channels before they are actually created
            category.createTextChannel("schedule").queue();
            category.createTextChannel("boss_1").queue();
            category.createTextChannel("boss_2").queue();
            category.createTextChannel("boss_3").queue();
            category.createTextChannel("boss_4").queue();
            category.createTextChannel("boss_5").queue(ignored -> {
                // Very messy, but all further calls are done in here, since we should be able to guarantee this is the last call
                // There will likely be a lot of rate limiting here, so expect this command to be SLOW on startup
                for (TextChannel channel : category.getTextChannels()) {
                    if (channel.getName().equals(SCHEDULING_CHANNEL_NAME)) {
                        // This should be the target of the main scheduling message
                        channel.sendMessageEmbeds(createScheduleEmbed(ctx)).queue();
                        try {
                            scheduleService.setChannelId(ctx.getGuildId(), channel.getId());
                            createAndSendSchedule(ctx);
                        } catch (ScheduleDoesNotExistException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Handle all 5 boss channels here
                        // Ugly nested method call, this basically just reads the last number in the name of the channel and parses it as an integer

                        // NOTE: THIS MEANS WE IMPLICITLY ENFORCE A NAMING SCHEME OF TEXT CHANNELS IN THE CATEGORY
                        System.out.println("Trying to split: " + channel.getName());
                        int bossPosition = Integer.parseInt(channel.getName().split("_")[1]);
                        List<MessageEmbed> bossEmbeds = createBossEmbed(ctx.getJDA(), ctx.getGuildId(), bossPosition);
                        channel.sendMessageEmbeds(bossEmbeds.get(0)).setActionRow(createBossButtons(ctx.getGuildId(), bossPosition)).queue(ignored2 -> {
                            channel.sendMessageEmbeds(bossEmbeds.get(1)).setActionRow(createBossButtons(ctx.getGuildId(), bossPosition + 5)).queue();
                        });
                    }
                }
            });
        });
    }

    private List<Button> createBossButtons(String guildId, int bossPosition) {
        // Protocol for button id is as follows:
        // ScheduleButtonType-GUILD_ID-BOSS_POSITION-BOSS_LAP
        // Example: "JOIN-253155853274841107-2-2"
        // Note that we do not use the relative position convention here, since we need the absolute lap value,
        // otherwise it would not be possible to check if the button is for this lap or the next
        int lap = guildService.getGuild(guildId).getLap();
        if (bossPosition > 5) {
            lap++;
            bossPosition -= 5;
        }
        String idSuffix = "-" + guildId + "-" + bossPosition + "-" + lap;
        Button joinButton = Button.primary(ScheduleButtonType.JOIN + idSuffix, "Join");
        Button leaveButton = Button.danger(ScheduleButtonType.LEAVE + idSuffix, "Leave");
        Button completeButton = Button.success(ScheduleButtonType.COMPLETE + idSuffix, "Complete");
        Button unCompleteButton = Button.danger(ScheduleButtonType.UNCOMPLETE + idSuffix, "Unmark Completion");
        return new ArrayList<>() {{
            add(joinButton);
            add(leaveButton);
            add(completeButton);
            add(unCompleteButton);
        }};
    }



    // This has been moved out of the original createSchedule() method to allow it to be called only after the channels have been created
    private void createAndSendSchedule(CommandContext ctx) {
        // When creating the channel, the schedule channel ID is set, this means we now force the channel to be the one created by the bot, no more placing it wherever you want
        String channelID = scheduleService.getScheduleByGuildId(ctx.getGuildId()).getChannelId();
        ctx.getGuild()
                .getTextChannelById(channelID)
                // TODO: Consider fixing this null pointer, although it theoretically shouldn't matter
                .sendMessage(createMessage(ctx.getGuildId(), ctx.getGuild().getName(), new HashMap<>(), new HashMap<>()))
                .queue(message -> {
                    try {
                        scheduleService.setMessageId(ctx.getGuildId(), message.getId());
                    } catch (ScheduleDoesNotExistException ignored) {  } // We literally just created this schedule, if it doesn't exist something is seriously wrong
                });
    }

    private MessageEmbed createScheduleEmbed(CommandContext ctx) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Main scheduling channel");
        eb.setDescription("This channel will contain an overview of all current bosses, as well as what members plan to attack or have already attacked." +
                "\n" + "To queue up for an attack, use the generated channels #boss_1 to #boss_5. Admins can manually add or remove members using the commands "
                + " `addspot <@user> <position> <lap>`, `!removespot <@user> <position> <lap>`, and `!completespot <@user> <position> <lap>`" + "\n"
                + "The schedule will automatically mark members as having completed their attack if they use the `!addbattle` command, note that this requires "
                + "members to input their scores or it will not track the current boss correctly, currently there is no way to manually change the tracking HP, (this is planned though)");
        return eb.build();
    }

    /**
     * Creates two embeds for the given boss, first for the current lap, second for the next lap
     * @param ctx command context for the original schedule call
     * @param bosPosition the position of the boss
     * @return a list of size 2 containing the two embeds
     */
    private List<MessageEmbed> createBossEmbed(JDA jda, String guildId, int bosPosition) {
        EmbedBuilder eb1 = new EmbedBuilder();
        eb1.setTitle("Current lap");

        EmbedBuilder eb2 = new EmbedBuilder();
        eb2.setTitle("Next lap");

        return new ArrayList<>() {{
            add(eb1.build());
            add(eb2.build());
        }};
    }


    @Override
    public void createSchedule(CommandContext ctx) throws ScheduleException {
        GuildEntity guild = guildService.getGuild(ctx.getGuildId());

        // Delete old schedule if it exists
        if (guild.getSchedule() != null) {
            ScheduleEntity oldSchedule = guild.getSchedule();
            String oldChannelID = oldSchedule.getChannelId();
            String oldMessageID = oldSchedule.getMessageId();
            if (oldChannelID != null && oldMessageID != null) {
                ctx.getGuild().getTextChannelById(oldChannelID).deleteMessageById(oldMessageID).queue();
            }
            scheduleService.deleteSchedule(ctx.getGuildId());
        }

        // Create new schedule
        scheduleService.createScheduleForGuild(ctx.getGuildId());

        if (!hasScheduleChannels(ctx)) {
            // If this throws an exception, we completely exit the method and rely on parent to do error handling
            createScheduleChannels(ctx);
        }
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

    private void updateBossChannel(JDA jda, String guildId, int newBossPosition) {
        int deadBossPosition = newBossPosition;
        if (newBossPosition == 1) {
            // This means we killed B5
            deadBossPosition = 5;
        }
        else {
            // All other options just need to be moved back one
            deadBossPosition--;
        }

        List<TextChannel> channels = jda.getGuildById(guildId)
                .getCategoriesByName(SCHEDULING_CATEGORY_NAME, true)
                .get(0)
                .getTextChannels();
        for (TextChannel channel : channels) {
            if (!channel.getName().equals(SCHEDULING_CHANNEL_NAME)) {
                int channelBoss = Integer.parseInt(channel.getName().split("_")[1]);

                boolean isNewLap = deadBossPosition == 5;
                // Convert to final so that lambda is happy later       No clue why this is needed
                final int finalBossPos = deadBossPosition;
                // This is a bit messy, but it is needed in order to avoid a race condition relating to the getHistory() call
                if (isNewLap) {
                    if (channelBoss == deadBossPosition) {
                        channel.getHistory()
                                .retrievePast(2)
                                .map(messages -> messages.get(1))
                                .queue(message -> message.delete().queue(ignored -> channel
                                        .sendMessageEmbeds(createBossEmbed(jda, guildId, finalBossPos).get(1))
                                        .setActionRow(createBossButtons(guildId, finalBossPos + 5))
                                        .queue()));
                    } else {
                        channel.sendMessageEmbeds(createBossEmbed(jda, guildId, finalBossPos).get(1))
                                .setActionRow(createBossButtons(guildId, finalBossPos + 5))
                                .queue();
                    }
                }
                // Delete the first message in the channel if it corresponds to the defeated boss
                if (channelBoss == deadBossPosition && !isNewLap) {
                    channel.getHistory()
                            .retrievePast(2)
                            .map(messages -> messages.get(1))
                            .queue(message -> message.delete().queue());
                }
            }
        }
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
            updateBossChannel(jda, guildId, newBoss.getPosition());
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
