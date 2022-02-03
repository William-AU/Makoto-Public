package bot.services;

import bot.storage.models.GuildEntity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/*
Only has placeholder implementation for now since I don't have the required tokens and don't know how the API works
TODO: Since we don't have the same streamlike behaviour anymore remember to add exceptions to all methods here,
 otherwise we will react negative to some incorrect inputs. Parsing spreadsheetId is NOT currently handled in
 RegisterCommand and would need an exception to be thrown here

 Note: JDA and D4J swap User and Member, in JDA a User is a specific member of a channel, where in D4J this is called a Member.
 A member in JDA is the main profile shared between all servers
 */
public class SheetService {
    public void setupSheet(String guildId, String spreadsheetId, List<User> users) {
        System.out.println("Method setupSheet called");
    }

    public void addBattle(GuildEntity guild, String userId, String damage) {
        System.out.println("Method addBattle was called with guild: " + guild + ", userId: " + userId + ", damage: " + damage);
    }

    public void redoBattle(GuildEntity guild, String userId, String damage) {
        System.out.println("Method redoBattle was called with guild: " + guild + ", userId: " + userId + ", damage: " + damage);
    }

    public void addCarryOver(GuildEntity guild, String userId, String damage) {
        System.out.println("Method addCarryOver was called with guild: " + guild + ", userId: " + userId + ", damage: " + damage);
    }

    public String verifyBattle(String damage) {
        System.out.println("Method verifyBattle was called with damage: " + damage);
        return "";
    }

    public void removeMembersFromSheet(List<String> userIds, String spreadsheetId) {

    }

    public  void addUsersFromSheet(List<User> members, String spreadsheetId) {

    }
}
