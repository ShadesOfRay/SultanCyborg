package commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class MangaCommand implements Command{

    @Override
    public String invoker() {
        return null;
    }

    @Override
    public String info() {
        return null;
    }

    @Override
    public int[] argumentsNeeded() {
        return new int[0];
    }

    @Override
    public void action(MessageCreateEvent event, String[] arguments) {

    }

    @Override
    public void log(MessageCreateEvent event) {

    }
}
