package bot.commands.scheduling;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DeleteScheduleChannelsCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        // FIXME: Category name hard coded here but technically variable in scheduling
        try {
            ctx.getGuild().getCategoriesByName("makoto-scheduling", true)
                    .get(0)
                    .getChannels()
                    .forEach(guildChannel -> guildChannel.delete().queue());
            ctx.getGuild().getCategoriesByName("makoto-scheduling", true).get(0).delete().queue();
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
