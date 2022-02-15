package bot.configuration;

import bot.commands.battles.strategies.BasicDamageStrategy;
import bot.commands.battles.strategies.DamageBasedPictureStrategy;
import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.battles.strategies.PictureStrategy;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.ScheduleStrategy;
import bot.commands.tracking.TrackingStrategy;
import bot.listeners.CommandListener;
import bot.services.BossService;
import bot.services.GuildService;
import bot.services.SheetService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BotConfig {
    @Value("${token}")
    private String token;

    private static final Map<String, ICommand> commands = new HashMap<>();

    private final GuildService guildService;
    private final SheetService sheetService;
    private final BossService bossService;
    private final TrackingStrategy trackingStrategy;
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public BotConfig(GuildService guildService, SheetService sheetService, BossService bossService, TrackingStrategy trackingStrategy, ScheduleStrategy scheduleStrategy) {
        this.guildService = guildService;
        this.sheetService = sheetService;
        this.bossService = bossService;
        this.trackingStrategy = trackingStrategy;
        this.scheduleStrategy = scheduleStrategy;
    }

    @Bean
    public JDA jda(List<ICommand> commandList) throws LoginException {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.setEnabledIntents(
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_WEBHOOKS);

        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableCache(CacheFlag.CLIENT_STATUS);
        jdaBuilder.setActivity(Activity.of(Activity.ActivityType.COMPETING, "CB!"));

        commandList.forEach(command ->
                command.getIdentifiers().forEach(identifier ->
                        commands.put(identifier, command)));


        jdaBuilder.addEventListeners(new CommandListener(commands));

        return jdaBuilder.build();
    }

    // Define strategies here
    @Bean
    public DamageStrategy damageStrategy() {
        return new BasicDamageStrategy(guildService, sheetService, bossService, trackingStrategy, scheduleStrategy);
    }

    @Bean
    public PictureStrategy pictureStrategy() {
        return new DamageBasedPictureStrategy(guildService);
    }
}
