package bot.common;

public class CBUtils {
    public static int getStageFromLap(int position) {
        if (position <= 3) return 1;
        if (position <= 10) return 2;
        return 3;
    }
}
