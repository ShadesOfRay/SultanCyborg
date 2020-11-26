package commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class MangaCommand implements Command{

    private String baseurl = "https://mangadex.org/api/v2/";
    private OkHttpClient client;
    private MessageChannel channel;
    JSONParser parser;

    @Override
    public String invoker() {
        return "manga";
    }

    @Override
    public String info() {
        //TODO
        return null;
    }

    @Override
    public int[] argumentsNeeded() {
        return null;
    }

    @Override
    public void action(MessageCreateEvent event, String[] arguments) {
        if (arguments != null) {
            channel = event.getMessage().getChannel().block();
            client = new OkHttpClient();
            parser = new JSONParser();

            switch (arguments[0].toLowerCase()) {
                case "add":
                    addManga(arguments[1]);
                    break;
                case "update":
                    updateManga();
                    break;
                case "list":
                    listManga();
                    break;
                case "remove":
                    removeManga();
                default:
                    printHelp();
            }
        }
        else {
            printHelp();
        }
    }

    @Override
    public void log(MessageCreateEvent event) {
        //todo
    }

    private void addManga(String mangaURL) {
        if (mangaURL.startsWith("https://mangadex.org/title/")) {
            String mangaID = mangaURL.substring(27, mangaURL.lastIndexOf("/"));
            System.out.println(mangaID);

            try {
                //TODO CHECK IF ITS ALREADY IN THE DATABASE
                JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
                if (!mainData.containsKey(mangaID)) {
                    Request request = new Request.Builder()
                            .url(baseurl + "manga/" + mangaID)
                            .build();
                    Response response = client.newCall(request).execute();
                    JSONObject temp = (JSONObject) parser.parse(response.body().charStream());
                    JSONObject data = (JSONObject) temp.get("data");
                    long responseCode = (long) temp.get("code");

                    if (responseCode == 200) {

                        mainData.put(data.get("id"), data);
                        FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
                        databaseWriter.write(mainData.toJSONString());
                        databaseWriter.close();

                        request = new Request.Builder()
                                .url(baseurl + "manga/" + mangaID + "/chapters")
                                .build();

                        response = client.newCall(request).execute();
                        temp = (JSONObject) parser.parse(response.body().string());
                        responseCode = (long) temp.get("code");
                        if (responseCode == 200) {
                            JSONObject chapterData = (JSONObject) temp.get("data");

                            File chapterDataFile = new File("data/chapters/" + data.get("id") + ".json");
                            FileWriter chapterDataWriter = new FileWriter(chapterDataFile);
                            chapterDataWriter.write(chapterData.toJSONString());
                            chapterDataWriter.close();

                            channel.createMessage("Should have worked...").block();

                        } else {
                            channel.createMessage("Unexpected response code: " + responseCode).block();
                        }
                    } else {
                        channel.createMessage("Unexpected response code: " + responseCode).block();
                    }
                }
                else {
                    channel.createMessage("Manga already in database").block();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            channel.createMessage("That's not even a valid MangaDex link dood").block();
        }
    }

    private void updateManga(){

    }

    private void listManga(){

    }

    private void removeManga(){

    }

    private void printHelp(){

    }

}
