package bot.commands.setup;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.utils.ButtonUtils;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetupCommand implements ICommand {
    @Autowired
    private GuildService guildService;

    @Autowired
    private SheetService sheetService;

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.reactNegative();
            return;
        }
        boolean hasMultipleSpreadsheets = guildService.hasAdditionalSpreadsheets(ctx.getGuildId());
        if (!hasMultipleSpreadsheets) {
            List<User> rawUsers = ctx.getMessage().getChannel().asTextChannel().getMembers().stream().map(Member::getUser).collect(Collectors.toList());
            List<User> users = new ArrayList<>();
            for (User user : rawUsers) {
                if (!user.isBot()) users.add(user);
            }

            String spreadsheetId = guildService.getSpreadSheetId(ctx.getGuild().getId());
            try {
                sheetService.setupSheet(ctx.getGuild().getId(), spreadsheetId, users);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.reactNegative();
                return;
            }
            ctx.reactPositive();
            return;
        }
        // We have more than one spreadsheet registered
        List<String> knownSpreadsheets = new ArrayList<>() {{
            add("Main");
            addAll(guildService.getSpreadsheetNames(ctx.getGuildId()));
        }};
        knownSpreadsheets.add("Abort");
        System.out.println("Asking generic buttons to create buttons with prefix: setup");
        System.out.println("Knownspreadsheets: " + knownSpreadsheets);
        List<ActionRow> buttons = ButtonUtils.createGenericButtons("setup", false, true, knownSpreadsheets.toArray(new String[0]));
        ctx.getChannel().sendMessage("This server has multiple clans registered, which guild would you like to set up?").setComponents(buttons).queue();
        ctx.reactWIP();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("setup");
    }
}
