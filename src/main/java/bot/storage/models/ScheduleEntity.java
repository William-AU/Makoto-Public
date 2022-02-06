package bot.storage.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    @OneToOne
    private BossEntity boss;

    private Integer expectedAttacks;

    private String channelId;

    private String messageId;
}
