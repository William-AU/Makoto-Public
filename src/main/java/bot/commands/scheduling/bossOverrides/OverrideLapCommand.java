package bot.commands.scheduling.bossOverrides;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.common.ConfirmButtonType;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OverrideLapCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        String content = ctx.getMessage().getContentRaw();
        String[] split = content.split(" ");
        if (split.length != 2) {
            ctx.reactNegative();
            return;
        }
        int lap;
        try {
            lap = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            ctx.reactNegative();
            return;
        }

        ctx.getChannel().sendMessage("This command will reset ALL current attacks, are you sure?").setActionRow(createButtons(lap)).queue();
    }

    private List<Button> createButtons(int newLap) {
        String idPrefix = "lapoverride";
        Button confirm = Button.success(idPrefix + "-" + ConfirmButtonType.CONFIRM + "-" + newLap, "Confirm");
        Button abort = Button.danger(idPrefix + "-" + ConfirmButtonType.ABORT, "Abort");
        return new ArrayList<>() {{
            add(confirm);
            add(abort);
        }};
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("setlap");
            add("lap");
        }};
    }
}
