package bot.commands.scheduling;

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
        // FIXME: Category name hard coded here but technically variable in scheduling
        try {
            scheduleStrategy.deleteSchedule(ctx);
        } catch (Exception ignored) {
            ctx.reactNegative();
        }
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("deleteschedule");
    }
}
