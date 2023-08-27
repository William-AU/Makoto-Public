package bot.storage.repositories;

import bot.storage.models.ExpectedAttacksEntity;
import org.springframework.data.repository.CrudRepository;

public interface ExpectedAttacksRepository extends CrudRepository<ExpectedAttacksEntity, Integer> {
}
