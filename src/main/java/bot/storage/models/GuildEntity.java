package bot.storage.models;

import bot.configuration.GuildImagePreference;
import lombok.Data;
import org.jetbrains.annotations.Async;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class GuildEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
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

    @OneToOne
    private ScheduleEntity schedule;

    @Column(columnDefinition = "varchar(16) default 'NONE'")
    @Enumerated(EnumType.STRING)
    private GuildImagePreference imagePreference;

    public GuildEntity() {

    }
}
