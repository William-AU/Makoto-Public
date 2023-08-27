package bot.config;

import bot.commands.battles.strategies.DamageBasedPictureStrategy;
import bot.commands.battles.strategies.DamageStrategy;
import bot.commands.battles.strategies.PictureStrategy;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.*;
import bot.commands.tracking.TrackingStrategy;
import bot.listeners.*;
import bot.services.*;
import bot.utils.CachedSpreadSheetUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BotConfig {
    @Value("${token}")
    private String token;
    private Logger logger = LoggerFactory.getLogger(BotConfig.class);

    private static final Map<String, ICommand> commands = new HashMap<>();

    private final GuildService guildService;
    private final SheetService sheetService;
    private final BossService bossService;
    private final TrackingStrategy trackingStrategy;
    private final ScheduleStrategy scheduleStrategy;
    private final ScheduleService scheduleService;
    private final CachedSpreadSheetUtils cachedSpreadSheetUtils;

    @Autowired
    private DamageStrategy damageStrategy;

    @Autowired
    public BotConfig(@Lazy GuildService guildService, @Lazy SheetService sheetService, @Lazy BossService bossService,
                     @Lazy TrackingStrategy trackingStrategy, @Lazy ScheduleStrategy scheduleStrategy,
                     @Lazy ScheduleService scheduleService, @Lazy CachedSpreadSheetUtils cachedSpreadSheetUtils) {
        this.guildService = guildService;
        this.sheetService = sheetService;
        this.bossService = bossService;
        this.trackingStrategy = trackingStrategy;
        this.scheduleStrategy = scheduleStrategy;
        this.scheduleService = scheduleService;
        this.cachedSpreadSheetUtils = cachedSpreadSheetUtils;
    }

    @Bean
    public JDA jda(List<ICommand> commandList) throws LoginException {
        logger.info("We have started :)");
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.setEnabledIntents(
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.SCHEDULED_EVENTS,
                GatewayIntent.GUILD_WEBHOOKS);

        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableCache(CacheFlag.CLIENT_STATUS);
        //jdaBuilder.setActivity(Activity.of(Activity.ActivityType.COMPETING, "CB!"));
        // TEMPORARY MESSAGE
        //jdaBuilder.setActivity(Activity.of(Activity.ActivityType.PLAYING, "Updating for next CB"));
        jdaBuilder.setActivity(Activity.of(Activity.ActivityType.PLAYING, "Testing new scheduling layout"));

        commandList.forEach(command -> {
            if (command.isTextCommand()) {
                command.getIdentifiers().forEach(identifier ->
                        commands.put(identifier, command));
            }
        });
        jdaBuilder.addEventListeners(new CommandListener(commands));
        jdaBuilder.addEventListeners(new PrivateMessageListener(scheduleStrategy(guildService, bossService, scheduleService)));
        jdaBuilder.addEventListeners(new UpdateChannelListener(scheduleStrategy(guildService, bossService, scheduleService), scheduleService, guildService));
        jdaBuilder.addEventListeners(new SlashCommandListener(commands));
        jdaBuilder.addEventListeners(new SelectMenuListener(scheduleStrategy(guildService, bossService, scheduleService)));
        jdaBuilder.addEventListeners(new DetachedScheduleButtonListener(scheduleStrategy(guildService, bossService, scheduleService), guildService));
        jdaBuilder.addEventListeners(new ConfirmButtonListener(
                scheduleStrategy(guildService, bossService, scheduleService),
                guildService, bossService, sheetService, damageStrategy, cachedSpreadSheetUtils));


        JDA jda = jdaBuilder.build();
        initSlashCommands(jda, commandList);
        return jda;
    }

    private void initSlashCommands(JDA jda, List<ICommand> commandList) {
        // TODO: Consider removing description/identifier and instead use only data
        Map<String, ICommand> slashCommands = new HashMap<>();
        commandList.forEach(command -> {
            if (command.isSlashCommand()) {
                if (command.getIdentifiers().isEmpty()) throw new IllegalArgumentException("Command " + command + " does not have at least one identifier");
                slashCommands.put(command.getIdentifiers().get(0), command);
            }
        });
        List<Command> commands = jda.retrieveCommands().complete();
        commands.forEach(command -> {
            if (!slashCommands.containsKey(command.getName())) {
                slashCommands.remove(command.getName());
            }
        });
        if (slashCommands.isEmpty()) return;

        List<SlashCommandData> slashCommandData = new ArrayList<>() {{
            slashCommands.forEach((key, value) -> add(value.getSlashCommandData()));
        }};
        jda.updateCommands().addCommands(slashCommandData).queue();
    }

    // Define strategies here

    @Bean
    public PictureStrategy pictureStrategy() {
        return new DamageBasedPictureStrategy(guildService);
    }

    @Bean
    public ScheduleStrategy scheduleStrategy(@Lazy GuildService guildService, @Lazy BossService bossService, @Lazy ScheduleService scheduleService) {
        return new SelectMenuScheduleStrategy(guildService, bossService, scheduleService);
    }

    @Bean
    public ExpectedAttackStrategy expectedAttackStrategy(@Lazy DatabaseScheduleService scheduleService) {
        return new DBExpectedAttackStrategy(scheduleService);
    }

}