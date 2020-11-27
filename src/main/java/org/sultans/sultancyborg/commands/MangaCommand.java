package org.sultans.sultancyborg.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sultans.sultancyborg.utils.STATIC;

import java.io.*;

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
        //TODO make it update the main page too maybe
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            channel.typeUntil(channel.createMessage("Finished updating")).subscribe();
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
                        JSONObject manga = (JSONObject) value;
                        JSONObject newChapterData = (JSONObject) newChapterObject.get("data");
                        JSONArray groupArray = (JSONArray) newChapterData.get("groups");
                        JSONObject actualGroups = new JSONObject();
                        groupArray.forEach(group ->{
                            JSONObject groupObj = (JSONObject) group;
                            actualGroups.put(groupObj.get("id"), groupObj.get("name"));
                        });

                        JSONArray newChapterArray = (JSONArray) newChapterData.get("chapters");
                        JSONArray oldChapterArray = (JSONArray) oldChapterData.get("chapters");
                        //If the new chapter json is larger, then there are new chapters
                        //make a embedded message for the new chapter
                        if(oldChapterArray.size() < newChapterArray.size()){
                            int diff = newChapterArray.size() - oldChapterArray.size();
                            int oldPointer = 0;
                            for (int newPointer = 0; diff != 0; newPointer++){
                                //checks if the chapters are equal
                                JSONObject oldArrayChapter = (JSONObject)oldChapterArray.get(oldPointer);
                                JSONObject newArrayChapter = (JSONObject)newChapterArray.get(newPointer);
                                if (!oldArrayChapter.equals(newArrayChapter)){
                                    long groupID = (long) ((JSONArray)newArrayChapter.get("groups")).get(0);
                                    channel.createEmbed(spec ->
                                        spec.setColor(Color.RED)
                                            .setAuthor((String)((JSONArray)manga.get("author")).get(0),null ,null)
                                            .setThumbnail((String) manga.get("mainCover"))
                                            .setTitle((String) manga.get("title"))
                                            .setUrl(baseURL + String.format("chapter/%d/", (long) newArrayChapter.get("id")))
                                            .addField("Chapter", (String)newArrayChapter.get("chapter"), true)
                                            .addField("Group", (String) actualGroups.get(groupID), true)
                                            .addField("id", String.valueOf((long) manga.get("id")), true)
                                    ).block();
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
            channel.createMessage("Finished updating").subscribe();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void listManga(){
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            mainData.forEach((key, value)-> {
                JSONObject manga = (JSONObject) value;
                //JSONArray (JSONArray) manga.get("author");
                channel.createEmbed(spec ->
                    spec.setColor(Color.RED)
                        .setAuthor((String)((JSONArray)manga.get("author")).get(0),null ,null)
                        .setThumbnail((String) manga.get("mainCover"))
                        .setTitle((String) manga.get("title"))
                        .setUrl(baseURL + String.format("manga/%d/", (long) manga.get("id")))
                        .addField("id", String.valueOf((long) manga.get("id")), true)
                ).block();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeManga(String id){
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
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
