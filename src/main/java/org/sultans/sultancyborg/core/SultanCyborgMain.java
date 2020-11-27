package org.sultans.sultancyborg.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ImmutableMessageData;
import discord4j.discordjson.json.gateway.ChannelCreate;
import discord4j.discordjson.json.gateway.ImmutableMessageCreate;
import org.sultans.sultancyborg.commands.Command;
import org.sultans.sultancyborg.commands.DateAddCommand;
import org.sultans.sultancyborg.commands.MangaCommand;
import org.sultans.sultancyborg.commands.PingCommand;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.ArrayList;

public class SultanCyborgMain {
    public static final ArrayList<Command> commands = new ArrayList<>();
    public static GatewayDiscordClient client;

    public static void main(String[] args){
        //Add all the org.sultans.SultanCyborg.commands
        commands.add(new PingCommand());
        commands.add(new DateAddCommand());
        commands.add(new MangaCommand());
        //Create the client
        client = DiscordClientBuilder.create(args[0])
                .build()
                .login()
                .block();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    User self = event.getSelf();
                    client.getChannelById(Snowflake.of("445071996360065054"))
                            .cast(MessageChannel.class)
                            .flatMap(messageChannel -> messageChannel.createMessage("Bot Online"))
                            .then();
                });



        //The message listener, which filters out messages from bots and then sends them to the parser
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {
                    MessageParser.parseMessage(event);
        });
        client.onDisconnect().block();
    }
}
