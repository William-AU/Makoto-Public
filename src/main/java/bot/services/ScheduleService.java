package bot.services;

import bot.common.CBUtils;
import bot.exceptions.ScheduleDoesNotExistException;
import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.models.ScheduleEntity;
import bot.storage.repositories.GuildRepository;
import bot.storage.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final GuildRepository guildRepository;
    private final BossService bossService;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, GuildRepository guildRepository, BossService bossService) {
        this.scheduleRepository = scheduleRepository;
        this.guildRepository = guildRepository;
        this.bossService = bossService;
    }

    public boolean hasActiveScheduleForBoss(String guildId) {
        return getScheduleByGuildId(guildId) != null;
    }

    public ScheduleEntity getScheduleByGuildId(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getSchedule();
    }

    public void setChannelId(String guildId, String channelId) throws ScheduleDoesNotExistException {
        ScheduleEntity schedule = getScheduleByGuildId(guildId);
        if (schedule == null) throw new ScheduleDoesNotExistException();
        schedule.setChannelId(channelId);
        scheduleRepository.save(schedule);
    }

    public void setMessageId(String guildId, String messageId) throws ScheduleDoesNotExistException {
        ScheduleEntity schedule = getScheduleByGuildId(guildId);
        if (schedule == null) throw new ScheduleDoesNotExistException();
        schedule.setMessageId(messageId);
        scheduleRepository.save(schedule);
    }

    public void createScheduleForGuild(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        ScheduleEntity schedule = new ScheduleEntity();
        int currentStage = CBUtils.getStageFromLap(guild.getLap());
        int nextStage = CBUtils.getStageFromLap(guild.getLap() + 1);
        Map<Integer, BossEntity> positionIdMap = new HashMap<>();
        List<BossEntity> bossEntities = bossService.getBossesBetweenPositionAndStage(1, 5, currentStage, nextStage);
        for (BossEntity boss : bossEntities) {
            if (boss.getStage() == currentStage) {
                positionIdMap.put(boss.getPosition(), boss);
            }
            if (boss.getStage() == nextStage) {
                positionIdMap.put(boss.getPosition() + 5, boss);
            }
        }
        schedule.setGuild(guild);
        schedule.setCurrentLap(guild.getLap());
        schedule.setPositionBossIdMap(positionIdMap);
        scheduleRepository.save(schedule);
        guild.setSchedule(schedule);
        guildRepository.save(guild);
    }


    public void deleteSchedule(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        ScheduleEntity oldSchedule = guild.getSchedule();
        if (oldSchedule == null) return;
        guild.setSchedule(null);
        guildRepository.save(guild);
        scheduleRepository.delete(oldSchedule);
    }
}
