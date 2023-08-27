package bot.storage.models;

import lombok.Data;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.List;


/**
 * Class for keeping track of a single boss and lap for a specific schedule
 */
@Data
@Entity
public class ScheduleEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String scheduleId;
    private String guildId;
    private int lap;
    private int boss;
    private String attackerId;
    private String attackerNick;
    private int expectedDamage;
    private boolean isOvertime;



    public ScheduleEntryEntity() {

    }
}
