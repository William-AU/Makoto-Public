package bot.storage.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class GuildSpreadsheetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(unique = true)
    private String spreadsheetId;
    @Column(unique = true)
    private String name;

    @ManyToOne
    private GuildEntity guildEntity;

    @Override
    public String toString() {
        return "GuildSpreadsheetEntity{" +
                "name='" + name + '\'' +
                '}';
    }
}
