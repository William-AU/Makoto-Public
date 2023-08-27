package bot.commands.scheduling.management;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.ConfirmButtonType;
import bot.services.ScheduleService;
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
        if (scheduleStrategy.hasMoreThanOneSchedule(ctx)) {
            String content[] = ctx.getMessage().getContentRaw().split(" ");
            if (content.length != 2) {
                ctx.sendError("The server has more than one schedule please specify which you want to reset using `!resetSchedule <name>`");
                return;
            }
            String scheduleName = content[1];
            ctx.getChannel().sendMessage("Warning this will reset tracking for the CB, meaning all progress will be lost! Are you sure?").setActionRow(createButtons(scheduleName)).queue();
            ctx.reactPositive();
            return;
        }

        ctx.getChannel().sendMessage("Warning, this will reset the tracking for the CB, meaning all progress will be lost! Are you sure?").setActionRow(createButtons()).queue();
        ctx.reactPositive();
    }

    private List<Button> createButtons(String scheduleName) {
        String idPrefix = "hardreset";
        Button confirm = Button.success(idPrefix + "-" + ConfirmButtonType.CONFIRM + "-FILLER-" + scheduleName, "Confirm");
        Button abort = Button.danger(idPrefix + "-" + ConfirmButtonType.ABORT + "-" + scheduleName, "Abort");
        return new ArrayList<>() {{
            add(confirm);
            add(abort);
        }};
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
