package org.sultans.sultancyborg.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class SubmissionListener implements ChannelListener{

    @Override
    public String channel() {
        return "913588739681419294";
    }

    @Override
    public void action(MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();
        channel.createMessage("haha yes").subscribe();
    }

    @Override
    public void log(MessageCreateEvent event) {

    }
}
