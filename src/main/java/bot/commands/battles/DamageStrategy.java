package bot.commands.battles;

import bot.services.GuildService;
import bot.services.SheetService;
import bot.storage.models.GuildEntity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class DamageStrategy {
    private final GuildService guildService;
    private final SheetService sheetService;

    @Autowired
    public DamageStrategy(GuildService guildService, SheetService sheetService) {
        this.guildService = guildService;
        this.sheetService = sheetService;
    }

    public void addBattle(Guild guild, String userId, String damage) {
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.addBattle(guildEntity, userId, damage);
    }

    public void addCarryover(Guild guild, String userId, String damage) {
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.addCarryOver(guildEntity, userId, damage);
    }

    /**
     * Validates the message for addBattle and addCarryover
     * @param message the message with the damage
     * @return true if validation was successful, false otherwise
     */
    public boolean validatePersonal(Message message) {
        String[] content = message.getContentRaw().split(" ");
        if (content.length != 2) return false;
        try {
            int damage = Integer.parseInt(content[1]);
            if (damage < 0) return false;
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Validates the message for addBattleFor and addCarryoverFor
     * @param message the message with the damage and user
     * @return true if validation was successful, false otherwise
     */
    public boolean validateForOther(Message message) {
        String[] content = message.getContentRaw().split(" ");
        if (content.length != 3) return false;
        try {
            int damage = Integer.parseInt(content[1]);
            if (damage < 0) return false;
        } catch (NumberFormatException ignored) {
            return false;
        }
        // Mentions that refer to users look like <@!12345678910> where non mentions show up as @1234678910
        return content[2].startsWith("<@!");
    }
}
