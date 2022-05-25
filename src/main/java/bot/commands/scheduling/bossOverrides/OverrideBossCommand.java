package bot.commands.scheduling.bossOverrides;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.commands.tracking.TrackingStrategy;
import bot.services.BossService;
import bot.services.GuildService;
import bot.storage.models.GuildEntity;
import bot.utils.PermissionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OverrideBossCommand implements ICommand {
    private final GuildService guildService;
    private final BossService bossService;
    private final ScheduleStrategy scheduleStrategy;
    private final TrackingStrategy trackingStrategy;

    @Autowired
    public OverrideBossCommand(GuildService guildService, BossService bossService, ScheduleStrategy scheduleStrategy, TrackingStrategy trackingStrategy) {
        this.guildService = guildService;
        this.bossService = bossService;
        this.scheduleStrategy = scheduleStrategy;
        this.trackingStrategy = trackingStrategy;
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!PermissionsUtils.checkIfHasAdminPermissions(ctx)) {
            ctx.reactNegative();
            return;
        }

        String bossToSet = ctx.getMessage().getContentRaw().split(" ")[1];
        if (bossToSet == null) {
            ctx.reactNegative();
            return;
        }
        try {
            Integer.parseInt(bossToSet);
        } catch (NumberFormatException e) {
            ctx.reactNegative();
            return;
        }

        int bossPos = Integer.parseInt(bossToSet);
        GuildEntity guild = guildService.getGuild(ctx.getGuildId());
        int currentBossPos = guild.getBoss().getPosition();

        int bossDistance;
        if (bossPos == currentBossPos) {
            // Interpreted as "boss is already set correctly"
            // TODO: Figure out if this is actually a "success"
            ctx.reactPositive();
            return;
        }
        if (currentBossPos > bossPos) {
            bossDistance = 5 - currentBossPos + bossPos;
        } else {
            bossDistance = bossPos - currentBossPos;
        }
        for (int i = 0; i < bossDistance; i++) {
            // We have to manually increment the boss up until the desired amount, this WILL take a while because of how slow the database queries are, but oh well
            bossService.setNextBoss(ctx.getGuildId());
            //trackingStrategy.updateData(ctx.getJDA(), ctx.getGuildId(), true);
            scheduleStrategy.updateSchedule(ctx.getJDA(), ctx.getGuildId(), true);
        }
        ctx.reactPositive();
    }

    @Override
    public List<String> getIdentifiers() {
        return new ArrayList<>() {{
            add("setboss");
            add("setcurrentboss");
        }};
    }
}
