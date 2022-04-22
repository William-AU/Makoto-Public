package bot.commands.scheduling.bossOverrides;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.ScheduleStrategy;
import bot.services.GuildService;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetBossHealthCommand implements ICommand {
    private final GuildService guildService;
    private final ScheduleStrategy scheduleStrategy;

    @Autowired
    public SetBossHealthCommand(GuildService guildService, ScheduleStrategy scheduleStrategy) {
        this.guildService = guildService;
        this.scheduleStrategy = scheduleStrategy;
    }


    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.permissionsError();
            return;
        }
        String content = ctx.getMessage().getContentRaw().split(" ")[1];
        if (content == null) {
            ctx.reactNegative();
            return;
        }
        try {
            Integer.parseInt(content);
        } catch (NumberFormatException e) {
            ctx.reactNegative();
            return;
        }
        int health = Integer.parseInt(content);
        guildService.setCurrentBossHealth(ctx.getGuildId(), health);
        scheduleStrategy.updateSchedule(ctx.getJDA(), ctx.getGuildId(), false);
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("sethealth");
            add("sethp");
            add("setbosshealth");
            add("setbosshp");
        }};
    }
}
