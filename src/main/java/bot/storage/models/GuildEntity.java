package bot.storage.models;

import org.jetbrains.annotations.Async;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.List;

@Entity
public class GuildEntity {
    @Id
    @GeneratedValue
    private Integer id;

    @Nonnull
    private String  guildId;

    @Nonnull
    private String spreadsheetId;

    private String sheetId;

    private String bossChannelId;

    private String bossMessageId;

    @OneToOne
    private BossEntity boss;

    private Integer currentHealth;

    private Integer lap;

    @OneToMany
    private List<ScheduleEntity> schedules;
}
