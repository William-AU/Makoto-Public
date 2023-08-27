package bot.commands.battles;

import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.CachedSpreadSheetUtils;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RedoBattleForCommand implements ICommand {
    @Autowired
    private DamageStrategy damageStrategy;

    @Autowired
    private CachedSpreadSheetUtils cachedSpreadSheetUtils;

    @Override
    public void handle(CommandContext ctx) {
        if (!damageStrategy.validateForOther(ctx.getMessage())) {
            ctx.reactNegative();
            return;
        }
        String damage = ctx.getMessage().getContentRaw().split(" ")[1];
        List<Member> mentionedMembers = ctx.getMessage().getMentions().getMembers();
        if(mentionedMembers.isEmpty()){
            ctx.reactNegative();
            return;
        }
        if (mentionedMembers.size() > 1) {
            ctx.sendError("Too many mentions, please only mention one user");
            return;
        }
        if (!cachedSpreadSheetUtils.userIsSignedUpForMultipleSpreadsheets(ctx.getGuildId(), mentionedMembers.get(0).getUser().getId())) {
            String userID = mentionedMembers.get(0).getUser().getId();
            if (cachedSpreadSheetUtils.isSignedUpForMainSheet(ctx.getGuildId(), userID)) {
                damageStrategy.redoBattle(ctx.getGuild(), userID, damage, ctx.getJDA());
                ctx.reactPositive();
                return;
            }
            String sheetID = cachedSpreadSheetUtils.getSpreadsheetIDUserIsActiveIn(ctx.getGuildId(), ctx.getAuthorID()).get(0);
            damageStrategy.redoBattle(userID, sheetID, damage, ctx.getJDA());
            ctx.reactPositive();
            return;
        }
        String messageID = ctx.getMessage().getId();
        ctx.getChannel().sendMessage("This user is participating in multiple clans").setComponents(cachedSpreadSheetUtils.getSpreadsheetButtons(ctx.getGuildId(), "redobattlefor-" + messageID)).queue();
        ctx.reactWIP();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("redobattlefor");
    }
}
