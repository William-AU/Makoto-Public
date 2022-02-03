package bot.commands;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.services.GuildService;
import bot.storage.models.BossEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TestCommand implements ICommand {
    @Autowired
    private GuildService guildService;

    @Override
    public void handle(CommandContext ctx) {

    }

    @Override
    public List<String> getIdentifiers() {
        return Collections.singletonList("test");
    }
}
