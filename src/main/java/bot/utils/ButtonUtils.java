package bot.utils;

import bot.common.ConfirmButtonType;
import lombok.Data;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class ButtonUtils {
    /**
     * Creates two buttons for confirming or aborting an action
     * @param buttonPrefix The prefix corresponding to the calling method, needed for handlers to know what to later confirm
     * @return two created buttons with first element being the confirm button, second being the abort button
     */
    public static List<Button> createConfirmationButtons(String buttonPrefix) {
        Button confirm = Button.success(buttonPrefix + "-" + ConfirmButtonType.CONFIRM, "Confirm");
        Button abort = Button.danger(buttonPrefix + "-" + ConfirmButtonType.ABORT, "Abort");
        return new ArrayList<>() {{
            add(confirm);
            add(abort);
        }};
    }

    public static List<Button> createGenericIDButtons(String buttonPrefix, boolean firstGreen, boolean lastRed, List<ButtonNameIDTuple> nameIdTuples) {
        List<Button> res = new ArrayList<>();
        if (nameIdTuples.size() == 1 && firstGreen && lastRed) {
            throw new IllegalArgumentException("First button cannot be both red and green, too many flags set with argument length 1");
        }
        if (nameIdTuples.size() == 0) {
            throw new IllegalArgumentException("Must provide at least one button name");
        }
        for (int i = 0; i < nameIdTuples.size(); i++) {
            if (i == 0 && firstGreen) {
                res.add(Button.success(buttonPrefix + "-" + nameIdTuples.get(i).getId(), nameIdTuples.get(i).getName()));
            } else if (i == nameIdTuples.size() - 1) {
                res.add(Button.danger(buttonPrefix + "-" + nameIdTuples.get(i).getId(), nameIdTuples.get(i).getName()));
            } else {
                res.add(Button.primary(buttonPrefix + "-" + nameIdTuples.get(i).getId(), nameIdTuples.get(i).getName()));
            }
        }
        return res;
    }

    public static List<ActionRow> createGenericButtons(String buttonPrefix, boolean firstGreen, boolean lastRed, List<String> names) {
        return createGenericButtons(buttonPrefix, firstGreen, lastRed, names.toArray(new String[0]));
    }

    public static List<ActionRow> createGenericButtons(String buttonPrefix, boolean firstGreen, boolean lastRed, String... name) {
        System.out.println("Creating generic buttons with prefix: " + buttonPrefix);
        List<Button> res = new ArrayList<>();
        if (name.length == 1 && firstGreen && lastRed) {
            throw new IllegalArgumentException("First button cannot be both red and green, too many flags set with argument length 1");
        }
        if (name.length == 0) {
            throw new IllegalArgumentException("Must provide at least one button name");
        }
        for (int i = 0; i < name.length; i++) {
            if (i == 0 && firstGreen) {
                res.add(Button.success(buttonPrefix + "-" + name[i], name[i]));
            } else if (i == name.length - 1) {
                res.add(Button.danger(buttonPrefix + "-" + name[i], name[i]));
            } else {
                res.add(Button.primary(buttonPrefix + "-" + name[i], name[i]));
            }
        }
        List<Button> buttons = new ArrayList<>(res);
        List<ActionRow> rows = new ArrayList<>();
        if (buttons.size() > 5) {
            int buttonsLeft = buttons.size();
            int index = 0;
            while (buttonsLeft > 0) {
                if (buttonsLeft > 5) {
                    rows.add(ActionRow.of(
                            buttons.get(index),
                            buttons.get(index + 1),
                            buttons.get(index + 2),
                            buttons.get(index + 3),
                            buttons.get(index + 4)
                    ));
                    index += 5;
                    buttonsLeft -= 5;
                } else {
                    List<Button> toAdd = new ArrayList<>();
                    for (int i = 0; i < buttonsLeft; i++) {
                        toAdd.add(buttons.get(index + i));
                    }
                    rows.add(ActionRow.of(toAdd));
                    break;
                }
            }
        } else {
            rows.add(ActionRow.of(buttons));
        }
        return rows;
    }

    @Data
    public static class ButtonNameIDTuple {
        private final String name;
        private final String id;
        public ButtonNameIDTuple(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }
}
