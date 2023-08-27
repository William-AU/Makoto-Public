package bot.commands.battles;

import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.CachedSpreadSheetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RedoBattleCommand implements ICommand {
    @Autowired
    private DamageStrategy damageStrategy;

    @Autowired
    private CachedSpreadSheetUtils cachedSpreadSheetUtils;

    @Override
    public void handle(CommandContext ctx) {
        if (!damageStrategy.validatePersonal(ctx.getMessage())) {
            ctx.reactNegative();
            return;
        }
        if (!cachedSpreadSheetUtils.userIsSignedUpForMultipleSpreadsheets(ctx.getGuildId(), ctx.getAuthorID())) {
            String damage = ctx.getMessage().getContentRaw().split(" ")[1];
            if (cachedSpreadSheetUtils.isSignedUpForMainSheet(ctx.getGuildId(), ctx.getAuthorID())) {
                damageStrategy.redoBattle(ctx.getGuild(), ctx.getAuthorID(), damage, ctx.getJDA());
                ctx.reactPositive();
                return;
            }
            String sheetID = cachedSpreadSheetUtils.getSpreadsheetIDUserIsActiveIn(ctx.getGuildId(), ctx.getAuthorID()).get(0);
            damageStrategy.redoBattle(ctx.getAuthorID(), sheetID, damage, ctx.getJDA());
            ctx.reactPositive();
            return;
        }
        String messageID = ctx.getMessage().getId();
        ctx.getChannel().sendMessage("This user is participating in multiple clans").setComponents(cachedSpreadSheetUtils.getSpreadsheetButtons(ctx.getGuildId(), "redobattle-" + messageID)).queue();
        ctx.reactWIP();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("redobattle");
    }
}
