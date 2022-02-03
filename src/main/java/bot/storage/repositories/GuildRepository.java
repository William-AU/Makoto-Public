package bot.storage.repositories;

import bot.storage.models.GuildEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildRepository extends JpaRepository<GuildEntity, String> {
    GuildEntity getGuildEntityByGuildId(String guildId);
}
