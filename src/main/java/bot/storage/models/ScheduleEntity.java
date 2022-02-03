package bot.storage.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class ScheduleEntity {
    @Id
    private Integer id;

    @OneToOne
    private BossEntity boss;

    private Integer expectedAttacks;

    private String channelId;

    private String messageId;
}
