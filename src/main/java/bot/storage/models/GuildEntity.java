package bot.storage.models;

import bot.config.GuildImagePreference;
import lombok.Data;

import javax.annotation.Nonnull;
import javax.persistence.*;
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

    private Integer messagesToDisplay;

    @OneToMany(cascade = CascadeType.ALL)
    private List<GuildSpreadsheetEntity> additionalSpreadsheets;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ScheduleEntryEntity> scheduleEntities;

    @ManyToMany
    private List<ExpectedAttacksEntity> expectedAttacks;

    @ElementCollection
    private List<String> schedules;

    private boolean enableOT = false;

    private boolean askForDamage = true;

    private boolean useThreads = false;

    private String updatesChannelId;

    /**
     * Old way to handle schedules using discord messages, kept in the DB as backwards compatability
     * @deprecated do not access schedules from the guild directly, use guildID instead
     */
    @Deprecated
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private MessageScheduleEntity schedule;

    @Enumerated(EnumType.STRING)
    private GuildImagePreference imagePreference = GuildImagePreference.NONE;

    public GuildEntity() {

    }
}
