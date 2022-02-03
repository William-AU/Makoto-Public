package bot.services;

import bot.storage.models.BossEntity;
import bot.storage.models.GuildEntity;
import bot.storage.repositories.BossRepository;
import bot.storage.repositories.GuildRepository;
import bot.storage.repositories.ScheduleRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuildService {
    private final GuildRepository guildRepository;
    private final BossRepository bossRepository;
    private final ScheduleRepository scheduleRepository;

    @Autowired
    public GuildService(GuildRepository guildRepository, BossRepository bossRepository, ScheduleRepository scheduleRepository) {
        this.guildRepository = guildRepository;
        this.bossRepository = bossRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public boolean hasActiveBos(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getBoss() == null;
    }

    public GuildEntity getGuild(String guildId) {
        return guildRepository.getGuildEntityByGuildId(guildId);
    }

    public void setBossTrackerChannel(String guildId, String channelId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setBossChannelId(channelId);
        guildRepository.save(guild);
    }

    public void setBossTrackerMessage(String guildId, String messageId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setBossMessageId(messageId);
        guildRepository.save(guild);
    }

    public void setCurrentHealth(String guildId, int newHealth) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setCurrentHealth(newHealth);
        guildRepository.save(guild);
    }

    public String getSpreadSheetId(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getSpreadsheetId();
    }

    public void addSpreadsheetId(String guildId, String spreadSheetId) throws IllegalArgumentException {
        if (!verifySpreadsheetId(spreadSheetId)) throw new IllegalArgumentException();
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setSpreadsheetId(spreadSheetId);
        guildRepository.save(guild);
    }

    private boolean verifySpreadsheetId(String spreadsheetId) {
        return spreadsheetId.contains("/"); // Very simple check but it's what we had before
    }
}
