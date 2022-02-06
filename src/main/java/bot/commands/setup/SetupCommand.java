package bot.commands.setup;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.services.SheetService;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
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

        List<User> rawUsers = ctx.getMessage().getTextChannel().getMembers().stream().map(Member::getUser).collect(Collectors.toList());
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
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("setup");
    }
}
