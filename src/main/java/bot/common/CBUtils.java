package bot.common;

public class CBUtils {
    public static int getStageFromLap(int lap) {
        if (lap <= 3) return 1;
        if (lap <= 10) return 2;
        if (lap <= 34) return 3;
        return 4;
    }
}
