package bot.storage.models;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BossEntity {
    @Id
    private Integer id;

    private String name;

    private Integer stage;

    private Integer totalHealth;

    private Integer position;
}
