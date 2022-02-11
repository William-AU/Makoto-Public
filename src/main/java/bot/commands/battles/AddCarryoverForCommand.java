package bot.commands.battles;

import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AddCarryoverForCommand implements ICommand {
    @Autowired
    private DamageStrategy damageStrategy;

    @Override
    public void handle(CommandContext ctx) {
        if (!damageStrategy.validateForOther(ctx.getMessage())) {
            ctx.reactNegative();
            return;
        }
        String damage = ctx.getMessage().getContentRaw().split(" ")[1];
        List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();
        if(mentionedMembers.isEmpty()){
            ctx.reactNegative();
            return;
        }

        String userID = mentionedMembers.get(0).getUser().getId();
        damageStrategy.addCarryover(ctx.getGuild(), userID, damage, ctx.getJDA());
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("addcarryoverfor");
    }
}
