package bot.configuration;

import bot.storage.units.UnitNameIDContext;
import bot.utils.GenerateUnitIDNameMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfig {
    @Bean
    public UnitNameIDContext unitNameIDContext() {
        return GenerateUnitIDNameMap.generate();
    }
}
