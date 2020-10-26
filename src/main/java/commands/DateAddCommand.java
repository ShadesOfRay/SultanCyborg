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
        return "!dateAdd";
    }
    public void action(MessageCreateEvent event)
    {
        JSONParser readingRainbow = new JSONParser();
        try
        {
            JSONObject jObj = (JSONObject) readingRainbow.parse(new FileReader(("DatesAndNames.json")));

            jObj.put("date", "message");

            FileWriter file = new FileWriter("DatesAndNames.json");
            file.write(jObj.toJSONString());
            final MessageChannel channel = event.getMessage().getChannel().block();
            channel.createMessage("Complete!").block();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

    }
    public void log(MessageCreateEvent event)
    {

    }
}
