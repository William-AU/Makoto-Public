package bot.listeners;

import bot.commands.framework.CommandContext;
import bot.commands.framework.ICommandContext;
import bot.commands.framework.ManualCommandContext;
import bot.commands.scheduling.ResetScheduleCommand;
import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.ConfirmButtonType;
import bot.exceptions.ScheduleException;
import bot.services.BossService;
import bot.services.GuildService;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

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
        if (!split[0].equalsIgnoreCase("lapoverride")) return;
        ConfirmButtonType type = ConfirmButtonType.valueOf(split[1]);
        switch (type) {
            case ABORT -> {
                event.getMessage().delete().queue();
                event.getHook().sendMessage("Aborted").queue();
            }
            case CONFIRM -> {
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
    }
}
