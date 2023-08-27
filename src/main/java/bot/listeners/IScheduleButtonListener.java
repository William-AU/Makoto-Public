package bot.listeners;

import com.sun.istack.NotNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public interface IScheduleButtonListener extends EventListener {
    void onButtonInteraction(@NotNull ButtonInteractionEvent event);
}
