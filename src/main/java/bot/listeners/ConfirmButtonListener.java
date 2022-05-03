package bot.listeners;

import bot.commands.framework.ICommandContext;
import bot.commands.framework.ManualCommandContext;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.ConfirmButtonType;
import bot.exceptions.schedule.ScheduleException;
import bot.services.BossService;
import bot.services.GuildService;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class ConfirmButtonListener extends ListenerAdapter {
    private final ScheduleStrategy scheduleStrategy;
    private final GuildService guildService;
    private final BossService bossService;

    public ConfirmButtonListener(ScheduleStrategy scheduleStrategy, GuildService guildService, BossService bossService) {
        this.scheduleStrategy = scheduleStrategy;
        this.guildService = guildService;
        this.bossService = bossService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferReply(true).queue();
        Button button = event.getButton();
        String buttonId = button.getId();
        String[] split = buttonId.split("-");
        ConfirmButtonType type = ConfirmButtonType.valueOf(split[1]);
        if (type.equals(ConfirmButtonType.ABORT)) {
            event.getMessage().delete().queue();
            event.getHook().sendMessage("Aborted").queue();
            return;
        }


        switch (split[0]) {
            case "lapoverride" -> handleLapOverride(event, split);
            case "hardreset" -> handleHardReset(event);
        }
    }

    private void handleHardReset(ButtonInteractionEvent event) {
        ManualCommandContext ctx = new ManualCommandContext(event.getGuild(), event.getGuild().getId(), event.getJDA());
        scheduleStrategy.deleteSchedule(ctx);
    }

    private void handleLapOverride(ButtonInteractionEvent event, String[] split) {
        guildService.setLap(event.getGuild().getId(), Integer.parseInt(split[2]));
        // This could be one call, instead of two
        bossService.resetBossHP(event.getGuild().getId());
        bossService.resetBoss(event.getGuild().getId());
        ICommandContext ctx = new ManualCommandContext(event.getGuild(), event.getGuild().getId(), event.getJDA());
        try {
            scheduleStrategy.createSchedule(ctx);
        } catch (ScheduleException e) {
            e.printStackTrace();
        }
        event.getMessage().delete().queue();
        event.getHook().sendMessage("Successfully changed lap").queue();
    }
}
