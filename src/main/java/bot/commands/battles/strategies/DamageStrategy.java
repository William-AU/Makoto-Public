package bot.commands.battles.strategies;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

public interface DamageStrategy {
    void addBattle(Guild guild, String userId, String damage, JDA jda);

    void addCarryover(Guild guild, String userId, String damage, JDA jda);

    void redoBattle(Guild guild, String userId, String damage, JDA jda);

    boolean validatePersonal(Message message);

    boolean validateForOther(Message message);
}
