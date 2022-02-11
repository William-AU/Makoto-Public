package bot.services;

import bot.configuration.GuildImagePreference;
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

    @Autowired
    public GuildService(GuildRepository guildRepository, BossRepository bossRepository, ScheduleRepository scheduleRepository) {
        this.guildRepository = guildRepository;
    }

    public boolean hasActiveBos(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getBoss() != null;
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

    public String getSpreadSheetId(String guildId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        return guild.getSpreadsheetId();
    }

    public void addSpreadsheetId(String guildId, String spreadSheetId) throws IllegalArgumentException {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        if(guild == null){
            guild = new GuildEntity();
            guild.setGuildId(guildId);
        }
        guild.setSpreadsheetId(spreadSheetId);
        guildRepository.save(guild);
    }

    public void setGuildSheetId(String guildId, String sheetId) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setSheetId(sheetId);
        guildRepository.save(guild);
    }

    public void setImagePreference(String guildId, GuildImagePreference preference) {
        GuildEntity guild = guildRepository.getGuildEntityByGuildId(guildId);
        guild.setImagePreference(preference);
        guildRepository.save(guild);
    }
}
