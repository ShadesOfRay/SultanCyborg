package commands;
import discord4j.core.event.domain.message.MessageCreateEvent;

public interface Command {
    /**
     * The String that must be used to invoke the command after the prefix
     * @return the string that calls the command
     */
    String invoker();

    /**
     * The String that has all the info about the command, including what it does and what kind of inputs it wants.
     * @return the info about the commands
     */
    String info();

    /**
     * The amount of arguments that a command wants in order to run correctly, the array should have all the valid
     * amounts of arguments that the command wants, -1 means any amount of arguments
     * @return the amount of arguments that a command requires
     */
    int[] argumentsNeeded();

    /**
     * The method that is called when the command is called
     * @param event is the MessageCreateEvent that called the command
     */
    void action(MessageCreateEvent event);

    /**
     * The This is also called when the command is called, but is used to log the command
     * @param event is the MessageCreateEvent that called the command
     */
    void log(MessageCreateEvent event);

}
