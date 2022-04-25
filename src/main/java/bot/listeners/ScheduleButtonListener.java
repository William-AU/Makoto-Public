package bot.listeners;

import bot.commands.scheduling.strategies.ScheduleStrategy;
import bot.common.ScheduleButtonType;
import bot.exceptions.MemberAlreadyExistsException;
import bot.exceptions.MemberHasAlreadyAttackedException;
import bot.exceptions.MemberHasNotAttackedException;
import bot.exceptions.MemberIsNotAttackingException;
import bot.services.GuildService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class ScheduleButtonListener extends ListenerAdapter {
    private final ScheduleStrategy scheduleStrategy;
    private final GuildService guildService;

    public ScheduleButtonListener(ScheduleStrategy scheduleStrategy, GuildService guildService) {
        this.scheduleStrategy = scheduleStrategy;
        this.guildService = guildService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        System.out.println("NAME: " + event.getUser().getName());
        Member member = event.getMember();
        Button button = event.getButton();
        String buttonId = button.getId();
        String[] buttonInfo = buttonId.split("-"); // Array should be of size 4 with the following info: ScheduleButtonType, guildID, bossPosition, bossLap
        ScheduleButtonType type;
        try {
            type = ScheduleButtonType.valueOf(buttonInfo[0]);
        } catch (IllegalArgumentException e) {
            // If we are in this case, it means the type doesn't exist, therefore this is not a button we should be interacting with!
            return;
        }
        String guildId = buttonInfo[1];
        int bossPosition = Integer.parseInt(buttonInfo[2]);
        int lap = Integer.parseInt(buttonInfo[3]);
        int currentLap = guildService.getGuild(guildId).getLap();
        // Check to see if the button is for the next lap or the current lap and change the boss position accordingly
        if (lap > currentLap) {
            bossPosition += 5;
        }
        InteractionHook hook = event.deferReply(true).complete();

        try {
            switch (type) {
                case JOIN -> {
                    scheduleStrategy.addAttacker(event.getJDA(), guildId, bossPosition, member.getNickname());
                }
                case LEAVE -> {
                    scheduleStrategy.removeAttacker(event.getJDA(), guildId, bossPosition, member.getNickname());
                }
                case COMPLETE -> {
                    scheduleStrategy.markFinished(event.getJDA(), guildId, bossPosition, member.getNickname());
                }
                case UNCOMPLETE -> {
                    scheduleStrategy.unMarkFinished(event.getJDA(), guildId, bossPosition, member.getNickname());
                }
            }
            hook.editOriginal("Success!").queue();
            //event.reply("Success!").setEphemeral(true).queue();
        } catch (MemberAlreadyExistsException e) {
            sendError(hook, "Cannot join, already attacking");
        } catch (MemberHasAlreadyAttackedException e) {
            sendError(hook, "You have already attacked this boss, the bot currently does not support multiple attacks on the same boss, sorry!");
        } catch (MemberHasNotAttackedException e) {
            sendError(hook, "You have already completed your attack, the bot currently does not support multiple attacks on the same boss, sorry!");
        } catch (MemberIsNotAttackingException e) {
            sendError(hook, "You are not attacking the current boss");
        }
    }

    private void sendError(InteractionHook hook, String message) {
        hook.editOriginal(message).queue();
    }
}
