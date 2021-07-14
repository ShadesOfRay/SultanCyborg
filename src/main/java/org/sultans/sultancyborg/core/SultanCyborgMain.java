package org.sultans.sultancyborg.core;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.sultans.sultancyborg.commands.*;

import java.util.ArrayList;

public class SultanCyborgMain {
    public static final ArrayList<Command> commands = new ArrayList<>();
    public static GatewayDiscordClient client;

    public static void main(String[] args){
        //Add all the org.sultans.SultanCyborg.commands
        commands.add(new PingCommand());
        commands.add(new DateAddCommand());
        //commands.add(new MangaCommand());
        commands.add(new HelpCommand());
        commands.add(new MeirlCommand());
        //Create the client
        client = DiscordClientBuilder.create(args[0])
                .build()
                .login()
                .block();

        /*
        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    User self = event.getSelf();
                    client.getChannelById(Snowflake.of("445071996360065054"))
                            .cast(MessageChannel.class)
                            .flatMap(messageChannel -> messageChannel.createMessage("Bot Online"))
                            .subscribe();
                });

         */


        //The message listener, which filters out messages from bots and then sends them to the parser
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {
                    MessageParser.parseMessage(event);
        });

        //congrats on the engagement
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> user.getId().asString().equals("152897641942876162")).orElse(false))
                .flatMap(message -> message.getMessage().getChannel())
                .flatMap(channel -> channel.createMessage("Hey Zach, Congrats on the engagement"))
                .subscribe();


        client.onDisconnect().block();
    }
}
