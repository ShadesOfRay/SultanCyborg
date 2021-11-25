package org.sultans.sultancyborg.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;

public interface ChannelListener {
    /**
     * The channel id that you want to listen to, in String form
     *
     * @return the string of the channel that wants to be listened to
     */
    String channel();

    /**
     * The method that is called when the listener is called
     *
     * @param event is the MessageCreateEvent that called the listener
     */
    void action(MessageCreateEvent event);

    /**
     * The This is also called when the listener is called, but is used to log the command
     *
     * @param event is the MessageCreateEvent that called the command
     */
    void log(MessageCreateEvent event);

}

