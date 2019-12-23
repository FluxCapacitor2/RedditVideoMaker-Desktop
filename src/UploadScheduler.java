import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public class UploadScheduler extends JDialog {
    private DatePicker datePicker;
    private TimePicker timePicker;
    private JPanel panel;
    private JButton doneBtn;
    private JPanel dpp;
    private JPanel tpp;

    private JFrame f;

    UploadScheduler() throws NoSuchFieldException, IllegalAccessException {

        try {
            YouTube youtube = ApiUtils.getService();
            System.out.println("youtube = " + youtube.search().list("id"));
            YouTube.Search.List request = youtube.search()
                    .list("id");
            SearchListResponse response = request
                    //.setChannelId("UC9yNvUdCqYvfo4qqxiSiKaA")
                    .setForMine(true)
                    .setMaxResults(25L)
                    .setOrder("date")
                    .setType("video")
                    .execute();
            Main.out(response.toPrettyString());
            List<SearchResult> items = response.getItems();
            ArrayList<Date> queue = new ArrayList<>();
            ArrayList<Video> queueVideos = new ArrayList<>();
            String formatted;
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm:ss aa");
            // items.size() should always be <= 1
            for (SearchResult item : items) {
                //Main.out("item = " + item);
                //Main.out("item.getId().getVideoId() = " + item.getId().getVideoId());
                VideoListResponse vlr = youtube.videos().list("snippet,status")
                        .setId(item.getId().getVideoId())
                        .setMaxResults(1L)
                        .execute();
                VideoStatus status = vlr.getItems().get(0).getStatus();
                VideoSnippet snippet = vlr.getItems().get(0).getSnippet();
                //Main.out("status = " + status);
                //Main.out("status.getPublishAt() = " + status.getPublishAt());
                if (status.getPublishAt() != null) {
                    if (status.getPrivacyStatus().equals("private")) {
                        Main.out("[PRIVATE][SCHEDULED]");
                        Main.out("\tLink: https://youtube.com/watch?v=" + item.getId().getVideoId());
                        Main.out("\tTitle: " + snippet.getTitle());
                        Main.out("\tChannel: " + snippet.getChannelTitle() + " (" +
                                snippet.getChannelId() + ")");
                        Date date = new Date(status.getPublishAt().getValue());
                        formatted = sdf.format(date);
                        Main.out("\tDate: " + formatted);
                        queue.add(date);
                        queueVideos.add(vlr.getItems().get(0));
                    } else {
                        Main.out("[PUBLIC][SCHEDULED] " + snippet.getTitle());
                        //Break the loop because once we get to the public videos that don't have a date,
                        //we're probably done. (only doing this to save API calls and preserve
                        //the quota so we can upload more videos per day.
                        break;
                    }
                } else {
                    Main.out("[PUBLIC][NOT SCHEDULED] " + snippet.getTitle());
                }
            }
            Collections.sort(queue);
            if (queue.size() > 0) {
                Date latest = queue.get(queue.size() - 1);
                Main.out("Latest video upload: " + latest);
                Calendar cal = Calendar.getInstance();
                cal.setTime(latest);

                Date prediction = null;

                /*
                U P L O A D   S C H E D U L E
                as of 12/13/19
                ==== == == == === ====
                Sunday    - 10am - 6pm
                Monday    - 10am - 6pm
                Tuesday   - 10am - 6pm
                Wednesday - 10am - 6pm
                Thursday  - 10am - 6pm
                Friday    - 10am - 6pm
                Saturday  - 10am - 6pm
                ==== == == == === ====

                Repeats weekly.
                 */

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

                /*

                OLD, HARDCODED PREDICTION ALGORITHM REPLACED BY CONFIGURABLE ONE ABOVE

                int dow = cal.get(Calendar.DAY_OF_WEEK);
                if (dow == Calendar.SUNDAY || dow == Calendar.FRIDAY || dow == Calendar.SATURDAY) {
                    //If the latest video is scheduled for a weekend, then...
                    if (cal.get(Calendar.HOUR_OF_DAY) <= 14) {
                        //If it was scheduled on or before 2:00 PM, then...
                        //predict the next video should be scheduled
                        //for >=2 hours later (4pm)
                        cal.set(Calendar.HOUR_OF_DAY, 16);
                        prediction = cal.getTime();
                    } else if (cal.get(Calendar.HOUR_OF_DAY) >= 16) {
                        //If the latest video was scheduled for on or after 4:00 PM, then...
                        //predict the next video should be scheduled for
                        //the next day (time is TBD currently)

                        //Add one day to the day of the week and check if that next day is a double-upload day:
                        cal.add(Calendar.DAY_OF_WEEK, 1);
                        dow = cal.get(Calendar.DAY_OF_WEEK);
                        cal.add(Calendar.DAY_OF_WEEK, -1);
                        if (dow == Calendar.SUNDAY || dow == Calendar.FRIDAY || dow == Calendar.SATURDAY) {
                            //If the next day is a weekend day, schedule for 2pm
                            cal.add(Calendar.DAY_OF_YEAR, 1);
                            cal.set(Calendar.HOUR_OF_DAY, 14);
                            prediction = cal.getTime();
                        } else {
                            //If not, schedule for 4pm
                            cal.add(Calendar.DAY_OF_YEAR, 1);
                            cal.set(Calendar.HOUR_OF_DAY, 16);
                            prediction = cal.getTime();
                        }
                    }
                } else {
                    cal.add(Calendar.DAY_OF_WEEK, 1);
                    dow = cal.get(Calendar.DAY_OF_WEEK);
                    cal.add(Calendar.DAY_OF_WEEK, -1);
                    if (dow == Calendar.SUNDAY || dow == Calendar.FRIDAY || dow == Calendar.SATURDAY) {
                        //If the next day is a weekend day, schedule for 2pm
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 14);
                        prediction = cal.getTime();
                    } else {
                        //If not, schedule for 4pm
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 16);
                        prediction = cal.getTime();
                    }
                }
                */
                /*
                Dialog is disabled because it sometimes doesn't show up
                (and the prediction is mostly reliable).
                 */
                if (p == null) UploadVideo.onDateTimeGathered(null);
                else UploadVideo.onDateTimeGathered(new DateTime(p));
                /*
                StringBuilder queueString = new StringBuilder("<table><thead><tr><th>Position</th><th>Video Title</th>" +
                        "<th>Privacy Status</th><th>Publish Date</th></tr></thead><tbody>");
                int i = 0;
                for(Video v : queueVideos) {
                    queueString
                            .append("<tr><td>")
                            .append("#").append(i)
                            .append("</td><td>")
                            .append(v.getSnippet().getTitle())
                            .append("</td><td>")
                            .append(v.getStatus().getPrivacyStatus())
                            .append("</td><td>")
                            .append(sdf.format(queue.get(i)))
                            .append("</td></tr>");
                    i ++;
                    if(i >= 25) {
                        i = queueVideos.size() - 1;
                        v = queueVideos.get(i);
                        queueString
                                .append("<tr><td>...</td><td>...</td><td>...</td></tr><tr><td>")
                                .append("#").append(i)
                                .append("</td><td>")
                                .append(v.getSnippet().getTitle())
                                .append("</td><td>")
                                .append(v.getStatus().getPrivacyStatus())
                                .append("</td><td>")
                                .append(sdf.format(queue.get(i)))
                                .append("</td></tr>");
                    }
                }

                if (prediction != null) {
                    String predictedTime = sdf.format(prediction);

                    int r = JOptionPane.showOptionDialog(null,
                            "<html><p>Based on your upload schedule, this video should be scheduled for <strong>" +
                                    predictedTime + "</strong><br /><br />All videos in queue " +
                                    "to be public:<br /></p>" + queueString + "</html>",
                            "Select an option", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                            null, new String[]{"Schedule for " + predictedTime, "Don't schedule", "Cancel upload"},
                            "Schedule for " + predictedTime);
                    //If 'r' is one of our options, then...
                    if (0 <= r && r <= 2) {
                        if (r == 0) {
                            //Schedule for recommended time
                            UploadVideo.onDateTimeGathered(new DateTime(prediction));
                            return;
                        } else if(r == 1) {
                            //Don't schedule
                            UploadVideo.onDateTimeGathered(null);
                            return;
                        } else { // (r == 2) is always true here
                            //Cancel upload
                            Main.exit(2);
                            return;
                        }
                    } else {
                        //If not, then cancel the program.
                        Main.exit(2);
                        return;
                    }
                } else {
                    //We couldn't predict a time. Leave it up to the user to pick a time.
                    main();
                    JOptionPane.showMessageDialog(null, "Unable to predict an upload time. Please enter a time for the video to be scheduled.");
                }
                */
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
        }/* catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }*/

        doneBtn.addActionListener(event -> {
            try {
                onDoneBtnPressed();
            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof GoogleJsonResponseException) {
                    Main.err("Google JSON Response Error: " + e.getMessage() + "\nDetails: " + ((GoogleJsonResponseException) e).getDetails());
                }
                Main.err("Error with video scheduler: " + e.getMessage());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Main.err("Error with video scheduler: " + e.getMessage());
            }
        });
    }

    public static void main(String[] args) throws FileNotFoundException, IllegalAccessException, NoSuchFieldException {
        new UploadScheduler();
    }

    private void onDoneBtnPressed() throws IOException, GeneralSecurityException {
        LocalDate date = datePicker.getDate();
        LocalTime time = timePicker.getTime();
        LocalDateTime ldt = LocalDateTime.of(date, time);
        DateTime dt = new DateTime(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        Main.out("DateTime: " + dt.toStringRfc3339());
        f.dispose();
        UploadVideo.onDateTimeGathered(dt);
    }

    private void main() {
        f = new JFrame("Schedule Upload");
        f.setSize(200, 150);
        setModal(true);
        f.setContentPane(this.panel);
        f.setAlwaysOnTop(true);
        f.setVisible(true);
    }
}
