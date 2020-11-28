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
            }))
            .subscribe();
    }

    @Override
    public void log(MessageCreateEvent event) {
        //TODO;
    }
}
