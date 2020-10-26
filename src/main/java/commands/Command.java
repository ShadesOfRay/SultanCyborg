package commands;
import discord4j.core.event.domain.message.MessageCreateEvent;

public interface Command {
    //The string that has to be called after the prefix in order to call the command
    String invoker();
    void action(MessageCreateEvent event);
    void log(MessageCreateEvent event);

}
