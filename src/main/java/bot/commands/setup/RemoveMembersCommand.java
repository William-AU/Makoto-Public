package bot.commands.setup;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.utils.ButtonUtils;
import bot.utils.CachedSpreadSheetUtils;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RemoveMembersCommand implements ICommand {
    @Autowired
    private SheetService sheetService;
    @Autowired
    private GuildService guildService;
    @Autowired
    private CachedSpreadSheetUtils cachedSpreadSheetUtils;

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.reactNegative();
            return;
        }
        boolean hasSingleSpreadsheet = !guildService.hasAdditionalSpreadsheets(ctx.getGuildId());
        if (hasSingleSpreadsheet) {
            List<User> mentionedUsers = ctx.getMessage().getMentions().getUsers();
            String sheetID = guildService.getSpreadSheetId(ctx.getGuildId());
            List<String> usersString = new ArrayList<>() {{
                for (User user : mentionedUsers) {
                    add(user.getId());
                }
            }};
            sheetService.removeMembersFromSheet(usersString, sheetID);
            for (User user : mentionedUsers) {
                cachedSpreadSheetUtils.removeMember(ctx.getGuildId(), sheetID, user.getId());
            }
            ctx.reactPositive();
            return;
        }
        String messageID = ctx.getMessage().getId();
        List<String> knownSpreadSheets = new ArrayList<>() {{
            add("Main");
            addAll(guildService.getSpreadsheetNames(ctx.getGuildId()));
            add("Abort");
        }};
        List<ActionRow> buttons = ButtonUtils.createGenericButtons("removemembers-" + messageID, false, true, knownSpreadSheets.toArray(new String[0]));
        ctx.getChannel().sendMessage("This server has multiple clans registered, which clan would you like to remove members from?").setComponents(buttons).queue();
        ctx.reactWIP();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("removemembers");
    }
}
