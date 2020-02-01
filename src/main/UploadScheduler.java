package main;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UploadScheduler {

    UploadScheduler() throws NoSuchFieldException, IllegalAccessException {

        try {
            YouTube youtube = ApiUtils.getService();
            System.out.println("youtube = " + youtube.search().list("id"));
            YouTube.Search.List request = youtube.search()
                    .list("id");
            SearchListResponse response = request
                    .setForMine(true)
                    .setMaxResults(25L)
                    .setOrder("date")
                    .setType("video")
                    .execute();
            Main.out(response.toPrettyString());
            List<SearchResult> items = response.getItems();
            ArrayList<Date> queue = new ArrayList<>();
            String formatted;
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm:ss aa");
            // items.size() should always be <= 1
            for (SearchResult item : items) {
                VideoListResponse vlr = youtube.videos().list("snippet,status")
                        .setId(item.getId().getVideoId())
                        .setMaxResults(1L)
                        .execute();
                VideoStatus status = vlr.getItems().get(0).getStatus();
                VideoSnippet snippet = vlr.getItems().get(0).getSnippet();
                if (status.getPublishAt() != null) {
                    if (status.getPrivacyStatus().equals("private")) {
                        Date date = new Date(status.getPublishAt().getValue());
                        formatted = sdf.format(date);
                        queue.add(date);
                    } else {
                        //Break the loop because once we get to the public videos that don't have a date,
                        //we're probably done.
                        break;
                    }
                }
            }
            Collections.sort(queue);
            if (queue.size() > 0) {
                Date latest = queue.get(queue.size() - 1);
                Main.out("Latest video upload: " + latest);
                Calendar cal = Calendar.getInstance();
                cal.setTime(latest);

                File schedule = new File(Config.getLibraryFolder() + "/upload_schedule.json");
                Gson gson = new Gson();
                UploadSchedule uploadSchedule = gson.fromJson(new FileReader(schedule), UploadSchedule.class);

                int latestHour = cal.get(Calendar.HOUR_OF_DAY);
                int latestMinute = cal.get(Calendar.MINUTE);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                String[] weekDays = new String[]{"", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
                String weekDay = weekDays[dayOfWeek];
                Field field = uploadSchedule.getClass().getDeclaredField(weekDay);
                String[] data = (String[]) field.get(uploadSchedule);

                String latestTimeFormatted = latestHour + ":" + (latestMinute < 10 ? "0" : "") + latestMinute;
                Main.out("Looking for " + latestTimeFormatted + " (" + cal.get(Calendar.DAY_OF_WEEK) + ") in upload_schedule.json...");
                Main.out("Found data for " + weekDay + ": " + Arrays.toString(data));
                Main.out("Latest video: " + cal.getTime());

                Date p = null;

                int j = 0;
                for (String time : data) {
                    j++;
                    if (time.equals(latestTimeFormatted)) {
                        //We found where the latest video is on our schedule.
                        //Now, find where the next video should be.
                        int index;
                        Main.out("j = " + j);
                        Main.out("time = " + time);
                        Main.out("latestTimeFormatted = " + latestTimeFormatted);
                        if (j >= data.length) {
                            //Go to the next day.
                            dayOfMonth++;
                            dayOfWeek++;
                            if (dayOfWeek > 7) dayOfWeek = 1;
                            Main.out("Choosing the first time of the next day (" + weekDays[dayOfWeek] + ").");
                            index = 0;
                        } else {
                            Main.out("Choosing the next time of the current day (" + weekDay + " at " + data[j] + ").");
                            index = j;
                        }
                        Field f2 = uploadSchedule.getClass().getDeclaredField(weekDays[dayOfWeek]);
                        String[] d2 = (String[]) f2.get(uploadSchedule);
                        Main.out("Found data for " + weekDays[dayOfWeek] + ": " + Arrays.toString(d2));
                        Main.out("Currently looking at scheduling the video for " + weekDays[dayOfWeek] + " at " + d2[index]);
                        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(d2[index].split(":")[0]));
                        cal.set(Calendar.MINUTE, Integer.parseInt(d2[index].split(":")[1]));
                        p = cal.getTime();
                        break;
                    }
                }
                if (p == null) UploadVideo.onDateTimeGathered(null);
                else UploadVideo.onDateTimeGathered(new DateTime(p));
            } else {
                UploadVideo.onDateTimeGathered(null);
            }

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Main.err("Error with video scheduler: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof GoogleJsonResponseException) {
                Main.err("Google JSON Response Error: " + e.getMessage() + "\nDetails: " + ((GoogleJsonResponseException) e).getDetails());
            }
            Main.err("Error with video scheduler: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IllegalAccessException, NoSuchFieldException {
        new UploadScheduler();
    }
}
