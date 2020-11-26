package commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

public class PingCommand implements Command{
    public String invoker() {
        return "ping";
    }

    @Override
    public String info() {
        return "Just says pong back, also used for testing";
    }

    @Override
    public int[] argumentsNeeded() {
        return new int[]{0};
    }

    public void action(MessageCreateEvent event, String[] arguments) {
        final MessageChannel channel = event.getMessage().getChannel().block();
        if (arguments != null) {
            channel.createMessage("Pong!\n" + arguments.toString()).block();
        }
        else {
            channel.createMessage("Pong!\n").block();
        }
    }

    public void log(MessageCreateEvent event){
        System.out.println("Ping command was used");
    }
}