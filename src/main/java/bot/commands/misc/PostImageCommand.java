package bot.commands.misc;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.configuration.GuildImagePreference;
import bot.services.GuildService;
import bot.storage.images.MakotoImages;
import bot.utils.PermissionsUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PostImageCommand implements ICommand  {
    private final GuildService guildService;

    @Autowired
    public PostImageCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void handle(CommandContext ctx) {
        if(!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Bad image");
        eb.setImage(MakotoImages.getBadImage());
        EmbedBuilder eb2 = new EmbedBuilder();
        eb2.setTitle("Decent image");
        eb2.setImage(MakotoImages.getDecentImage());
        EmbedBuilder eb3 = new EmbedBuilder();
        eb3.setTitle("Good image");
        eb3.setImage(MakotoImages.getGoodImage());

        ctx.getChannel().sendMessageEmbeds(eb.build(), eb2.build(), eb3.build()).queue();
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("image");
    }
}
