package bot.commands.battles.strategies;

import bot.commands.framework.CommandContext;

public interface PictureStrategy {
    void display(CommandContext ctx, int damage);
}
