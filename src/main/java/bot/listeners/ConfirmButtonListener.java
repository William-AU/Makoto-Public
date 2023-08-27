package bot.listeners;

import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.battles.strategies.PictureStrategy;
import bot.commands.framework.ICommandContext;
import bot.commands.framework.ManualCommandContext;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.ConfirmButtonType;
import bot.exceptions.schedule.ScheduleException;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.utils.CachedSpreadSheetUtils;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.*;
import java.util.stream.Collectors;

public class ConfirmButtonListener extends ListenerAdapter {
    private final ScheduleStrategy scheduleStrategy;
    private final GuildService guildService;
    private final BossService bossService;
    private final SheetService sheetService;
    private final DamageStrategy damageStrategy;
    private final CachedSpreadSheetUtils cachedSpreadSheetUtils;


    public ConfirmButtonListener(ScheduleStrategy scheduleStrategy, GuildService guildService, BossService bossService, SheetService sheetService, DamageStrategy damageStrategy, CachedSpreadSheetUtils cachedSpreadSheetUtils) {
        this.scheduleStrategy = scheduleStrategy;
        this.guildService = guildService;
        this.bossService = bossService;
        this.sheetService = sheetService;
        this.damageStrategy = damageStrategy;
        this.cachedSpreadSheetUtils = cachedSpreadSheetUtils;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();
        String buttonId = button.getId();
        System.out.println("Found button with id: " + buttonId);
        String[] split = buttonId.split("-");
        ConfirmButtonType type;
        try {
            type = ConfirmButtonType.valueOf(split[1].toUpperCase());
        } catch (IllegalArgumentException wrongButton) {
            type = ConfirmButtonType.MISC;
        }
        if (!knownCommand(split[0])) return;
        event.deferReply(true).queue();

        if (type.equals(ConfirmButtonType.ABORT)) {
            event.getMessage().delete().queue();
            event.getHook().sendMessage("Aborted").queue();
            return;
        }

        System.out.println("Split: " + split[0]);
        switch (split[0].toLowerCase()) {
            case "lapoverride" -> handleLapOverride(event, split);
            case "hardreset" -> handleHardReset(event, split);
            case "register" -> handleRegister(event, split);
            case "setup" -> handleSetup(event, split[1].toLowerCase());
            case "addmembers" -> handleAddMembers(event, split);
            case "removemembers" -> handleRemoveMembers(event, split);
            case "addbattle" -> handleAddBattle(event, split);
            case "addbattlefor" -> handleAddBattleFor(event, split);
            case "addcarryover" -> handleAddCarryover(event, split);
            case "addcarryoverfor" -> handleAddCarryoverFor(event, split);
            case "redobattle" -> handleRedoBattle(event, split);
            case "redobattlefor" -> handleRedoBattleFor(event, split);
        }
    }

    // Very ugly code duplication, only used to ensure we don't double defer and have to deal with RestAction failures
    private boolean knownCommand(String command) {
        Set<String> knownCommands = new HashSet<>() {{
           add("lapoverride");
           add("hardreset");
           add("register");
           add("setup");
           add("addmembers");
           add("removemembers");
           add("addbattle");
           add("addbattlefor");
           add("addcarryover");
           add("addcarryoverfor");
           add("redobattle");
           add("redobattlefor");
        }};
        return knownCommands.contains(command);
    }


    private void handleRedoBattleFor(ButtonInteractionEvent event, String[] split) {
        Message message = getMessageFromID(event, split[1]);
        String user = message.getMentions().getUsers().get(0).getId();
        String damage = message.getContentRaw().split(" ")[1];
        redoBattle(event, user, split[2], damage);
    }

    private void handleRedoBattle(ButtonInteractionEvent event, String[] split) {
        Message message = getMessageFromID(event, split[1]);
        String damage = message.getContentRaw().split(" ")[1];
        redoBattle(event, message.getAuthor().getId(), split[2], damage);
    }

    private void redoBattle(ButtonInteractionEvent event, String userId, String spreadsheetName, String damage) {
        String sheetID = guildService.getSpreadSheetIdFromName(event.getGuild().getId(), spreadsheetName);
        damageStrategy.redoBattle(userId, spreadsheetName, damage, event.getJDA());
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully redid battle").queue();
    }

