package bot.listeners;

import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.BotConstants;
import bot.exceptions.MemberAlreadyExistsException;
import bot.exceptions.MemberHasAlreadyAttackedException;
import bot.exceptions.MemberIsNotAttackingException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class SelectMenuListener extends ListenerAdapter {
    private final ScheduleStrategy scheduleStrategy;

    public SelectMenuListener(ScheduleStrategy scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        System.out.println("Using strategy: " + scheduleStrategy.getClass());
        String rawInfo = event.getValues().get(0);
        String[] info = rawInfo.split("-");
        if (info[0].equalsIgnoreCase(BotConstants.STRING_MENU_NULL_VALUE)) {
            event.reply("That is not a valid option...").setEphemeral(true).queue();
            return;
        }
        String menuType = event.getComponentId().split("-")[0];
        switch (menuType) {
            case BotConstants.STRING_MENU_PREFIX -> handleScheduleMenu(info, event);
        }
    }

    private void handleScheduleMenu(String[] info, StringSelectInteractionEvent event) {
        event.deferReply(true).queue();
        // Quick heads up, java UUIDs just so happens to have enough "-" to make this check fail for null events...
        if (info.length != 5) return;
        Member member = event.getMember();

        String type = info[0];
        int pos = Integer.parseInt(info[1]);
        int lap = Integer.parseInt(info[2]);
        String guildId = info[3];
        String scheduleName = info[4];

        System.out.println(Arrays.toString(info) + " (" + info.length + ")");
        try {
            switch (type) {
                case BotConstants.STRING_MENU_ATTACK -> {
                    scheduleStrategy.addAttacker(event.getJDA(), guildId, pos, lap, member.getEffectiveName(), scheduleName, false);
                    finish(event);
                }
                case BotConstants.STRING_MENU_ATTACK_OT -> {
                    scheduleStrategy.addAttacker(event.getJDA(), guildId, pos, lap, member.getEffectiveName(), scheduleName, true);
                    finish(event);
                }
                case BotConstants.STRING_MENU_LEAVE -> {
                    scheduleStrategy.removeAttacker(event.getJDA(), guildId, pos, lap, member.getEffectiveName(), scheduleName);
                    finish(event);
                }
            }
        } catch (MemberAlreadyExistsException e) {
            sendError(event, "You are already assigned!");
        } catch (MemberHasAlreadyAttackedException e) {
            sendError(event, "You are already attacking!");
        } catch (MemberIsNotAttackingException e) {
            sendError(event, "You must be attacking to leave!");
        }
    }



    private void sendError(StringSelectInteractionEvent event, String message) {
        event.getHook().editOriginal(message).queue();
    }

    private void finish(StringSelectInteractionEvent event) {
        event.getHook().editOriginal("Success!").queue();
    }
}
