package bot.commands.battles;

import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.battles.strategies.PictureStrategy;
import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.utils.CachedSpreadSheetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AddBattleCommand implements ICommand {
    @Autowired
    private DamageStrategy damageStrategy;

    @Autowired
    private PictureStrategy pictureStrategy;

    @Autowired
    private CachedSpreadSheetUtils cachedSpreadSheetUtils;

    @Override
    public void handle(CommandContext ctx) {
        System.out.println("In AddBattleCommand");
        if (!damageStrategy.validatePersonal(ctx.getMessage())) {
            ctx.reactNegative();
            return;
        }
        if (!cachedSpreadSheetUtils.userIsSignedUpForMultipleSpreadsheets(ctx.getGuildId(), ctx.getAuthorID())) {
            String damage = ctx.getMessage().getContentRaw().split(" ")[1];
            // We cannot simply assume this is the main sheet!
            if (cachedSpreadSheetUtils.isSignedUpForMainSheet(ctx.getGuildId(), ctx.getAuthorID())) {
                System.out.println("User is only signed up for one spreadsheet");
                damageStrategy.addBattle(ctx.getGuild(), ctx.getAuthorID(), damage, ctx.getJDA());
                pictureStrategy.display(ctx, Integer.parseInt(damage));
                ctx.reactPositive();
                return;
            }
            // In this case we know that the user is in one spreadsheet and that it is not the main spreadsheet
            String sheetID = cachedSpreadSheetUtils.getSpreadsheetIDUserIsActiveIn(ctx.getGuildId(), ctx.getAuthorID()).get(0);
            damageStrategy.addBattle(ctx.getAuthorID(), sheetID, damage, ctx.getJDA());
            pictureStrategy.display(ctx, Integer.parseInt(damage));
            ctx.reactPositive();
            return;
        }
        System.out.println("User is signed up for multiple spreadsheets");
        String messageID = ctx.getMessage().getId();
        ctx.getChannel().sendMessage("This user is participating in multiple clans").setComponents(cachedSpreadSheetUtils.getSpreadsheetButtons(ctx.getGuildId(), "addbattle-" + messageID)).queue();
        ctx.reactWIP();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("addbattle");
    }
}
