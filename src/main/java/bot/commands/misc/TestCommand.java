package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.EmbedScheduleStrategy;
import bot.commands.scheduling.ScheduleStrategy;
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
        scheduleStrategy.extractMembers(ctx.getJDA(), ctx.getGuildId());
        System.out.println(ctx.getMessage().getContentRaw());
        String content = ctx.getMessage().getContentRaw().split(" ")[1];
        System.out.println(content.substring(3, content.length() - 1));
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("test");
    }
}
