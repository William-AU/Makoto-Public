package bot.services;

import bot.storage.models.GuildEntity;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/*
Only has placeholder implementation for now since I don't have the required tokens and don't know how the API works
 */
public class SheetService {
    public void setupSheet(String guildId, String spreadsheetId, List<Member> users) {
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

    public  void addMembersFromSheet(List<Member> members, String spreadsheetId) {

    }
}
