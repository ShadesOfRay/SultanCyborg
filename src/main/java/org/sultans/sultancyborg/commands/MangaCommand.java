package org.sultans.sultancyborg.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Color;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sultans.sultancyborg.utils.RateLimitInterceptor;
import org.sultans.sultancyborg.utils.STATIC;

import java.io.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MangaCommand implements Command{
    private final String baseURL = "https://mangadex.org/";
    private final String cdnURL = "https://mangadex.org/api/v2/";
    private OkHttpClient client;
    private MessageChannel channel;
    private JSONParser parser;

    @Override
    public String invoker() {
        return "manga";
    }

    @Override
    public String info() {
        return "Interacts with the manga database\n" +
                "Use \"sult!manga help\" for more info";
    }

    @Override
    public int[] argumentsNeeded() {
        return null;
    }

    @Override
    public void action(MessageCreateEvent event, String[] arguments) {
        if (arguments != null) {
            channel = event.getMessage().getChannel().block();
            parser = new JSONParser();
            client = new OkHttpClient.Builder()
                    .addInterceptor(new RateLimitInterceptor())
                    .build();

            switch (arguments[0].toLowerCase()) {
                case "add":
                    if (arguments.length == 2) {
                        addManga(arguments[1]);
                    }
                    else {
                        channel.createMessage("Invalid amount of arguments").block();
                    }
                    break;
                case "update":
                    updateManga();
                    break;
                case "list":
                    listManga();
                    break;
                case "remove":
                    if (arguments.length == 2) {
                        removeManga(arguments[1]);
                    }
                    else {
                        channel.createMessage("Invalid amount of arguments").block();
                    }

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
        //checks if the string is a valid mangadex link
        if (mangaURL.startsWith("https://mangadex.org/title/")) {
            String mangaID = mangaURL.substring(27, mangaURL.lastIndexOf("/"));
            System.out.println(mangaID);
            try {
                //pulls the database
                JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
                if (!mainData.containsKey(mangaID)) {
                    //sends a request to the mangadex api for the manga in question
                    Request request = new Request.Builder()
                            .url(cdnURL + "manga/" + mangaID)
                            .build();
                    Response response = client.newCall(request).execute();
                    JSONObject temp = (JSONObject) parser.parse(response.body().charStream());
                    JSONObject data = (JSONObject) temp.get("data");
                    long responseCode = (long) temp.get("code");
                    //checks the response that mangadex gave
                    if (responseCode == 200) {
                        //if successful, get the data from mangadex
                        mainData.put(data.get("id"), data);
                        FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
                        databaseWriter.write(mainData.toJSONString());
                        databaseWriter.close();
                        //build a new request for the list of chapters
                        request = new Request.Builder()
                                .url(cdnURL + "manga/" + mangaID + "/chapters")
                                .build();
                        response = client.newCall(request).execute();
                        temp = (JSONObject) parser.parse(response.body().string());
                        responseCode = (long) temp.get("code");
                        //check if the chapters gave a good response code
                        if (responseCode == 200) {
                            JSONObject chapterData = (JSONObject) temp.get("data");
                            //write the data to its own file
                            File chapterDataFile = new File("data/chapters/" + data.get("id") + ".json");
                            FileWriter chapterDataWriter = new FileWriter(chapterDataFile);
                            chapterDataWriter.write(chapterData.toJSONString());
                            chapterDataWriter.close();
                            channel.createMessage(String.format("Successfully added \"%s\" to the database", data.get("title"))).block();

                        }
                        else {
                            channel.createMessage(String.format("Unexpected response code: %d %s", responseCode, (String) temp.get("status"))).block();
                        }
                    }
                    else {
                        channel.createMessage(String.format("Unexpected response code: %d %s", responseCode, (String) temp.get("status"))).block();
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
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            JSONObject newMainData = new JSONObject();
            channel.getLastMessage().flatMap(message -> message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4C")));
            channel.createMessage("Updating...").subscribe();
            Snowflake updatingMessageID = channel.getLastMessageId().get();

            mainData.forEach((key, value) -> {
                //get the new chapter json, check the size differences
                try {
                    JSONObject oldChapterData = (JSONObject) parser.parse(new FileReader("data/chapters/"+ key + ".json"));
                    Request request = new Request.Builder()
                            .url(cdnURL + "manga/" + key + "/chapters")
                            .build();
                    Response response = client.newCall(request).execute();
                    JSONObject newChapterObject = (JSONObject) parser.parse(response.body().charStream());
                    long responseCode = (long) newChapterObject.get("code");
                    if (responseCode == 200){
                        //update the main pages as well
                        request = new Request.Builder()
                                .url(cdnURL + "manga/" + key)
                                .build();
                        response = client.newCall(request).execute();
                        JSONObject temp = (JSONObject) parser.parse(response.body().charStream());
                        JSONObject data = (JSONObject) temp.get("data");
                        responseCode = (long) temp.get("code");
                        //checks the response that mangadex gave
                        if (responseCode == 200) {
                            //if successful, get the data from mangadex
                            newMainData.put(key, data);
                        }
                        else {
                            channel.createMessage(String.format("Unexpected response code: %d %s", responseCode, (String) newChapterObject.get("status"))).block();
                        }

                        JSONObject manga = (JSONObject) value;
                        JSONObject newChapterData = (JSONObject) newChapterObject.get("data");
                        JSONArray groupArray = (JSONArray) newChapterData.get("groups");
                        JSONObject actualGroups = new JSONObject();
                        groupArray.forEach(group ->{
                            JSONObject groupObj = (JSONObject) group;
                            actualGroups.put(groupObj.get("id"), groupObj.get("name"));
                        });
                        //gets the chapter arrays for both the old and new ones
                        JSONArray newChapterArray = (JSONArray) newChapterData.get("chapters");
                        JSONArray oldChapterArray = (JSONArray) oldChapterData.get("chapters");
                        //If the new chapter json is larger, then there are new chapters
                        //make a embedded message for the new chapter
                        if(oldChapterArray.size() < newChapterArray.size()){
                            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:ss");
                            int diff = newChapterArray.size() - oldChapterArray.size();
                            int oldPointer = 0;
                            for (int newPointer = 0; diff != 0; newPointer++){
                                //checks if the chapters are equal
                                JSONObject oldArrayChapter = (JSONObject)oldChapterArray.get(oldPointer);
                                JSONObject newArrayChapter = (JSONObject)newChapterArray.get(newPointer);
                                if (!oldArrayChapter.equals(newArrayChapter)){
                                    String language = (String) newArrayChapter.get("language");
                                    //Only send a message if it is an english chapter, possibly accept other languages later
                                    if (language.equals("gb")) {
                                        long groupID = (long) ((JSONArray) newArrayChapter.get("groups")).get(0);
                                        long timestamp = (long) newArrayChapter.get("timestamp");
                                        Date uploadDate = new Date(timestamp * 1000);
                                        channel.createEmbed(spec ->
                                                spec.setColor(Color.of((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)))
                                                        .setAuthor((String) ((JSONArray) manga.get("author")).get(0), null, null)
                                                        .setThumbnail((String) manga.get("mainCover"))
                                                        .setTitle((String) manga.get("title"))
                                                        .setUrl(baseURL + String.format("chapter/%d/", (long) newArrayChapter.get("id")))
                                                        .addField("Chapter", (String) newArrayChapter.get("chapter"), true)
                                                        .addField("Group", (String) actualGroups.get(groupID), true)
                                                        .addField("Uploaded on", dateFormat.format(uploadDate), false)
                                                        .addField("id", String.valueOf((long) manga.get("id")), true)
                                        ).block();
                                    }
                                    diff--;
                                }
                                else {
                                    oldPointer++;
                                }
                            }
                            FileWriter chapterDataWriter = new FileWriter("data/chapters/" + key + ".json");
                            chapterDataWriter.write(newChapterData.toJSONString());
                            chapterDataWriter.close();
                        }
                        //If the new chapter json is smaller, then there was a deleted chapter
                        //silently replace the chapter file with the new one
                        else if(oldChapterArray.size() > newChapterArray.size()){
                            FileWriter chapterDataWriter = new FileWriter("data/chapters/" + key + ".json");
                            chapterDataWriter.write(newChapterData.toJSONString());
                            chapterDataWriter.close();
                        }
                        //Chapter arrays are the same size, do nothing
                    }
                    else {
                        channel.createMessage(String.format("Unexpected response code: %d %s", responseCode, (String) newChapterObject.get("status"))).block();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //if the new data is the same size as the old data, nothing went wrong, clear to replace
            if (newMainData.size() == mainData.size()) {
                FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
                databaseWriter.write(newMainData.toJSONString());
                databaseWriter.close();
            }
            channel.getMessageById(updatingMessageID).flatMap(message -> message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4C"))).subscribe();
            channel.getMessageById(updatingMessageID).flatMap(Message::delete).subscribe();
            channel.createMessage("Finished updating").subscribe();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void listManga(){
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            //create a message for each key in the database
            mainData.forEach((key, value)-> {
                JSONObject manga = (JSONObject) value;
                try {
                    JSONObject chapterData = (JSONObject) parser.parse(new FileReader("data/chapters/" + key + ".json"));
                    JSONArray chapterList = (JSONArray) chapterData.get("chapters");
                    //JSONArray (JSONArray) manga.get("author");
                    JSONObject latestChapter = (JSONObject) chapterList.get(0);
                    long timestamp = (long)latestChapter.get("timestamp");
                    Date uploadDate = new Date(timestamp * 1000);
                    channel.typeUntil(
                    channel.createEmbed(spec ->
                        spec.setColor(Color.of((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)))
                            .setAuthor((String)((JSONArray)manga.get("author")).get(0),null ,null)
                            .setThumbnail((String) manga.get("mainCover"))
                            .setTitle((String) manga.get("title"))
                            .setUrl(baseURL + String.format("manga/%d/", (long) manga.get("id")))
                            .addField("Latest Chapter", (String)latestChapter.get("chapter"), true)
                            .addField("Last Updated", dateFormat.format(uploadDate), true )
                            .addField("id", String.valueOf((long) manga.get("id")), true)
                    )).subscribe();
                } catch (Exception e) {
                e.printStackTrace();
            }
        });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeManga(String id){
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            //check if the database has the key, then remove it if it does
            if (mainData.containsKey(id)){
                mainData.remove(id);
                FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
                databaseWriter.write(mainData.toJSONString());
                databaseWriter.close();
                channel.createMessage("Removed").block();
            }
            else {
                channel.createMessage("That manga is not in the database").block();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printHelp(){
        //creates an embedded message for the help message
        channel.createEmbed(spec ->
            spec.setColor(Color.BLACK)
                .setTitle(STATIC.PREFIX + "manga <command> [args]")
                .addField("help", "Prints this message", false)
                .addField("add [MangaDex manga Link]", "Adds the given manga from MangaDex to the database", false)
                .addField("remove [Manga ID]", "Removes the given manga from the database", false)
                .addField("list", "Lists all the manga in the database", false)
                .addField("update", "Updates all the manga in the database, and sends any new chapters", false)
        ).block();
    }
}
