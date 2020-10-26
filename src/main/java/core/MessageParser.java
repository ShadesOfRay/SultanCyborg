package core;

import commands.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import utils.STATIC;

public class MessageParser {

    private static String raw;
    private static String no_prefix;
    private static String[] arguments;

    public static void parseMessage(MessageCreateEvent event){
        //get the raw string from the message
        raw = event.getMessage().getContent();
        if (raw.startsWith(STATIC.PREFIX)){
            //checks if the command is standalone or has arguments
            if (raw.contains(" ")){
                int split = raw.indexOf(" ");
                no_prefix = raw.substring(STATIC.PREFIX.length(),split);
                arguments = raw.substring(split+1).split(" ");
            }
            else {
                no_prefix = raw.substring(STATIC.PREFIX.length());
                arguments = null;
            }

            //checks each command to see if the command matches
            //TODO make it check the arguments amount as well
            for(Command cmd : SultanCyborgMain.commands){
                if (no_prefix.equalsIgnoreCase(cmd.invoker())){
                    cmd.action(event, arguments);
                }
                else {
                    //TODO either print invalid or print help message
                }
            }
        }

    }
}