    private void handleAddCarryoverFor(ButtonInteractionEvent event, String[] split) {
        Message message = getMessageFromID(event, split[1]);
        String user = message.getMentions().getUsers().get(0).getId();
        String damage = message.getContentRaw().split(" ")[1];
        carryover(event, user, split[2], damage);
    }

    private void handleAddCarryover(ButtonInteractionEvent event, String[] split) {
        Message message = getMessageFromID(event, split[1]);
        String damage = message.getContentRaw().split(" ")[1];
        carryover(event, message.getAuthor().getId(), split[2], damage);
    }

    private void carryover(ButtonInteractionEvent event, String userId, String spreadsheetName, String damage) {
        String sheetID = guildService.getSpreadSheetIdFromName(event.getGuild().getId(), spreadsheetName);
        damageStrategy.addCarryover(userId, sheetID, damage, event.getJDA());
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully added carryover").queue();
    }

    private Message getMessageFromID(ButtonInteractionEvent event, String messageID) {
        return event.getChannel().retrieveMessageById(messageID).complete();
    }

    private void handleAddBattleFor(ButtonInteractionEvent event, String[] split) {
        Message message = getMessageFromID(event, split[1]);
        String user = message.getMentions().getUsers().get(0).getId();
        String damage = message.getContentRaw().split(" ")[1];
        addBattle(event, user, split[2], damage);
    }

    private void handleAddBattle(ButtonInteractionEvent event, String[] split) {
        Message message = getMessageFromID(event, split[1]);
        String damage = message.getContentRaw().split(" ")[1];
        addBattle(event, message.getAuthor().getId(), split[2], damage);
    }

    private void addBattle(ButtonInteractionEvent event, String userId, String spreadsheetName, String damage) {
        String sheetID = guildService.getSpreadSheetIdFromName(event.getGuild().getId(), spreadsheetName);
        damageStrategy.addBattle(userId, sheetID, damage, event.getJDA());
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully added battle").queue();
    }

    private void handleRemoveMembers(ButtonInteractionEvent event, String[] split) {
        String messageID = split[1];
        Message originalMessage = event.getChannel().retrieveMessageById(messageID).complete(); // Yes bad idea to use complete but should be fine here as we want thread blocking behaviour
        List<User> mentionedUsers = originalMessage.getMentions().getUsers();
        String sheetName = split[2];
        String spreadsheetID = getSpreadsheetIDFromName(sheetName, event);
        List<String> usersString = new ArrayList<>() {{
            for (User user : mentionedUsers) {
                add(user.getId());
            }
        }};
        sheetService.removeMembersFromSheet(usersString, spreadsheetID);
        for (User user : mentionedUsers) {
            cachedSpreadSheetUtils.removeMember(event.getGuild().getId(), spreadsheetID, user.getId());
        }
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully removed member(s) from sheet " + sheetName).queue();
    }

    private void handleAddMembers(ButtonInteractionEvent event, String[] split) {
        String messageID = split[1];
        Message originalMessage = event.getChannel().retrieveMessageById(messageID).complete(); // Same consideration as in addmembers
        List<User> mentionedMembers = originalMessage.getMentions().getUsers();
        String sheetName = split[2];
        String spreadsheetID = getSpreadsheetIDFromName(sheetName, event);
        sheetService.addUsersToSheet(mentionedMembers, spreadsheetID);
        for (User user : mentionedMembers) {
            cachedSpreadSheetUtils.addMember(event.getGuild().getId(), spreadsheetID, user.getId());
        }
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully added member(s) to sheet " + sheetName).queue();
    }

    private String getSpreadsheetIDFromName(String name, ButtonInteractionEvent event) {
        if (name.equalsIgnoreCase("main")) {
            return guildService.getSpreadSheetId(event.getGuild().getId());
        }
        List<GuildService.Spreadsheet> spreadsheets = guildService.getAdditionalSpreadsheets(event.getGuild().getId());
        GuildService.Spreadsheet toFind = null;
        for (GuildService.Spreadsheet sheet : spreadsheets) {
            if (sheet.getName().equalsIgnoreCase(name)) {
                toFind = sheet;
            }
        }
        if (toFind == null) throw new IllegalArgumentException("Unable to find spreadsheet with name: " + name);
        return toFind.getID();
    }

