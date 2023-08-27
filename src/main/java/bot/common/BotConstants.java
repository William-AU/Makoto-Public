package bot.common;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class BotConstants {
    public static final String PREFIX = "!";
    public static final Emoji REACT_POSITIVE = Emoji.fromFormatted("<:CONFIRMED:957640892192272424>");
    //public static final String REACT_POSITIVE = "\u2705";
    public static final Emoji REACT_NEGATIVE = Emoji.fromUnicode("\u274C");
    public static final Emoji REACT_WIP = Emoji.fromUnicode("\uD83D\uDEA7");

    public static final String SCHEDULING_CATEGORY_NAME = "makoto-scheduling";
    //public static final String SCHEDULING_CATEGORY_NAME = "yukari-scheduling"; // TODO: Disable lol
    public static final String SCHEDULING_CHANNEL_NAME = "schedule";

    public static final String ASK_FOR_DAMAGE_ID = "ASK_FOR_DAMAGE";

    public static final String STRING_MENU_PREFIX = "select";
    public static final String STRING_MENU_ATTACK = "att";
    public static final String STRING_MENU_ATTACK_OT = "attot";
    public static final String STRING_MENU_LEAVE = "outti";
    public static final String STRING_MENU_NULL_VALUE = "none";

    public static final String STRING_MENU_BOSS_OPTION = "sboss";
}