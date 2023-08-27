package bot.utils;

import bot.commands.framework.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PermissionsUtils {
    public static boolean checkIfHasAdminPermissions(CommandContext ctx) {
        final Member user = ctx.getMember();
        if (user.getId().equals("125599045853904896")) return true; // Manual override for Tal
        // FuwaFuwa overrides
        if (user.getId().equals("255057399088545793") && ctx.getGuildId().equals("529079922036047872")) return true;
        if (ctx.getGuildId().equals("529079922036047872")) {
            for (Role role : user.getRoles()) {
                // YabaiYabai Officer
                if (role.getId().equals("810699774021992449")) return true;
                // ALL YabaiYabai members
                if (role.getId().equals("802671256163844136")) return true;
            }
        }
        return user.hasPermission(Permission.ADMINISTRATOR);
    }
}
