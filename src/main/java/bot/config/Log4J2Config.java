package bot.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class Log4J2Config {
    private static final Logger logger = LogManager.getLogger(Log4J2Config.class);

    @PostConstruct
    public void init() {
        logger.trace("Bot initialized successfully");
    }
}
