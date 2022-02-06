package bot.storage.models;

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

    @OneToMany(mappedBy = "guild", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules;

    public void addSchedule(ScheduleEntity schedule) {
        schedules.add(schedule);
    }

    public GuildEntity() {

    }
}
