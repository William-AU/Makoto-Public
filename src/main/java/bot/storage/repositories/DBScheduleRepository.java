package bot.storage.repositories;

import bot.storage.models.DBScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface DBScheduleRepository extends JpaRepository<DBScheduleEntity, Integer> {
    List<DBScheduleEntity> getAllByGuildId(String guildId);
    List<DBScheduleEntity> getDBScheduleEntitiesByGuildIdAndLapAndPos(String guildId, int lap, int pos);

    @Query(value = "SELECT * FROM dbschedule_entity WHERE guild_id = ?1 AND lap = ?2 AND pos = ?3 AND user_id = ?4",
            nativeQuery = true)
    DBScheduleEntity getScheduleByLapAndPositionAndUserId(String guildId, int lap, int position, String userId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM dbschedule_entity WHERE guild_id = ?1 AND lap = ?2 AND pos = ?3 AND user_id = ?4",
            nativeQuery = true)
    int deleteDBScheduleEntityByLapAndPositionAndUserId(String guildId, int lap, int position, String userId);

    @Transactional
    void deleteDBScheduleEntitiesByGuildId(String guildId);
}
