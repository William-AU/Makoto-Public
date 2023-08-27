package bot.storage.repositories;

import bot.storage.models.MessageScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<MessageScheduleEntity, String> {
}
