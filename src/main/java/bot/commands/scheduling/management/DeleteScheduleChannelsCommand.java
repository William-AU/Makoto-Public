package bot.commands.scheduling.management;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DeleteScheduleChannelsCommand implements ICommand {
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public DeleteScheduleChannelsCommand(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }


    @Override
    public void handle(CommandContext ctx) {
        if (scheduleStrategy.hasMoreThanOneSchedule(ctx)) {
            System.out.println("Deleting schedule found multiple schedules");
            String[] content = ctx.getMessage().getContentRaw().split(" ");
            if (content.length != 2) {
                ctx.sendError("This server has more than one schedule, please specify which schedule to delete using `!deleteSchedule <name>`");
                return;
            }
            String nameToDelete = content[1];
            scheduleStrategy.deleteSchedule(ctx, nameToDelete);
            ctx.reactPositive();
            return;
        }
        // FIXME: Category name hard coded here but technically variable in scheduling
        try {
            scheduleStrategy.deleteSchedule(ctx);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.reactNegative();
            return;
        }
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("deleteschedule");
    }
}
