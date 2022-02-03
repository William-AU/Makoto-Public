package bot.utils;

import bot.commands.framework.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PermissionsUtils {
    public static boolean checkIfHasAdminPermissions(CommandContext ctx) {
        final Member user = ctx.getMember();
        return user.hasPermission(Permission.ADMINISTRATOR);
    }
}
