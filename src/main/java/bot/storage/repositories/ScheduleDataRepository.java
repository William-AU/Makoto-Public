package bot.storage.repositories;

import bot.storage.models.ScheduleDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleDataRepository extends JpaRepository<ScheduleDataEntity, Integer> {
    ScheduleDataEntity getScheduleDataEntityByGuildIdAndName(String guildId, String name);
    List<ScheduleDataEntity> getScheduleDataEntitiesByGuildId(String GuildId);
    void deleteByGuildIdAndName(String guildId, String name);
}
