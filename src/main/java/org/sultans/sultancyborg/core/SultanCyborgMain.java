package org.sultans.sultancyborg.core;

import org.sultans.sultancyborg.commands.Command;
import org.sultans.sultancyborg.commands.DateAddCommand;
import org.sultans.sultancyborg.commands.MangaCommand;
import org.sultans.sultancyborg.commands.PingCommand;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.ArrayList;

public class SultanCyborgMain {
    public static final ArrayList<Command> commands= new ArrayList<>();
    public static void main(String[] args){
        //Add all the org.sultans.SultanCyborg.commands
        commands.add(new PingCommand());
        commands.add(new DateAddCommand());
        commands.add(new MangaCommand());
        //Create the client
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build().login().block();


        //The message listener, which filters out messages from bots and then sends them to the parser
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {
                    MessageParser.parseMessage(event);
        });

        client.onDisconnect().block();
    }
}
