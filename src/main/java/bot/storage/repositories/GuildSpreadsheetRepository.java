package bot.storage.repositories;

import bot.storage.models.GuildEntity;
import bot.storage.models.GuildSpreadsheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuildSpreadsheetRepository extends JpaRepository<GuildSpreadsheetEntity, Integer> {
    GuildSpreadsheetEntity getGuildSpreadsheetEntityByGuildEntityAndName(GuildEntity entity, String name);
    List<GuildSpreadsheetEntity> getGuildSpreadsheetEntitiesByGuildEntity(GuildEntity entity);
}
