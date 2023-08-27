package bot.services;

import bot.config.GuildImagePreference;
import bot.storage.models.*;
import bot.storage.repositories.GuildRepository;
import bot.storage.repositories.GuildSpreadsheetRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class GuildService {
    private final GuildRepository guildRepository;
    private final GuildSpreadsheetRepository guildSpreadsheetRepository;

    @Autowired
    public GuildService(GuildRepository guildRepository, GuildSpreadsheetRepository guildSpreadsheetRepository) {
        this.guildRepository = guildRepository;
        this.guildSpreadsheetRepository = guildSpreadsheetRepository;
    }

    public boolean hasActiveBos(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getBoss() != null;
    }

    public GuildEntity getGuild(String guildId) {
        return guildRepository.getGuildEntityByGuildId(guildId);
    }

    public int getNumberOfSpreadsheets(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getAdditionalSpreadsheets().size();
    }

    public boolean hasAdditionalSpreadsheets(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return !guild.getAdditionalSpreadsheets().isEmpty();
    }

    public void addAdditionalSpreadsheetId(String guildId, String id, String name) {
        System.out.println("TRYING TO ADD ADDITIONAL SPREADSHEET WITH ID: " + id + " AND NAME: " + name);
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        List<GuildSpreadsheetEntity> knownSpreadSheets = guild.getAdditionalSpreadsheets();
        GuildSpreadsheetEntity newSpreadsheet = new GuildSpreadsheetEntity() {{
            setName(name);
            setSpreadsheetId(id);
            setGuildEntity(guild);
        }};
        knownSpreadSheets.add(newSpreadsheet);
        guild.setAdditionalSpreadsheets(knownSpreadSheets);
        //guildSpreadsheetRepository.save(newSpreadsheet);
        guildRepository.save(guild);
    }

    public void setClanName(String guildId, String oldName, String newName) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        GuildSpreadsheetEntity entity = guildSpreadsheetRepository.getGuildSpreadsheetEntityByGuildEntityAndName(guild, oldName);
        if (entity == null) {
            System.out.println("Failed to set clan name for " + oldName + " to " + newName + " because " + oldName + " does not exist");
            return;
        }
        entity.setName(newName);
        guildSpreadsheetRepository.save(entity);
    }

    public List<GuildSpreadsheetEntity> getAllSpreadSheets(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guildSpreadsheetRepository.getGuildSpreadsheetEntitiesByGuildEntity(guild);
    }

    public List<String> getSpreadsheetNames(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        List<GuildSpreadsheetEntity> entities = guildSpreadsheetRepository.getGuildSpreadsheetEntitiesByGuildEntity(guild);
        return new ArrayList<>() {{
            for (GuildSpreadsheetEntity entity : entities) {
                if (entity.getName() != null) {
                    add(entity.getName());
                }
            }
        }};
    }

    public void setBossTrackerChannel(String guildId, String channelId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setBossChannelId(channelId);
        guildRepository.save(guild);
    }

    public void setBossTrackerMessage(String guildId, String messageId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setBossMessageId(messageId);
        guildRepository.save(guild);
    }

    public String getSpreadSheetId(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getSpreadsheetId();
    }

    public String getSpreadSheetIdFromName(String guildId, String spreadSheetName) {
        if (spreadSheetName.equalsIgnoreCase("main")) return getSpreadSheetId(guildId);
        List<Spreadsheet> sheets = getAdditionalSpreadsheets(guildId);
        for (Spreadsheet sheet : sheets) {
            if (sheet.getName().equalsIgnoreCase(spreadSheetName)) {
                return sheet.getID();
            }
        }
        return null;
    }

    public void setLap(String guildId, int newLap) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setLap(newLap);
        guildRepository.save(guild);
    }

    public void addSpreadsheetId(String guildId, String spreadSheetId) throws IllegalArgumentException {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        if(guild == null){
            guild = new GuildEntity();
            guild.setGuildId(guildId);
        }
        guild.setSpreadsheetId(spreadSheetId);
        guildRepository.save(guild);
    }

    public void setGuildSheetId(String guildId, String sheetId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setSheetId(sheetId);
        guildRepository.save(guild);
    }

    public void setImagePreference(String guildId, GuildImagePreference preference) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setImagePreference(preference);
        guildRepository.save(guild);
    }

    public void setCurrentBossHealth(String guildId, int newHealth) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setCurrentHealth(newHealth);
        guildRepository.save(guild);
    }

    public int getBossPosition(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getBoss().getPosition();
    }

    public void setMessagesToDisplay(String guildId, int messagesToDisplay) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setMessagesToDisplay(messagesToDisplay);
        guildRepository.save(guild);
    }

    public int getMessagesToDisplay(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        if (guild.getMessagesToDisplay() == null) return 2;
        return guild.getMessagesToDisplay();
    }

    public List<Spreadsheet> getAdditionalSpreadsheets(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return new ArrayList<>() {{
            for (GuildSpreadsheetEntity gse : guild.getAdditionalSpreadsheets()) {
                add(new Spreadsheet(gse.getName(), gse.getSpreadsheetId()));
            }
        }};
    }

    public String getSpreadsheetNameFromID(String guildId, String spreadsheetId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        List<GuildSpreadsheetEntity> knownSheets = guildSpreadsheetRepository.getGuildSpreadsheetEntitiesByGuildEntity(guild);
        for (GuildSpreadsheetEntity gse : knownSheets) {
            if (gse.getSpreadsheetId().equals(spreadsheetId) || gse.getSpreadsheetId().substring(0, 20).equals(spreadsheetId)) {
                System.out.println("FOUND SPREADSHEET!");
                return gse.getName();
            }
        }
        System.out.println("getSpreadsheetNameFromID could not find spreadsheet matching ID: " + spreadsheetId);
        return null;
    }

    // TODO: This does not delete spreadsheets correctly
    public void removeAdditionalSpreadsheetBySmallID(String guildId, String smallSpreadsheetID) {
        System.out.println("Trying to remove spreadsheet with id: " + smallSpreadsheetID);
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        List<GuildSpreadsheetEntity> knownSheets = guild.getAdditionalSpreadsheets();
        System.out.println("Known sheets: " + knownSheets);
        for (GuildSpreadsheetEntity gse : knownSheets) {
            // This is actually the name not really the ID, a bit misleading but should work for now
            if (gse.getName().equals(smallSpreadsheetID)) {
                guild.getAdditionalSpreadsheets().remove(gse);
                guildRepository.save(guild);
                guildSpreadsheetRepository.delete(gse);
                System.out.println("REMOVED SUCCESSFULLY");
                return;
            }
        }
        System.out.println("FAILED TO REMOVE SPREADSHEET BY SMALL ID");
    }

    public void removeAdditionalSpreadsheetByID(String guildId, String spreadsheetId) {
        System.out.println("In remove spreadsheet");
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        List<GuildSpreadsheetEntity> knownSheets = guild.getAdditionalSpreadsheets();
        for (GuildSpreadsheetEntity gse : knownSheets) {
            System.out.println("LOOKING AT: " + gse.getId() + " COMPARING WITH: " + spreadsheetId);
            if (gse.getSpreadsheetId().equals(spreadsheetId)) {
                guildSpreadsheetRepository.delete(gse);
                System.out.println("Deleted spreadsheet from repository");
                return;
            }
        }
        System.out.println("Could not find a spreadsheet by the id: " + spreadsheetId);
    }

    public List<String> getSchedules(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        System.out.println("GuildService getSchedule looking for schedules: " + guild.getSchedules());
        if (guild.getSchedules() == null) return new ArrayList<>();
        System.out.println("GuildService getSchedule returning list: " + guild.getSchedules());
        return guild.getSchedules();
    }

    @Transactional
    public void setAskForDamage(String guildId, boolean flag) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setAskForDamage(flag);
        guildRepository.save(guild);
    }

    public boolean getAskForDamage(String guildId) {
        return guildRepository.getGuildEntityByGuildId(guildId).isAskForDamage();
    }

    @Transactional
    public void setUpdatesChannelId(String guildId, String id) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setUpdatesChannelId(id);
        guildRepository.save(guild);
    }

    public String getUpdatesChannelId(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        if (guild == null) {
            System.out.println("Guild not in the database tried to get updates channel id");
           return null;
        }
        return guildRepository.getGuildEntityByGuildId(guildId).getUpdatesChannelId();
    }

    @Transactional
    public void setUseThreads(String guildId, boolean flag) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setUseThreads(flag);
        guildRepository.save(guild);
    }

    public boolean getUseThreads(String guildId) {
        return guildRepository.getGuildEntityByGuildId(guildId).isUseThreads();
    }

    public List<String> getAllGuildIDs() {
        return new ArrayList<>() {{
            List<GuildEntity> entities = guildRepository.findAll();
            for (GuildEntity entity : entities) {
                add(entity.getGuildId());
            }
        }};
    }

    @Data
    public static class Spreadsheet {
        private final String name;
        private final String ID;
        private final String smallID;

        public Spreadsheet(String name, String ID) {
            this.name = name;
            this.ID = ID;
            if (ID.length() < 21) {
                System.out.println("ID somehow too small to be shortened, this is bad");
                this.smallID = ID;
            } else {
                this.smallID = ID.substring(0, 20);
            }
        }
    }
}
