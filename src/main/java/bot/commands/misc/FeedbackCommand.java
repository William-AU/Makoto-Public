package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FeedbackCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        ctx.reactWIP();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("feedback");
    }
}
