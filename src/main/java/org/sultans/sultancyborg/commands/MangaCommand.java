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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MangaCommand implements Command {
    private final String baseURL = "https://mangadex.org/";
    private final String cdnURL = "https://api.mangadex.org/";
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
                    } else {
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
                    } else {
                        channel.createMessage("Invalid amount of arguments").block();
                    }
                    break;
                default:
                    printHelp();
            }
        } else {
            printHelp();
        }
    }

    @Override
    public void log(MessageCreateEvent event) {
        //todo
    }

    private void addManga(String mangaURL) {
        //checks if the string is a valid mangadex link4
        if (mangaURL.startsWith(baseURL + "title/")) {
            String mangaID = mangaURL.substring(27, mangaURL.lastIndexOf("/"));
            System.out.println(mangaID);
            try {
                //pulls the database
                JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
                if (!mainData.containsKey(mangaID)) {
                    //sends a request to the mangadex api for the manga in question
                    Request request = new Request.Builder()
                            .url(cdnURL + "manga/" + mangaID + "?includes[]=author&includes[]=cover_art")
                            .build();
                    Response response = client.newCall(request).execute();
                    response.code();
                    //checks the response that mangadex gave
                    if (response.code() == 200) {
                        JSONObject temp = (JSONObject) parser.parse(response.body().charStream());
                        String result = (String) temp.get("result");
                        if (!result.equals("ok")) {
                            channel.createMessage(String.format("Unexpected result: %d %s", result, (String) temp.get("message"))).block();
                        } else {
                            //if successful, get the data from mangadex
                            JSONObject data = (JSONObject) temp.get("data");

                            mainData.put(data.get("id"), data);
                            /*
                            FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
                            databaseWriter.write(mainData.toJSONString());
                            databaseWriter.close();
                            */
                            //build a new request for the list of chapters
                            request = new Request.Builder()
                                    .url(cdnURL + "manga/" + mangaID + "/aggregate?translatedLanguage[]=en")
                                    .build();
                            response = client.newCall(request).execute();

                            //check if the chapters gave a good response code
                            if (response.code() == 200) {
                                temp = (JSONObject) parser.parse(response.body().string());
                                result = (String) temp.get("result");
                                if (!result.equals("ok")) {
                                    channel.createMessage(String.format("Unexpected result: %d %s", result, (String) temp.get("message"))).block();
                                } else {
                                    //JSONObject chapterData = (JSONObject) temp.get("data");
                                    JSONObject volumes = (JSONObject) temp.get("volumes");
                                    //write the data
                                    JSONObject chapterJSON = new JSONObject();
                                    volumes.forEach((volumeKey, volumeObj) -> {
                                        JSONObject volume = (JSONObject) volumeObj;
                                        JSONObject chapters = (JSONObject) volume.get("chapters");
                                        chapters.forEach((chapterKey, chapterObj) -> {
                                            JSONObject chapter = (JSONObject) chapterObj;
                                            chapterJSON.put(chapter.get("id"), chapter.get("chapter"));
                                        });
                                    });


                                    data.put("chapters", chapterJSON);
                                    mainData.put(mangaID, data);
                                    FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
                                    databaseWriter.write(mainData.toJSONString());
                                    databaseWriter.close();

                                    String title = (String) ((JSONObject) ((JSONObject) data.get("attributes")).get("title")).get("en");
                                    channel.createMessage(String.format("Successfully added `%s` to the database", title)).block();
                                }

                            } else {
                                channel.createMessage(String.format("Unexpected response code: %d", response.code())).block();
                            }
                        }
                    } else {
                        channel.createMessage(String.format("Unexpected response code: %d", response.code())).block();
                    }
                } else {
                    channel.createMessage("Manga already in database").block();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            channel.createMessage("That's not even a valid MangaDex link dood").block();
        }
    }

    private void updateManga() {
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            JSONObject newMainData = new JSONObject();
            channel.getLastMessage()
                    .flatMap(message -> message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4C")))
                    .subscribe();
            channel.createMessage("Updating...").subscribe();
            //for some reason this is the message before the Updating... message
            Snowflake updatingMessageID = channel.getLastMessageId().get();

            mainData.forEach((key, value) -> {
                //get the new chapter json, check the size differences
                JSONObject newMangaData = new JSONObject();
                try {
                    JSONObject manga = (JSONObject) value;
                    Request request = new Request.Builder()
                            .url(cdnURL + "manga/" + key + "/aggregate?translatedLanguage[]=en")
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.code() != 200) {
                        System.out.println("here!");
                        channel.createMessage(String.format("Unexpected response code: %d", response.code())).block();
                    } else {
                        JSONObject newChapterObject = (JSONObject) parser.parse(response.body().charStream());
                        String result = (String) newChapterObject.get("result");
                        if (result.equals("ok")) {
                            //update the main pages as well
                            request = new Request.Builder()
                                    .url(cdnURL + "manga/" + key + "?includes[]=author&includes[]=cover_art")
                                    .build();
                            response = client.newCall(request).execute();
                            JSONObject temp = (JSONObject) parser.parse(response.body().charStream());
                            if (response.code() != 200) {
                                channel.createMessage(String.format("Unexpected response code: %d", response.code())).block();
                            } else {
                                JSONObject data = (JSONObject) temp.get("data");
                                result = (String) temp.get("result");
                                //checks the response that mangadex gave
                                if (result.equals("ok")) {
                                    //if successful, get the data from mangadex
                                    newMangaData = data;
                                } else {
                                    channel.createMessage(String.format("Unexpected response code: %d %s", result, (String) newChapterObject.get("status"))).block();
                                }
                            }
                            //JSONObject chapterData = (JSONObject) temp.get("data");
                            JSONObject volumes = (JSONObject) newChapterObject.get("volumes");
                            JSONObject oldChapterJSON = (JSONObject) manga.get("chapters");
                            //write the data
                            JSONObject finalNewMangaData = newMangaData;
                            volumes.forEach((volumeKey, volumeObj) -> {
                                JSONObject volume = (JSONObject) volumeObj;
                                JSONObject chapters = (JSONObject) volume.get("chapters");
                                chapters.forEach((chapterKey, chapterObj) -> {
                                    JSONObject chapter = (JSONObject) chapterObj;
                                    String chapterID = (String) chapter.get("id");
                                    if (!oldChapterJSON.containsKey(chapterID)) {
                                        oldChapterJSON.put(chapterID, chapter.get("chapter"));
                                        try {
                                            Request chapterRequest = new Request.Builder()
                                                    .url(cdnURL + "chapter/" + chapterID + "?includes[]=scanlation_group")
                                                    .build();

                                            Response chapterResponse = client.newCall(chapterRequest).execute();
                                            JSONObject chapterData = (JSONObject) parser.parse(chapterResponse.body().charStream());
                                            if (chapterResponse.code() != 200) {
                                                channel.createMessage(String.format("Unexpected response code: %d", chapterResponse.code())).block();
                                            } else {
                                                JSONObject data = (JSONObject) chapterData.get("data");
                                                String chapterResult = (String) chapterData.get("result");
                                                //checks the response that mangadex gave
                                                if (chapterResult.equals("ok")) {
                                                    //Make the channel response
                                                    JSONArray mangaRelationships = (JSONArray) finalNewMangaData.get("relationships");
                                                    JSONObject authorAttributes = (JSONObject) ((JSONObject) mangaRelationships.get(0)).get("attributes");
                                                    JSONObject coverAttributes = (JSONObject) ((JSONObject) mangaRelationships.get(2)).get("attributes");
                                                    String coverUrl = (String) coverAttributes.get("fileName");
                                                    String author = (String) authorAttributes.get("name");
                                                    JSONObject attributes = (JSONObject) data.get("attributes");
                                                    String chapterNum = (String) attributes.get("chapter");
                                                    String chapterDesc = (String) attributes.get("title");
                                                    JSONArray relationships = (JSONArray) data.get("relationships");
                                                    JSONObject groupAttributes = (JSONObject) ((JSONObject) relationships.get(0)).get("attributes");
                                                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:ss");
                                                    Date uploadDate = Date.from((OffsetDateTime.parse((String) attributes.get("createdAt")).toInstant()));
                                                    channel.createEmbed(spec ->
                                                            spec.setColor(Color.of((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)))
                                                                    .setAuthor(author, null, null)
                                                                    .setThumbnail("https://uploads.mangadex.org/covers/" + String.format("%s/%s.512.jpg", key, coverUrl))
                                                                    .setTitle((String) ((JSONObject) ((JSONObject) finalNewMangaData.get("attributes")).get("title")).get("en"))
                                                                    .setUrl(baseURL + String.format("chapter/%s", data.get("id")))
                                                                    .setDescription(chapterDesc == null? "": chapterDesc)
                                                                    .addField("Chapter", chapterNum == null? "N/A": chapterNum, true)
                                                                    .addField("Group", (String) groupAttributes.get("name"), true)
                                                                    .addField("Uploaded on", dateFormat.format(uploadDate), false)
                                                                    .addField("id", (String) finalNewMangaData.get("id"), true)
                                                    ).block();
                                                } else {
                                                    channel.createMessage(String.format("Unexpected response code: %s %s", chapterResult, (String) newChapterObject.get("status"))).block();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                finalNewMangaData.put("chapters", oldChapterJSON);
                            });
                            newMainData.put(key, finalNewMangaData);
                        } else {
                            channel.createMessage(String.format("Unexpected response code: %s %s", result, (String) newChapterObject.get("status"))).block();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //if the new data is the same size as the old data, nothing went wrong, clear to replace
            FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
            databaseWriter.write(newMainData.toJSONString());
            databaseWriter.close();
            //again, for some reason the updating manga message does not count, so it deletes the one right after
            channel.getMessagesAfter(updatingMessageID).take(1).flatMap(Message::delete).subscribe();
            channel.createMessage("Finished updating").subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listManga() {
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            //create a message for each key in the database
            mainData.forEach((key, value) -> {
                JSONObject manga = (JSONObject) value;
                try {

                    JSONArray mangaRelationships = (JSONArray) manga.get("relationships");
                    JSONObject mangaAttributes = (JSONObject) manga.get("attributes");
                    JSONObject authorAttributes = (JSONObject) ((JSONObject) mangaRelationships.get(0)).get("attributes");
                    JSONObject coverAttributes = (JSONObject) ((JSONObject) mangaRelationships.get(2)).get("attributes");
                    String coverUrl = (String) coverAttributes.get("fileName");
                    String author = (String) authorAttributes.get("name");
                    Date uploadDate = Date.from((OffsetDateTime.parse((String) mangaAttributes.get("updatedAt")).toInstant()));

                    channel.typeUntil(
                            channel.createEmbed(spec ->
                                    spec.setColor(Color.of((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)))
                                            .setAuthor(author, null, null)
                                            .setThumbnail("https://uploads.mangadex.org/covers/" + String.format("%s/%s.512.jpg", key, coverUrl))
                                            .setTitle((String) ((JSONObject) ((JSONObject) manga.get("attributes")).get("title")).get("en"))
                                            .setUrl(baseURL + String.format("manga/%s", manga.get("id")))
                                            //.addField("Latest Chapter", (String) mangaAttributes.get("lastChapter"), true)
                                            .addField("Last Updated", dateFormat.format(uploadDate), true)
                                            .addField("id", (String) manga.get("id"), true)
                            )).subscribe();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeManga(String id) {
        try {
            JSONObject mainData = (JSONObject) parser.parse(new FileReader("data/mangaDatabase.json"));
            //check if the database has the key, then remove it if it does
            if (mainData.containsKey(id)) {
                mainData.remove(id);
                FileWriter databaseWriter = new FileWriter("data/mangaDatabase.json");
                databaseWriter.write(mainData.toJSONString());
                databaseWriter.close();
                channel.createMessage("Removed").block();
            } else {
                channel.createMessage("That manga is not in the database").block();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printHelp() {
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
