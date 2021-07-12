package org.sultans.sultancyborg.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MeirlCommand implements Command{
    public String invoker() {
        return "meirl";
    }

    @Override
    public String info() {
        return "Sends back a me_irl :)\n" +
                "Using \"sult!meirl count\" will tell you how many there are";
    }

    @Override
    public int[] argumentsNeeded() {
        return null;
    }

    public void action(MessageCreateEvent event, String[] arguments) {
        final MessageChannel channel = event.getMessage().getChannel().block();
        File meirlDirectory = new File("data/meirl");
        String[] meirlFiles = meirlDirectory.list();

        if (arguments == null) {
            int num = (int) (Math.random() * meirlFiles.length);
            String filename = meirlFiles[num];
            String filetype = filename.substring(filename.lastIndexOf("."));
            try {
                FileInputStream fis = new FileInputStream("data/meirl/" + filename);
                channel.createMessage(spec -> spec.addFile("me_irl" + filetype, fis)).block();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (arguments.length == 1 && arguments[0].equals("count")){
            channel.createMessage("There are " + meirlFiles.length +" me_irls. Tell Raymond to add more").block();
        }
    }

    public void log(MessageCreateEvent event){
        System.out.println("Someone asked for a me_irl");
    }
}
