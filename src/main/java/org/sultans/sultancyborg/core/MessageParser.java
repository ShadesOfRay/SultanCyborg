package org.sultans.sultancyborg.core;

import discord4j.core.object.entity.channel.Channel;
import org.sultans.sultancyborg.commands.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import org.sultans.sultancyborg.listeners.ChannelListener;
import org.sultans.sultancyborg.utils.STATIC;

public class MessageParser {

    private static String raw;
    private static String no_prefix;
    private static String[] arguments;
    private static int temp = 0;

    /**
     * The parse message function, which checks if a message contains the prefix to call the bot, and if that uses a command
     *
     *
     * @param event is the event of the message
     * @return 1 if the message contained a valid command, 0 if it did not
     */
    public static int parseMessage(MessageCreateEvent event){
        //for fun
        if (event.getMessage().getAuthor().get().getId().asString().equals("152897641942876162")) {
            if (temp == 0) {
                event.getMessage().getChannel()
                        .flatMap(channel -> channel.createMessage("Hey Zach, Congrats on the engagement"))
                        .subscribe();
            }
        }
        temp = (temp + 1) % 5;

        //also for fun
        if (event.getMessage().getContent().startsWith("https://twitter.com/YakuzaFriday/")) {
            event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage("Friday at last..."))
                    .subscribe();
        }

        //get the raw string from the message
        raw = event.getMessage().getContent();
        if (raw.toLowerCase().startsWith(STATIC.PREFIX)) {
            //checks if the command is standalone or has arguments
            if (raw.contains(" ")) {
                int split = raw.indexOf(" ");
                no_prefix = raw.substring(STATIC.PREFIX.length(), split);
                arguments = raw.substring(split+1).split(" ");
            }
            else {
                no_prefix = raw.substring(STATIC.PREFIX.length());
                arguments = null;
            }

            //checks each command to see if the command matches
            for(Command cmd : SultanCyborgMain.commands) {
                if (no_prefix.equalsIgnoreCase(cmd.invoker())) {
                    if (cmd.argumentsNeeded() != null) {
                        for (int args : cmd.argumentsNeeded()) {
                            if ((args == 0 && arguments == null) ||args == arguments.length) {
                                cmd.action(event, arguments);
                                cmd.log(event);
                                return 1;
                            }
                        }

                    }
                    else {
                        cmd.action(event, arguments);
                        cmd.log(event);
                        return 1;
                    }
                    final MessageChannel channel = event.getMessage().getChannel().block();
                    channel.createMessage("Wrong amount of arguments").block();
                    return 0;
                }
            }

            final MessageChannel channel = event.getMessage().getChannel().block();
            channel.createMessage("That's crazy, cuz I didn't ask").block();
            return 0;
        }
        else {
            for (ChannelListener channelListener: SultanCyborgMain.channelListeners) {
                if (event.getMessage().getChannelId().asString().equals(channelListener.channel())) {
                    channelListener.action(event);
                }
            }
        }
        return 0;
    }
}
