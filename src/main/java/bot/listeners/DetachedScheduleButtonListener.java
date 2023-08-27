package bot.listeners;

import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.ScheduleButtonType;
import bot.exceptions.MemberAlreadyExistsException;
import bot.exceptions.MemberHasAlreadyAttackedException;
import bot.exceptions.MemberIsNotAttackingException;
import bot.services.GuildService;
import com.sun.istack.NotNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Arrays;

public class DetachedScheduleButtonListener extends ListenerAdapter implements IScheduleButtonListener {
    private final ScheduleStrategy scheduleStrategy;
    private final GuildService guildService;

    public DetachedScheduleButtonListener(ScheduleStrategy scheduleStrategy, GuildService guildService) {
        this.scheduleStrategy = scheduleStrategy;
        this.guildService = guildService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Member member = event.getMember();
        Button button = event.getButton();
        String buttonId = button.getId();
        String[] buttonInfo = buttonId.split("-");
        String guildId;
        int lap, pos;
        try {
            guildId = buttonInfo[1];
            lap = Integer.parseInt(buttonInfo[2]);
            pos = Integer.parseInt(buttonInfo[3]);
        } catch (Exception ignored) {
            // If this fails, assume we aren't meant to listen for it
            return;
        }
        String scheduleName = null;
        System.out.println("BUTTON INFO: " + Arrays.toString(buttonInfo));
        boolean isOverTime = buttonInfo[4].equalsIgnoreCase("overtime");
        try {
            scheduleName = buttonInfo[5];
        } catch (IndexOutOfBoundsException ignored) { }
        ScheduleButtonType type;
        try {
            type = ScheduleButtonType.valueOf(buttonInfo[0]);
        } catch (IllegalArgumentException e) {
            return;
        }
        System.out.println("BUTTON CLICKED WITH ID: " + button.getId());
        event.deferReply(true).queue();
        String name = member.getEffectiveName();
        System.out.println("Effective name: " + name + " nickname: " + member.getNickname());
        try {
            switch (type) {
                case JOIN -> {
                    if (scheduleName != null) {
                        scheduleStrategy.addAttacker(event.getJDA(), guildId, pos, lap, member.getEffectiveName(), scheduleName, isOverTime);
                    } else {
                        scheduleStrategy.addAttacker(event.getJDA(), guildId, pos, lap, member.getEffectiveName());
                    }
                    System.out.println("DEBUG MEMBER JOINED SCHEDULE FOR GUILD: " + event.getGuild().getName() + " MEMBER: " + member.getEffectiveName());
                    finish(event);
                }
                case LEAVE -> {
                    if (scheduleName != null) {
                        scheduleStrategy.removeAttacker(event.getJDA(), guildId, pos, lap, member.getEffectiveName(), scheduleName);
                    } else {
                        scheduleStrategy.removeAttacker(event.getJDA(), guildId, pos, lap, member.getEffectiveName());
                    }
                    finish(event);
                }
                default -> {
                    System.out.println("UNEXPECTED BUTTON IN DetachedScheduleButtonListener: " + event.getButton().getId());
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

    private void sendError(ButtonInteractionEvent event, String message) {
        event.getHook().editOriginal(message).queue();
    }

    private void finish(ButtonInteractionEvent event) {
        event.getHook().editOriginal("Success!").queue();
    }
}
