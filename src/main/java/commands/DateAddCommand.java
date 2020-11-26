package commands;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.io.*;

public class DateAddCommand implements Command
{
    public String invoker()
    {
        return "dateAdd";
    }

    @Override
    public String info() {
        return null;
    }

    @Override
    public int[] argumentsNeeded() {
        return new int[] {2};
    }

    public void action(MessageCreateEvent event, String[] arguments)
    {
        JSONParser readingRainbow = new JSONParser();
        try
        {
            JSONObject jObj = (JSONObject) readingRainbow.parse(new FileReader("data/DatesAndNames.json"));

            jObj.put(arguments[0], arguments[1]);

            FileWriter file = new FileWriter("data/DatesAndNames.json");
            file.write(jObj.toJSONString());
            file.close();
            final MessageChannel channel = event.getMessage().getChannel().block();
            channel.createMessage("Complete!").block();
        }
        catch(Exception e)
        {
            final MessageChannel channel = event.getMessage().getChannel().block();
            channel.createMessage("WRONG! " + e.toString()).block();
        }

    }
    public void log(MessageCreateEvent event)
    {

    }
}
