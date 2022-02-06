package bot.services;

import bot.exceptions.ScheduleDoesNotExistException;
import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.models.ScheduleEntity;
import bot.storage.repositories.GuildRepository;
import bot.storage.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public boolean hasActiveScheduleForBoss(String guildId, int bossId) {
        return getScheduleByGuildIdAndBossId(guildId, bossId) != null;
    }

    public ScheduleEntity getScheduleByGuildIdAndBossId(String guildId, int bossId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        for (ScheduleEntity schedule : guild.getSchedules()) {
            if (schedule.getBoss().getId().equals(bossId)) return schedule;
        }
        return null;
    }

    public void setChannelId(String guildId, int bossId, String channelId) throws ScheduleDoesNotExistException {
        ScheduleEntity schedule = getScheduleByGuildIdAndBossId(guildId, bossId);
        if (schedule == null) throw new ScheduleDoesNotExistException();
        schedule.setChannelId(channelId);
        scheduleRepository.save(schedule);
    }

    public void setMessageId(String guildId, int bossId, String messageId) throws ScheduleDoesNotExistException {
        ScheduleEntity schedule = getScheduleByGuildIdAndBossId(guildId, bossId);
        if (schedule == null) throw new ScheduleDoesNotExistException();
        schedule.setMessageId(messageId);
        scheduleRepository.save(schedule);
    }

    public void createScheduleForGuildAndBoss(String guildId, int bossId) {
        ScheduleEntity schedule = new ScheduleEntity();
        BossEntity boss = bossService.getBossFromId(bossId + "");
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        schedule.setBoss(boss);
        guild.addSchedule(schedule);
        schedule.setGuild(guild);
        scheduleRepository.save(schedule);
        guildRepository.save(guild);
    }
}
