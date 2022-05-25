package bot.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public interface IScheduleButtonListener extends EventListener {
    void onButtonInteraction(@NotNull ButtonInteractionEvent event);
}
