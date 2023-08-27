package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.config.GuildImagePreference;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SetImagePreferenceCommand implements ICommand {
    private final GuildService guildService;

    @Autowired
    public SetImagePreferenceCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        String[] content = ctx.getMessage().getContentRaw().split(" ");
        GuildImagePreference preference;
        try {
            preference = GuildImagePreference.valueOf(content[1].toUpperCase());
        } catch (Exception e) {
            ctx.sendError("Incorrect input, please use `!imagepreference <PREFERENCE>`, where preference is either `NONE`, `SFW` or `NSFW` \nNote NSFW is not yet implemented");
            return;
        }
        guildService.setImagePreference(ctx.getGuildId(), preference);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("imagepreference");
    }
}
