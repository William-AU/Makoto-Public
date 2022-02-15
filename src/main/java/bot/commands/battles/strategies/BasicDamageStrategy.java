package bot.commands.battles.strategies;

import bot.commands.scheduling.ScheduleStrategy;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.storage.models.GuildEntity;
import bot.commands.tracking.TrackingStrategy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BasicDamageStrategy implements DamageStrategy {
    // FIXME: Responsibility bloat
    private final GuildService guildService;
    private final SheetService sheetService;
    private final BossService bossService;
    private final TrackingStrategy trackingStrategy;
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public BasicDamageStrategy(GuildService guildService, SheetService sheetService, BossService bossService, TrackingStrategy trackingStrategy, ScheduleStrategy scheduleStrategy) {
        this.guildService = guildService;
        this.sheetService = sheetService;
        this.bossService = bossService;
        this.trackingStrategy = trackingStrategy;
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void addBattle(Guild guild, String userId, String damage, JDA jda) {
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.addBattle(guildEntity, userId, damage);
        bossService.takeDamage(guild.getId(), Integer.parseInt(damage));
        trackingStrategy.updateData(jda, guild.getId());
        scheduleStrategy.updateSchedule(jda, guild.getId(), guildEntity.getBoss().getPosition());
    }

    @Override
    public void addCarryover(Guild guild, String userId, String damage, JDA jda) {
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.addCarryOver(guildEntity, userId, damage);
        bossService.takeDamage(guild.getId(), Integer.parseInt(damage));
        trackingStrategy.updateData(jda, guild.getId());
        scheduleStrategy.updateSchedule(jda, guild.getId(), guildEntity.getBoss().getPosition());
    }

    @Override
    public void redoBattle(Guild guild, String userId, String damage, JDA jda) {
        GuildEntity guildEntity = guildService.getGuild(guild.getId());
        sheetService.redoBattle(guildEntity, userId, damage);
        // TODO: figure out how to update the damage. Not sure how we can get the previous damage done cleanly,
        //  since that needs to be reverted before this can apply
        trackingStrategy.updateData(jda, guild.getId());
        scheduleStrategy.updateSchedule(jda, guild.getId(), guildEntity.getBoss().getPosition());
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
        return content[2].startsWith("<@!");
    }
}
