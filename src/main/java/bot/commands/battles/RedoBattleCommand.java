package bot.commands.battles;

import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RedoBattleCommand implements ICommand {
    @Autowired
    private DamageStrategy damageStrategy;

    @Override
    public void handle(CommandContext ctx) {
        if (!damageStrategy.validatePersonal(ctx.getMessage())) {
            ctx.reactNegative();
            return;
        }
        String damage = ctx.getMessage().getContentRaw().split(" ")[1];
        damageStrategy.redoBattle(ctx.getGuild(), ctx.getAuthorID(), damage, ctx.getJDA());
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("redobattle");
    }
}
