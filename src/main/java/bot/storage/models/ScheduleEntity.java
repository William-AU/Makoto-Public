package bot.storage.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

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