    private void handleSetup(ButtonInteractionEvent event, String id) {
        List<User> rawUsers = event.getChannel().asTextChannel().getMembers().stream().map(Member::getUser).collect(Collectors.toList());
        List<User> users = new ArrayList<>() {{
           for (User user : rawUsers) {
               if (!user.isBot()) add(user);
           }
        }};
        String spreadsheetId = getSpreadsheetIDFromName(id, event);

        try {
            sheetService.setupSheet(event.getGuild().getId(), spreadsheetId, users);
        } catch (Exception e) {
            e.printStackTrace();
            event.getMessage().delete().queue();
            event.getHook().sendMessage("Something went wrong trying to register sheet, perhaps you already set up the sheet? Otherwise ask for help :(").queue();
            return;
        }
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully registered sheet").queue();
    }

    private void handleHardReset(ButtonInteractionEvent event, String[] split) {
        ManualCommandContext ctx = new ManualCommandContext(event.getGuild(), event.getGuild().getId(), event.getJDA());
        if (split.length == 4) {
            String name = split[3];
            scheduleStrategy.resetSchedule(ctx, name);
        } else {
            scheduleStrategy.resetSchedule(ctx);
        }
        event.getHook().sendMessage("Success!").queue();
    }

    private void handleLapOverride(ButtonInteractionEvent event, String[] split) {
        guildService.setLap(event.getGuild().getId(), Integer.parseInt(split[2]));
        // This could be one call, instead of two
        bossService.resetBossHP(event.getGuild().getId());
        bossService.resetBoss(event.getGuild().getId());
        ICommandContext ctx = new ManualCommandContext(event.getGuild(), event.getGuild().getId(), event.getJDA());
        try {
            scheduleStrategy.createSchedule(ctx, null);
        } catch (ScheduleException e) {
            e.printStackTrace();
        }
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully changed lap").queue();
    }

    private String generateClanName(List<String> currentSpreadSheetNames) {
        Random rn = new Random();
        int number = rn.nextInt(999);
        String res = "clan" + number;
        for (String name : currentSpreadSheetNames) {
            if (name.equals(res)) {
                return generateClanName(currentSpreadSheetNames);
            }
        }
        return res;
    }

    private String extractFullID(String[] split, int offset) {
        StringBuilder sb = new StringBuilder();
        sb.append(split[3 - offset]);
        int basePos = 4 - offset;
        if (split.length > basePos) {
            for (int i = basePos; i < split.length; i++) {
                sb.append("-").append(split[i]);
            }
        }
        System.out.println("Extracted full ID: " + sb);
        return sb.toString();
    }

    private void handleRegister(ButtonInteractionEvent event, String[] split) {
        //System.out.println("----Register command----");
        switch (split[1].toLowerCase()) {
            case "add new guild" -> {
                String spreadsheet = extractFullID(split, 1);
                Guild guild = event.getGuild();
                assert guild != null;
                List<String> knownNames = guildService.getSpreadsheetNames(guild.getId());
                String clanName = generateClanName(knownNames);
                guildService.addAdditionalSpreadsheetId(guild.getId(), spreadsheet, clanName);
                event.getHook().sendMessage("Added new guild with id: " + clanName + "\nTo change id use `!changeClanName <clanID> <name>`").queue();
            } // Retrieve args from button id (should be split[2]) and add spreadsheet to second guild in discord
            case "replace existing guild" -> {
                Guild guild = event.getGuild();
                assert (guild != null);
                String spreadsheetToReplace = split[2];
                if (spreadsheetToReplace.equalsIgnoreCase("Main")) {
                    guildService.addSpreadsheetId(guild.getId(), extractFullID(split, 0));
                    event.getHook().sendMessage("Replaced main guild").queue();
                } else {
                    String spreadsheetId = guildService.getSpreadsheetNameFromID(guild.getId(), spreadsheetToReplace);
                    String newSpreadsheet = extractFullID(split, 0);
                    String name = guildService.getSpreadsheetNameFromID(guild.getId(), spreadsheetToReplace);
                    guildService.removeAdditionalSpreadsheetBySmallID(guild.getId(), spreadsheetId);
                    guildService.addAdditionalSpreadsheetId(guild.getId(), newSpreadsheet, name);
                    event.getHook().sendMessage("Replaced guild " + name).queue();
                }
            } // Retrieve args from button id and replace existing spreadsheet for guild
            case "abort" -> {
                event.getHook().sendMessage("Aborted").queue();
            } // Abort current action
        }
        event.getMessage().delete().queue();
    }
}
