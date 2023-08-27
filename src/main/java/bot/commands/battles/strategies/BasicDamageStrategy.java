package bot.commands.battles.strategies;

import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.storage.models.GuildEntity;
import bot.commands.tracking.TrackingStrategy;
import bot.utils.ButtonUtils;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BasicDamageStrategy implements DamageStrategy {
    // FIXME: Responsibility bloat
    private final GuildService guildService;
    private final SheetService sheetService;
    private final BossService bossService;
    private final TrackingStrategy trackingStrategy;

    @Autowired
    public BasicDamageStrategy(GuildService guildService, SheetService sheetService, BossService bossService, TrackingStrategy trackingStrategy) {
        this.guildService = guildService;
        this.sheetService = sheetService;
        this.bossService = bossService;
        this.trackingStrategy = trackingStrategy;
    }


    private void addToSchedule(Guild guild, String userId, String damage, JDA jda, GuildEntity guildEntity) {

    }



    @Override
    public void addBattle(Guild guild, String userId, String damage, JDA jda) {
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.addBattle(guildEntity, userId, damage);
        addToSchedule(guild, userId, damage, jda, guildEntity);
    }

    @Override
    public void addBattle(String userId, String spreadsheetId, String damage, JDA jda) {
        sheetService.addBattleForSheet(spreadsheetId, userId, damage);
        // TODO: Fix multiple schedules!
    }

    @Override
    public void addCarryover(Guild guild, String userId, String damage, JDA jda) {
        boolean hasActiveBoss = guildService.hasActiveBos(guild.getId());
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.addCarryOver(guildEntity, userId, damage);
        if (hasActiveBoss) {
            boolean bossDead = bossService.takeDamage(guild.getId(), Integer.parseInt(damage));
            addToSchedule(guild, userId, damage, jda, guildEntity);
        }
    }

    @Override
    public void addCarryover(String userId, String spreadsheetId, String damage, JDA jda) {
        //System.out.println("Adding carryover for sheetID: " + spreadsheetId);
        sheetService.addCarryoverForSheet(spreadsheetId, userId, damage);
    }

    @Override
    public void redoBattle(Guild guild, String userId, String damage, JDA jda) {
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.redoBattle(guildEntity, userId, damage);
        // TODO: figure out how to update the damage. Not sure how we can get the previous damage done cleanly,
        //  since that needs to be reverted before this can apply
        if (guildEntity.getSchedule() != null) {
            //trackingStrategy.updateData(jda, guild.getId(), false); // We don't mess with bosses when redoing damage, it's just too messy and it's better to have admins manually fix it
            //scheduleStrategy.updateSchedule(jda, guild.getId(), false);
        }
    }

    @Override
    public void redoBattle(String userId, String spreadsheetId, String damage, JDA jda) {
        sheetService.redoBattleForSheet(spreadsheetId, userId, damage);
        // TODO: Fix scheduling for multiple guilds
    }

    /**
     * Validates the message for addBattle and addCarryover
     * @param message the message with the damage
     * @return true if validation was successful, false otherwise
     */
    @Override
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
    @Override
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
        return true;
        //return content[2].startsWith("<@!");
    }
}
