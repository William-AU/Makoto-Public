package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class TestCommand implements ICommand {
    @Autowired
    private ScheduleStrategy scheduleStrategy;

    @Autowired
    private ScheduleService scheduleService;
    @Override
    public void handle(CommandContext ctx) {
        ctx.sendMessageInChannel(ctx.getJDA().getGuildById("997446642267607070").getName());
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("test");
    }
}
