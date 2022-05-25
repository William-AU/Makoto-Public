package bot.storage.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class ExpectedAttacksEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private GuildEntity guildEntity;

    private int stage;
    private int pos;
    private int expectedAttacks = 0;
}
