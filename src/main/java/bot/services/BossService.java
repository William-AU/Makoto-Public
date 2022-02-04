package bot.services;

import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.repositories.BossRepository;
import bot.storage.repositories.GuildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BossService {
    private final GuildRepository guildRepository;
    private final BossRepository bossRepository;

    @Autowired
    public BossService(GuildRepository guildRepository, BossRepository bossRepository) {
        this.guildRepository = guildRepository;
        this.bossRepository = bossRepository;
    }

    public void initNewBoss(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        BossEntity newBoss = bossRepository.findBossEntityByStageAndPosition(1, 1);
        guild.setBoss(newBoss);
        guild.setCurrentHealth(newBoss.getTotalHealth());
        guild.setLap(1);
        guildRepository.save(guild);
    }

    public void setNextBoss(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        BossEntity oldBoss = guild.getBoss();
        if (oldBoss == null) {
            initNewBoss(guildId);
            return;
        }
        int oldPos = oldBoss.getPosition();
        int oldStage = oldBoss.getStage();
        BossEntity newBoss;
        if (oldPos == 5) {
            int currentLap = guild.getLap();
            if (currentLap + 1 == 4 || currentLap + 1 == 11) {
                newBoss = bossRepository.findBossEntityByStageAndPosition(oldStage + 1, 1);
            }
            newBoss = bossRepository.findBossEntityByStageAndPosition(oldStage, 1);
        }
        else {
            newBoss = bossRepository.findBossEntityByStageAndPosition(oldStage, oldPos + 1);
        }
        guild.setBoss(newBoss);
        guild.setCurrentHealth(newBoss.getTotalHealth());
        guildRepository.save(guild);
    }

    public void takeDamage(String guildId, int damage) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        int oldHealth = guild.getCurrentHealth();
        if (oldHealth - damage < 1) {
            int carryOver = damage - oldHealth;
            setNextBoss(guildId);
            // Reload here because previous call will cause updates to the guild table
            // There is a way to get a reference to an object instead of just the object (think it's get vs find)
            // but this should work for now
            guild = guildRepository.getGuildEntityByGuildId(guildId);
            guild.setCurrentHealth(guild.getCurrentHealth() - carryOver);
        }
        else {
            guild.setCurrentHealth(oldHealth - damage);
        }
        guildRepository.save(guild);
    }
}
