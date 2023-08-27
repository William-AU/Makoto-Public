package bot.services;

import bot.storage.models.GuildEntity;
import bot.storage.repositories.SheetRepository;
import com.google.api.services.sheets.v4.model.SheetProperties;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
/*
TODO: Since we don't have the same streamlike behaviour anymore remember to add exceptions to all methods here,
 otherwise we will react negative to some incorrect inputs. Parsing spreadsheetId is NOT currently handled in
 RegisterCommand and would need an exception to be thrown here

 Note: JDA and D4J swap User and Member, in JDA a User is a specific member of a channel, where in D4J this is called a Member.
 A member in JDA is the main profile shared between all servers
 */
public class SheetService {
    private final SheetRepository sheetRepository;
    private final GuildService guildService;

    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    @Autowired
    public SheetService(SheetRepository sheetRepository, GuildService guildService) {
        this.sheetRepository = sheetRepository;
        this.guildService = guildService;
    }

    public void addBattleForSheet(String spreadsheetId, String userId, String damage) {
        String verifiedDamage = verifyBattle(damage);
        sheetRepository.addBattle(userId, spreadsheetId, damage);
    }

    public void redoBattleForSheet(String spreadsheetId, String userId, String damage) {
        String verifiedDamage = verifyBattle(damage);
        sheetRepository.redoBattle(userId, spreadsheetId, damage);
    }

    public void addCarryoverForSheet(String spreadsheetId, String userId, String damage) {
        //System.out.println("SheetService.addCarryoverForSheet sheetID: " + spreadsheetId);
        String verifiedDamage = verifyBattle(damage);
        sheetRepository.addCarryover(userId, spreadsheetId, damage);
    }

    public void setupSheet(String guildId, String spreadsheetId, List<User> users) {
        SheetProperties properties = sheetRepository.setupBaseSpreadsheet(spreadsheetId);
        guildService.setGuildSheetId(guildId, properties.getSheetId().toString());
        sheetRepository.setUsers(users, spreadsheetId);
    }

    public void addBattle(GuildEntity guild, String userId, String damage) {
        String verifiedDamage = verifyBattle(damage); // this could just be a void method, also TODO: actually catch the exception
        sheetRepository.addBattle(userId, guild, verifiedDamage);
    }

    public void redoBattle(GuildEntity guild, String userId, String damage) {
        String verifiedDamage = verifyBattle(damage); // Same as above
        sheetRepository.redoBattle(userId, guild, verifiedDamage);
    }

    public void addCarryOver(GuildEntity guild, String userId, String damage) {
        String verifiedDamage = verifyBattle(damage); // Same as above
        sheetRepository.addCarryover(userId, guild, verifiedDamage);
    }

    public String verifyBattle(String damage) {
        boolean matches = pattern.matcher(damage).matches();
        if (!matches) throw new IllegalArgumentException();
        return damage;
    }

    public void removeMembersFromSheet(List<String> userIds, String spreadsheetId) {
        sheetRepository.removeMembersFromSheet(userIds, spreadsheetId);
    }

    public  void addUsersToSheet(List<User> members, String spreadsheetId) {
        sheetRepository.addMembersToSheet(members, spreadsheetId);
    }

    public List<String> getUserIDsFromSheet(String spreadsheetId) {
        System.out.println("GETTING IDs FROM SHEET: " + spreadsheetId);
        return sheetRepository.getAllMemberIDs(spreadsheetId);
    }
}
