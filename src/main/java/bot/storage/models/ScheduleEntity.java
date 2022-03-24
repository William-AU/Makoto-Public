package bot.storage.models;

import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Map;

@Data
@Entity
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private Integer currentLap;

    @ManyToMany
    // Maps the position to boss ID, positions 1-5 are for the current lap, positions 6-10 are for the next lap
    // BossEntity should probably be used here, but having it map correctly is a complete nightmare
    private Map<Integer, BossEntity> positionBossIdMap;

    private Integer expectedAttacks;

    private String channelId;

    private String messageId;

    @OneToOne(mappedBy = "schedule")
    private GuildEntity guild;
}
