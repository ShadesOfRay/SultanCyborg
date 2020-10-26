package commands;
import java.util.ArrayList;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class DateCommand implements Command{
    ArrayList<String> nameOfImportance = new ArrayList<String>();

    public String invoker()
    {
        return "date " + nameOfImportance;
    }

    @Override
    public String info() {
        return null;
    }

    @Override
    public int[] argumentsNeeded() {
        return new int[0];
    }

    public void action(MessageCreateEvent event, String[] arguments)
    {
        final MessageChannel channel = event.getMessage().getChannel().block();
        channel.createMessage("Pong!").block();
    }

    public void log(MessageCreateEvent event){
        System.out.println("Ping command was used");
    }
}
