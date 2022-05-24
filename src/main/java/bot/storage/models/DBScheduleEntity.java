package bot.storage.models;

import bot.exceptions.MemberIsAlreadyAttackingException;
import bot.exceptions.MemberIsNotAttackingException;
import lombok.Data;
import org.aspectj.weaver.ast.Test;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String emojiRegex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
            Pattern emojiPattern = Pattern.compile(emojiRegex, Pattern.UNICODE_CHARACTER_CLASS);
            Matcher emojiMatcher = emojiPattern.matcher(userNick);
            String noEmojiNick = emojiMatcher.replaceAll("");
            this.userNick = noEmojiNick;
            this.hasAttacked = hasAttacked;
        }

        public void setUserNick(String userNick) {
            String emojiRegex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
            Pattern emojiPattern = Pattern.compile(emojiRegex, Pattern.UNICODE_CHARACTER_CLASS);
            Matcher emojiMatcher = emojiPattern.matcher(userNick);
            String noEmojiNick = emojiMatcher.replaceAll("");
            this.userNick = noEmojiNick;
        }

        public ScheduleUser() {
            hasAttacked = false;
        }
    }
}
