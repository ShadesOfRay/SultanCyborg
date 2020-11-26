package org.sultans.sultancyborg.commands;
import java.io.FileReader;
import java.util.ArrayList;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import org.json.simple.*;
import org.json.simple.parser.*;

public class DateCommand implements Command{
    ArrayList<String> nameOfImportance = new ArrayList<String>();

    public String invoker()
    {
        return "date";
    }
    @Override
    public String info() {
        return "Give the info about this date";
    }

    @Override
    public int[] argumentsNeeded() {
        return new int[] {1};
    }
    public void action(MessageCreateEvent event, String[] arguments)
    {
        JSONParser readingRainbow = new JSONParser();

        try
        {
            JSONObject jSonA = (JSONObject) readingRainbow.parse(new FileReader(("DatesAndNames.json")));

            if(jSonA.containsValue(arguments[0]))
            {
                final MessageChannel channel = event.getMessage().getChannel().block();
                channel.createMessage((String) jSonA.get(arguments[0])).block();
            }
            else
            {
                throw new Exception();
            }
        }
        catch(Exception e)
        {
            final MessageChannel channel = event.getMessage().getChannel().block();
            channel.createMessage("WHAT A LOSER!").block();
        }
    }

    public void log(MessageCreateEvent event){
        System.out.println("Ping command was used");
    }
}
