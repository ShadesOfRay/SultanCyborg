package commands;
import java.util.ArrayList;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class DateCommand implements Command{
    ArrayList<String> nameOfImportance = new ArrayList<String>();

    public String invoker()
    {
        return "!date " + nameOfImportance;
    }

    public void action(MessageCreateEvent event)
    {
        final MessageChannel channel = event.getMessage().getChannel().block();
        channel.createMessage("Pong!").block();
    }

    public void log(MessageCreateEvent event){
        System.out.println("Ping command was used");
    }
}
