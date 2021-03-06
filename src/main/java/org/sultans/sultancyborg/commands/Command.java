package org.sultans.sultancyborg.commands;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.util.annotation.Nullable;

public interface Command {
    /**
     * The String that must be used to invoke the command after the prefix
     *
     * @return the string that calls the command
     */
    String invoker();

    /**
     * The String that has all the info about the command, including what it does and what kind of inputs it wants.
     *
     * @return the info about the org.sultans.SultanCyborg.commands
     */
    String info();

    /**
     * The amount of arguments that a command wants in order to run correctly, the array should have all the valid
     * amounts of arguments that the command wants,
     *
     * returning null will opt the command out of argument checking
     *
     * @return the amount of arguments that a command requires
     */
    int[] argumentsNeeded();

    /**
     * The method that is called when the command is called
     *
     * @param event is the MessageCreateEvent that called the command
     */
    void action(MessageCreateEvent event, @Nullable String[] arguments);

    /**
     * The This is also called when the command is called, but is used to log the command
     *
     * @param event is the MessageCreateEvent that called the command
     */
    void log(MessageCreateEvent event);

}
