package bot.utils;

import bot.services.GuildService;
import bot.services.SheetService;
import bot.storage.models.GuildSpreadsheetEntity;
import com.google.api.client.util.ArrayMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class CachedSpreadSheetUtils {
    private final GuildService guildService;
    private final SheetService sheetService;
    private Map<String, Map<String,List<String>>> cachedGuildIDSpreadSheetIDUserIDMap;

    @Autowired
    public CachedSpreadSheetUtils(GuildService guildService, SheetService sheetService) {
        this.guildService = guildService;
        this.sheetService = sheetService;
        this.cachedGuildIDSpreadSheetIDUserIDMap = new HashMap<>() {{
            for (String id : guildService.getAllGuildIDs()) {
                put(id, new HashMap<>());
            }
        }};
    }


    public boolean hasSingleSheet(String guildId) {
        return !guildService.hasAdditionalSpreadsheets(guildId);
    }

    public boolean userIsSignedUpForMultipleSpreadsheets(String guildId, String userId) {
        if (hasSingleSheet(guildId)) return false;
        List<String> activeIn = getSpreadsheetIDUserIsActiveIn(guildId, userId);
        System.out.println("User is signed up for these spreadsheets: " + activeIn);
        HashSet<String> activeSet = new HashSet<>(activeIn);
        return activeSet.size() > 1;

    }

    public List<String> getSpreadsheetIDUserIsActiveIn(String guildId, String userId) {
        List<String> result = new ArrayList<>();
        // First check the cache
        String mainSheetID = guildService.getSpreadSheetId(guildId);
        List<String> toCheck = new ArrayList<>() {{
            add(mainSheetID);
            if (cachedGuildIDSpreadSheetIDUserIDMap.get(guildId) != null) {
                addAll(cachedGuildIDSpreadSheetIDUserIDMap.get(guildId).keySet());
            }
        }};
        for (String id : toCheck) {
            if (cachedGuildIDSpreadSheetIDUserIDMap.get(guildId) == null) continue;
            List<String> userIDs = cachedGuildIDSpreadSheetIDUserIDMap.get(guildId).get(id);
            if (userIDs == null) continue;
            if (userIDs.contains(userId)) {
                result.add(id);
            }
        }
        // Technically this cache could still be out of date, but there is nothing we can do at that point
        if (result.size() > 0) return result;

        // If nothing is found in cache we are forced to do an expensive search
        List<String> userIDs = sheetService.getUserIDsFromSheet(guildService.getSpreadSheetId(guildId));
        for (String userIdSheet : userIDs) {
            if (userId.equals(userIdSheet)) {
                result.add(guildService.getSpreadSheetId(guildId));
            }
        }
        cachedGuildIDSpreadSheetIDUserIDMap.get(guildId).put(mainSheetID, userIDs);
        if (hasSingleSheet(guildId)) return result;
        for (GuildService.Spreadsheet sheet : guildService.getAdditionalSpreadsheets(guildId)) {
            List<String> users = sheetService.getUserIDsFromSheet(sheet.getID());
            for (String userIdSheet : users) {
                if (userId.equals(userIdSheet)) {
                    result.add(sheet.getID());
                }
            }
            cachedGuildIDSpreadSheetIDUserIDMap.get(guildId).put(sheet.getID(), users);
        }
        return result;
    }

    public List<ActionRow> getSpreadsheetButtons(String guildId, String buttonPrefix) {
        List<String> knownSpreadsheets = new ArrayList<>() {{
            add("Main");
            addAll(guildService.getSpreadsheetNames(guildId));
            add("Abort");
        }};
        return ButtonUtils.createGenericButtons(buttonPrefix, false, true, knownSpreadsheets.toArray(new String[0]));
    }

    public List<String> getSpreadsheetNamesForUser(String guildId, String userId) {
        List<GuildSpreadsheetEntity> knownSpreadsheets = guildService.getAllSpreadSheets(guildId);
        // Manually add main because it is *probably* always there
        List<String> result = new ArrayList<>();
        String mainSheetID = guildService.getSpreadSheetId(guildId);
        List<String> mainSheetIDs = sheetService.getUserIDsFromSheet(mainSheetID);
        if (mainSheetIDs.contains(userId)) {
            result.add("Main");
        }
        for (GuildSpreadsheetEntity spreadsheet : knownSpreadsheets) {
            List<String> IDs = sheetService.getUserIDsFromSheet(spreadsheet.getSpreadsheetId());
            if (IDs.contains(userId)) result.add(spreadsheet.getName());
        }
        return result;
    }

    public boolean isSignedUpForMainSheet(String guildId, String userId) {
        // This call does not need to be cached because it is just a database lookup
        String mainSheetID = guildService.getSpreadSheetId(guildId);
        List<String> cachedMembers = cachedGuildIDSpreadSheetIDUserIDMap.get(guildId).get(mainSheetID);
        if (cachedMembers == null || cachedMembers.size() == 0) {
            List<String> newIDs = sheetService.getUserIDsFromSheet(mainSheetID);
            cachedGuildIDSpreadSheetIDUserIDMap.get(guildId).put(mainSheetID, newIDs);
            return newIDs.contains(userId);
        }
        return cachedMembers.contains(userId);
    }

    public void addMember(String guildID, String spreadsheetID, String userID) {
        cachedGuildIDSpreadSheetIDUserIDMap.get(guildID).get(spreadsheetID).add(userID);
    }

    public void removeMember(String guildID, String spreadsheetID, String userID) {
        cachedGuildIDSpreadSheetIDUserIDMap.get(guildID).get(spreadsheetID).remove(userID);
    }

    public void addOrOverwriteSpreadSheet(String guildID, String spreadsheetID, List<String> userIDs) {
        cachedGuildIDSpreadSheetIDUserIDMap.get(guildID).put(spreadsheetID, userIDs);
    }

    public void refreshCache(String guildId) {
        Map<String, List<String>> spreadSheetIDUserIDMap = cachedGuildIDSpreadSheetIDUserIDMap.get(guildId);
        List<String> spreadSheetIds = new ArrayList<>() {{
            add(guildService.getSpreadSheetId(guildId));
            for (GuildSpreadsheetEntity sheet : guildService.getAllSpreadSheets(guildId)) {
                add(sheet.getSpreadsheetId());
            }
        }};
        for (String spreadsheetID : spreadSheetIds) {
            if (spreadsheetID != null) {
                try {
                    List<String> userIDs = sheetService.getUserIDsFromSheet(spreadsheetID);
                    spreadSheetIDUserIDMap.put(spreadsheetID, userIDs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.cachedGuildIDSpreadSheetIDUserIDMap.put(guildId, spreadSheetIDUserIDMap);
    }
}
