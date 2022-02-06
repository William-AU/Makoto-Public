package bot.storage.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class ScheduleEntity {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    private BossEntity boss;

    private Integer expectedAttacks;

    private String channelId;

    private String messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private GuildEntity guild;
}
