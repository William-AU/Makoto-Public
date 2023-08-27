package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetMessagesToDisplayCommand implements ICommand {
    private final GuildService guildService;
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public SetMessagesToDisplayCommand(GuildService guildService, ScheduleStrategy scheduleStrategy) {
        this.guildService = guildService;
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.reactNegative();
            return;
        }
        String[] args = ctx.getMessage().getContentRaw().split(" ");
        if (args.length != 2) {
            ctx.sendError("Use `!setMessages <amount>`");
            return;
        }
        int messages = Integer.parseInt(args[1]);
        guildService.setMessagesToDisplay(ctx.getGuildId(), messages);
        scheduleStrategy.updateSchedule(ctx.getJDA(), ctx.getGuildId(), false);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("lapstoshow");
            add("messagestoshow");
            add("messages");
            add("setlapstoshow");
            add("setmessagestoshow");
            add("setlapstoshow");
        }};
    }
}
