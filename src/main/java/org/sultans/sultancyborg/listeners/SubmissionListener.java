package org.sultans.sultancyborg.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class SubmissionListener implements ChannelListener {

    @Override
    public String channel() {
        return "914524325225300030";
    }

    @Override
    public void action(MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();

        event.getMessage().getAttachments().stream()
                .map(attachment -> attachment.getData().url())
                .forEach(url -> {
                    try {
                        BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
                        String filename = url.substring(url.lastIndexOf('/')+1);
                        FileOutputStream fileOS = new FileOutputStream("data/meirl/" + filename);
                        byte data[] = new byte[1024];
                        int byteContent;
                        while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                            fileOS.write(data, 0, byteContent);
                        }
                        channel.createMessage(String.format("Successfully downloaded `%s`", filename)).subscribe();
                    } catch (IOException e) {
                        System.out.println("Error downloading attachment");
                    }
                });
    }

    @Override
    public void log(MessageCreateEvent event) {
    //TODO
    }
}
