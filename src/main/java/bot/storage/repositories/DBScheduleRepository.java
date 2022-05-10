package bot.storage.repositories;

import bot.storage.models.DBScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DBScheduleRepository extends JpaRepository<DBScheduleEntity, Integer> {
    List<DBScheduleEntity> getAllByGuildId(String guildId);
    List<DBScheduleEntity> getDBScheduleEntitiesByGuildIdAndLapAndPos(String guildId, int lap, int pos);

    @Query(value = "SELECT * FROM dbschedule_entity s WHERE s.guild_id = ?1 AND s.lap = ?2 AND s.pos = ?3 AND s.user_id = ?4",
            nativeQuery = true)
    DBScheduleEntity getScheduleByLapAndPositionAndUserId(String guildId, int lap, int position, String userId);

    @Query(value = "DELETE FROM dbschedule_entity s WHERE s.guild_id = ?1 AND s.lap = ?2 AND s.pos = ?3 AND s.user_id = ?4",
            nativeQuery = true)
    void deleteDBScheduleEntityByLapAndPositionAndUserId(String guildId, int lap, int position, String userId);

    void deleteDBScheduleEntitiesByGuildId(String guildId);
}
