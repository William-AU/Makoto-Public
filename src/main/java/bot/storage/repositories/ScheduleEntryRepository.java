package bot.storage.repositories;

import bot.storage.models.ScheduleEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntryEntity, Integer> {
    ScheduleEntryEntity getByLapAndBossAndAttackerIdAndGuildIdAndScheduleId(int lap, int boss, String attackerID, String guildId, String scheduleId);
    List<ScheduleEntryEntity> getAllByLapAndBossAndGuildIdAndScheduleId(int lap, int boss, String guildId, String scheduleId);


    //@Query(value = "SELECT COUNT (DISTINCT scheduleid) as schedules FROM (SELECT * FROM schedule_entry_entity WHERE guildid = ?1) as something", nativeQuery = true)
    @Query(value = "SELECT COUNT(distinct schedule_id) from schedule_entry_entity where guild_id = ?1", nativeQuery = true)
    int getNumberOfSchedulesForGuild(String guildId);

    void deleteScheduleEntryEntityByGuildIdAndScheduleIdAndAttackerIdAndLapAndBoss(String guildId, String scheduleId, String attackerId, int lap, int boss);
    void deleteScheduleEntryEntitiesByGuildIdAndScheduleId(String guildId, String scheduleId);

    List<ScheduleEntryEntity> findScheduleEntryEntitiesByGuildId(String guildId);


}
