package bot.commands.battles.strategies;

import bot.commands.framework.CommandContext;
import bot.configuration.ImageDamageConfig;
import bot.services.GuildService;
import bot.storage.images.MakotoImages;
import bot.storage.models.GuildEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DamageBasedPictureStrategy implements PictureStrategy {
    private final GuildService guildService;

    @Autowired
    public DamageBasedPictureStrategy(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void display(CommandContext ctx, int damage) {
        GuildEntity guild = guildService.getGuild(ctx.getGuildId());
        switch (guild.getImagePreference()) {
            case NONE -> {
                // do nothing
            }
            case SFW -> {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Congratulations on your attack! Have some Makoto (and friends)");
                if (ctx.getAuthor().getId().equals("125599045853904896")) {
                    eb.setImage(MakotoImages.getVeryBadImage());
                }
                else if (damage < ImageDamageConfig.DAMAGE_FOR_DECENT_IMAGE) {
                    eb.setImage(MakotoImages.getBadImage());
                }
                else if (damage < ImageDamageConfig.DAMAGE_FOR_GOOD_IMAGE) {
                    eb.setImage(MakotoImages.getDecentImage());
                }
                else {
                    eb.setImage(MakotoImages.getGoodImage());
                }
                ctx.getChannel().sendMessageEmbeds(eb.build()).queue();
            }
            case NSFW -> {
                throw new NotYetImplementedException();
            }
        }
    }
}
