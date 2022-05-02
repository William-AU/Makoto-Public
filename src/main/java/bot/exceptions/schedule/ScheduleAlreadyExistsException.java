package bot.exceptions.schedule;

public class ScheduleAlreadyExistsException extends ScheduleException {
    public ScheduleAlreadyExistsException(String message) {
        super(message);
    }
}
