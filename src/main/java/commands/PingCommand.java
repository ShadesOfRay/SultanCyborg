package commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class PingCommand implements Command{
    public String invoker() {
        return "ping";
    }

    public void action(MessageCreateEvent event) {
        final MessageChannel channel = event.getMessage().getChannel().block();
        channel.createMessage("Pong!").block();
    }

    public void log(MessageCreateEvent event){
        System.out.println("Ping command was used");
    }
}