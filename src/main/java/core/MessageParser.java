package core;

import commands.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import utils.STATIC;

public class MessageParser {

    private static String raw;
    private static String no_prefix;
    private static String post;

    public static void parseMessage(MessageCreateEvent event){
        raw = event.getMessage().getContent();
        if (raw.startsWith(STATIC.PREFIX)){
            if (raw.contains(" ")){
                int split = raw.indexOf(" ");
                no_prefix = raw.substring(STATIC.PREFIX.length(),split);
                post = raw.substring(split);
            }
            else {
                no_prefix = raw.substring(STATIC.PREFIX.length());
                post = "";
            }
        }
        for(Command cmd : SultanCyborgMain.commands){
            String[] arguments;
            if (no_prefix.equalsIgnoreCase(cmd.invoker())){
                cmd.action(event, post.split(" "));
            }
        }
    }
}
