package bot.services;

import bot.common.CBUtils;
import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.repositories.BossRepository;
import bot.storage.repositories.GuildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        int newPos = oldPos + 1;
        int stage = oldBoss.getStage();
        if (oldPos == 5) {
            int currentLap = guild.getLap();
            stage = CBUtils.getStageFromLap(currentLap + 1);
            guild.setLap(currentLap + 1);
            newPos = 1;
        }
        BossEntity newBoss = bossRepository.findBossEntityByStageAndPosition(stage, newPos);
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

    /**
     * Returns the id of the next boss at the given position, will only look forward from the current boss,
     * if the next boss is on a new stage, it will return the boss of the next stage correctly
     * @param guildId id of the guild
     * @param position position of the boss
     * @return id of next boss at the given position
     */
    public int getBossIdFromBossPosition(String guildId, int position) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        BossEntity currentBoss = guild.getBoss();
        int currentPosition = currentBoss.getPosition();
        int currentStage = currentBoss.getStage();
        if (position == currentPosition) return currentBoss.getId();
        if (position > currentPosition) return bossRepository.findBossEntityByStageAndPosition(currentStage, position).getId();
        if (currentStage + 1 == 4 || currentStage + 1 == 11) return bossRepository.findBossEntityByStageAndPosition(currentStage + 1, position).getId();
        return bossRepository.findBossEntityByStageAndPosition(currentStage, position).getId();
    }

    public BossEntity getBossFromId(Integer bossId) {
        return bossRepository.findBossEntityById(bossId);
    }

    public List<BossEntity> getBossesBetweenPositionAndStage(Integer minPos, Integer maxPos, Integer minStage, Integer maxStage) {
        return bossRepository.findBossEntitiesByPositionBetweenAndStageBetween(minPos, maxPos, minStage, maxStage);
    }

    public Integer getPositionFromBossName(String bossName) {
        return bossRepository.findBossEntityByNameAndStage(bossName, 1).getPosition();
    }
}
