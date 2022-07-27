package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class TestCommand implements ICommand {
    @Autowired
    private ScheduleStrategy scheduleStrategy;

    @Override
    public void handle(CommandContext ctx) {
        ctx.sendMessageInChannel("cp1252: 風宮 あかり" + "UTF-8: " + new String("風宮 あかり".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("test");
    }
}
