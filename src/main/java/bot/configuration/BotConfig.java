package bot.configuration;

import bot.commands.framework.ICommand;
import bot.listeners.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BotConfig {
    private static final Map<String, ICommand> commands = new HashMap<>();

    @Bean
    public JDA jda(List<ICommand> commandList) throws LoginException {
        JDABuilder jdaBuilder = JDABuilder.createDefault("NzUwNjQyNTI1NjY4MTE0NDQy.X09gVA.DfdemZxiPX19m1o82IrSEYYzY_s");
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

        commandList.forEach(command ->
                command.getIdentifiers().forEach(identifier ->
                        commands.put(identifier, command)));


        jdaBuilder.addEventListeners(new CommandListener(commands));

        return jdaBuilder.build();
    }
}
