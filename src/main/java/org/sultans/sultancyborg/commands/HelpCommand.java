package org.sultans.sultancyborg.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import org.sultans.sultancyborg.core.SultanCyborgMain;
import org.sultans.sultancyborg.utils.STATIC;

public class HelpCommand implements Command{
    @Override
    public String invoker() {
        return "help";
    }

    @Override
    public String info() {
        return "Prints this info";
    }

    @Override
    public int[] argumentsNeeded() {
        return null;
    }

    @Override
    public void action(MessageCreateEvent event, String[] arguments) {
        event.getMessage()
            .getChannel()
            .flatMap(channel -> channel.createEmbed(spec -> {
                spec.setTitle("SultanCyborg Commands");
                SultanCyborgMain.commands.forEach(command -> spec.addField(STATIC.PREFIX + command.invoker(), command.info(), false));
                // Hard coded for the other bot...
                spec.addField("sult!join", "Has the bot join the VC", false);
                spec.addField("sult!leave", "Has the bot leave the VC", false);
                spec.addField("sult!mute", "Mute the bot", false);
                spec.addField("sult!deafen", "Deafen the bot", false);
                spec.addField("sult!play", "Have the bot play a song\n Must be in a channel first", false);
                spec.addField("sult!pause", "Pause the currently playing song", false);
                spec.addField("sult!resume", "Resume playing the currently paused song", false);
                spec.addField("sult!skip", "skip the current song", false);
                spec.addField("sult!quiet", "Quiet - Stop playing if bad sfx, use play after", false);
                spec.addField("sult!stop", "stop playing and remove all songs from the queue", false);
                spec.addField("sult!queue", "Show the current queue and what's playing right now", false);
                spec.addField("sult!remove", "Remove a song from the queue", false);
                spec.addField("sult!sfx", "Overlay another sound", false);
            }))
            .subscribe();
    }

    @Override
    public void log(MessageCreateEvent event) {
        //TODO;
    }
}
