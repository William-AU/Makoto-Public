package bot.commands.setup;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.utils.ButtonUtils;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RegisterCommand implements ICommand {
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
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        if (content.length != 2) {
            ctx.reactNegative();
            return;
        }
        try {
            String existingID = guildService.getSpreadSheetId(ctx.getGuildId());
            if (existingID == null) {
                guildService.addSpreadsheetId(ctx.getGuild().getId(), extractId(content[1]));
                ctx.reactPositive();
                return;
            }
            // Handle confirmation for multiple guilds
            List<ButtonUtils.ButtonNameIDTuple> buttonNames = new ArrayList<>() {{
                add(new ButtonUtils.ButtonNameIDTuple("Add new guild", "add new guild-" + extractId(content[1])));
                add(new ButtonUtils.ButtonNameIDTuple("Replace existing guild: Main", "replace existing guild-Main-" + extractId(content[1])));
                List<GuildService.Spreadsheet> sheets = guildService.getAdditionalSpreadsheets(ctx.getGuildId());
                for (GuildService.Spreadsheet s : sheets) {
                    add(new ButtonUtils.ButtonNameIDTuple("Replace existing guild: " + s.getName(), "replace existing guild-" + s.getSmallID() + "-" + extractId(content[1])));
                }
                add(new ButtonUtils.ButtonNameIDTuple("Abort", "abort"));
            }};
            List<Button> buttons = ButtonUtils.createGenericIDButtons("register", true, true, buttonNames);
            List<ActionRow> rows = new ArrayList<>(){{
                if (buttons.size() <= 5) {
                    add(ActionRow.of(buttons));
                } else {
                    Map<Integer, List<Button>>  buttonMap = new HashMap<>() {{
                        put(0, new ArrayList<>());
                        put(1, new ArrayList<>());
                        put(2, new ArrayList<>());
                        put(3, new ArrayList<>());
                        put(4, new ArrayList<>());
                    }};
                    for (int i = 0; i < buttons.size(); i++) {
                        Button toAdd = buttons.get(i);
                        int rowNumber = i / 5;
                        List<Button> list = buttonMap.get(rowNumber);
                        list.add(toAdd);
                    }
                    for (int i = 0; i < 5; i++) {
                        List<Button> buttons = buttonMap.get(i);
                        if (buttons.isEmpty()) break;
                        add(ActionRow.of(buttons));
                    }
                }
            }};
            ctx.getChannel().sendMessage("A guild is already registered for this discord server, would you like to add another or replace the existing guild?").setComponents(rows).queue();
            ctx.reactWIP(); // Debatable if this is a positive reaction yet, but we give up ownership of the interaction so it's this or nothing
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            ctx.reactNegative();
        }
    }

    private String extractId(String url) {
        // Haven't tested, taken directly from old version
        return url.split("/")[5];
    }


    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("register");
    }
}
