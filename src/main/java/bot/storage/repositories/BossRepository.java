package bot.storage.repositories;

import bot.storage.models.BossEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BossRepository extends JpaRepository<BossEntity, String> {
    BossEntity findBossEntityByStageAndPosition(Integer stage, Integer position);
    BossEntity findBossEntityById(Integer bossId);
    List<BossEntity> findBossEntitiesByPositionBetweenAndStageBetween(Integer minPos, Integer maxPos, Integer minStage, Integer maxStage);
    BossEntity findBossEntityByNameAndStage(String name, Integer stage);
}
