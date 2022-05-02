package bot.storage.models;

import bot.exceptions.MemberIsAlreadyAttackingException;
import bot.exceptions.MemberIsNotAttackingException;
import lombok.Data;
import org.aspectj.weaver.ast.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
public class DBScheduleEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String guildId;
    private ScheduleUser user;
    private int stage;
    private int lap;
    private int pos;

    @Embeddable
    @Data
    public static class ScheduleUser {
        private String userId;
        private String userNick;
        private boolean hasAttacked;

        public ScheduleUser(String userId, String userNick, boolean hasAttacked) {
            this.userId = userId;
            this.userNick = userNick;
            this.hasAttacked = hasAttacked;
        }

        public ScheduleUser() {

        }
    }
}
