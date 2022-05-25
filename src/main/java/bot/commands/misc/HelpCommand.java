package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Collections;
import java.util.List;

@Service
public class HelpCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("For more info check my discord channel");
        embedBuilder.setDescription("Send me your feedback on my [discord channel](https://discord.gg/D7ubf5zq)\n" +
                "You can also support me on [Ko-fi](https://ko-fi.com/fexamakoto)" +
                " or [Paypal](https://paypal.me/fexaMakoto)\n");
        embedBuilder.setColor(new Color(78, 60, 108));
        embedBuilder.setFooter("All commands update the MakotoBot sheet and nothing else\n" +
                "If you want to create/remake the MakotoBot sheet either rename or delete the current one" +
                " before using the setup command", null);
        ctx.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("help");
    }
}
