package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TestCommand implements ICommand {
    @Autowired
    private ScheduleStrategy scheduleStrategy;

    @Override
    public void handle(CommandContext ctx) {

    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("test");
    }
}
