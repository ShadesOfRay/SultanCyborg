package core;

import commands.Command;
import commands.PingCommand;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.ArrayList;

public class SultanCyborgMain {
    public static final ArrayList<Command> commands= new ArrayList<>();
    public static void main(String[] args){
        //make a storage for all the commands
        commands.add(new PingCommand());
        //make a storage for all the listeners...?

        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();
        client.onDisconnect().block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {
                    MessageParser.parseMessage(event);
        });


    }
}
