package bot.storage.repositories;

import bot.storage.models.BossEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface BossRepository extends JpaRepository<BossEntity, String> {
    BossEntity findBossEntityByStageAndPosition(Integer stage, Integer position);
    BossEntity findBossEntityById(Integer bossId);
}
