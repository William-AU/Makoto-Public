package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.ConfirmButtonType;
import bot.exceptions.schedule.ScheduleException;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResetScheduleCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public ResetScheduleCommand(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }


    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        ctx.getChannel().sendMessage("Warning, this will reset the tracking for the CB, meaning all progress will be lost! Are you sure?").setActionRow(createButtons()).queue();
        ctx.reactPositive();
    }

    private List<Button> createButtons() {
        String idPrefix = "hardreset";
        Button confirm = Button.success(idPrefix + "-" + ConfirmButtonType.CONFIRM + "-FILLER", "Confirm");
        Button abort = Button.danger(idPrefix + "-" + ConfirmButtonType.ABORT, "Abort");
        return new ArrayList<>() {{
            add(confirm);
            add(abort);
        }};
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("resetschedule");
            add("restartschedule");
        }};
    }
}
