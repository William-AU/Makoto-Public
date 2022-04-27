package bot.commands.scheduling.bossOverrides;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.services.ScheduleService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetExpectedAttacksCommand implements ICommand {
    private final ScheduleService scheduleService;
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public SetExpectedAttacksCommand(ScheduleService scheduleService, ScheduleStrategy scheduleStrategy) {
        this.scheduleService = scheduleService;
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        String content = ctx.getMessage().getContentRaw();
        String[] contentSplit = content.split(" ");
        if (contentSplit.length != 4) {
            sendArgumentError(ctx);
            return;
        }
        String posString = contentSplit[1];
        String lapString = contentSplit[2];
        String expectedString = contentSplit[3];

        int pos, lap, expected;
        try {
            pos = Integer.parseInt(posString);
            lap = Integer.parseInt(lapString);
            expected = Integer.parseInt(expectedString);
        } catch (NumberFormatException ignored) {
            sendArgumentError(ctx);
            return;
        }

        scheduleService.setExpectedAttacks(ctx.getGuildId(), pos, lap, expected);
        scheduleStrategy.updateSchedule(ctx.getJDA(), ctx.getGuildId(), false);
        ctx.reactPositive();
    }

    private void sendArgumentError(CommandContext ctx) {
        ctx.sendError("Incorrect arguments, please use `!setexpected <boss position> <boss lap> <expected attacks>");
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("expected");
            add("setexpectedattacks");
            add("setexpected");
        }};
    }
}
