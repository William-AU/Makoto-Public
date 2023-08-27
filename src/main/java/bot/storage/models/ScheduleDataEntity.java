package bot.storage.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class ScheduleDataEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String guildId;
    private String name;
    private int currentLap;
    private int currentBossPos;
    // Maps in databases are made by the devil, we do this instead
    private int expected_1 = -1;
    private int expected_2 = -1;
    private int expected_3 = -1;
    private int expected_4 = -1;
    private int expected_5 = -1;
}
