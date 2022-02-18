package bot.storage.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Map;

@Data
@Entity
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private Integer currentLap;

    @ElementCollection
    // Maps the position to boss ID, positions 1-5 are for the current lap, positions 6-10 are for the next lap
    // BossEntity should probably be used here, but having it map correctly is a complete nightmare
    private Map<Integer, Integer> positionBossIdMap;

    private Integer expectedAttacks;

    private String channelId;

    private String messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private GuildEntity guild;
}
