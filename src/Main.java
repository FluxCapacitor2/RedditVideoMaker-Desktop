import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RedditVideoMaker's main class. Running the main method starts a swing GUI that pulls all of the strings
 * needed to make a full Reddit video, complete with music, an outro, all of the comments, etc.
 *
 * @author FluxCapacitor
 * @version 1.1.0
 */
public class Main {
    /**
     * These fields are configurable with command-line arguments.
     */
    static String LIBRARY_FOLDER;
    static String DOWNLOADS_FOLDER;
    static JFrame editFrame;
    /**
     * The GUI class for the swing window.
     * This is used to create the swing window (JFrame),
     * which displays the current action, progress,
     * time remaining, etc.
     *
     * @see Rendering The host class for the main GUI of this app.
     */
    static Rendering gui = null;
    private static String OUTPUT_FOLDER;
    static JFrame configFrame;
    /**
     * The JFrame object that represents the window
     * created by the program. It is a variable
     * here because it is modified later.
     *
     * @see Rendering
     * @see Options
     */
    static JFrame guiFrame;
    /**
     * The timer used by `calculateRemainingTime`.
     *
     * @see Main#calculateRemainingTime(long, long, long)
     */
    private static Timer timer;
    /**
     * A StringBuilder for the log of the program.
     * Used for saving the log to a file after the
     * program finishes.
     *
     * @see Main#out(String) Log to System.out, the StringBuilder for the log, and the log textArea.
     * @see Main#err(String) Log an error to System.out, the StringBuilder for the log, and the log textArea.
     * @see Main#exit(int) Saves the log and quits the program.
     */
    private static StringBuilder log = new StringBuilder();

    /**
     * Start the program. This is the main method.
     * <p>
     * All errors are caused by setting the look-and-feel of the swing application, but they
     * are highly unlikely to be caused because it works cross-platform.
     *
     * @param args Command-line arguments specified by Java.
     * @throws ClassNotFoundException          If no system look-and-feel class was found or the class name was invalid.
     * @throws UnsupportedLookAndFeelException If the look-and-feel is unsupported by the OS/vendor.
     * @throws InstantiationException          If a new instance of the look-and-feel class couldn't be created.
     * @throws IllegalAccessException          If the look-and-feel class/initializer is not accessible.
     */
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Config.setListeners(new ConfigEvents() {
            @Override
            void onBackgroundChanged(String newValue) {

            }

            @Override
            void onAlwaysOnTopChanged(boolean newValue) {
                if (guiFrame != null) guiFrame.setAlwaysOnTop(newValue);
            }

            @Override
            void onDownloadsFolderChanged(String newValue) {
                DOWNLOADS_FOLDER = newValue;
            }

            @Override
            void onLibraryFolderChanged(String newValue) {
                LIBRARY_FOLDER = newValue;
            }

            @Override
            void onOutputFolderChanged(String newValue) {
                OUTPUT_FOLDER = newValue;
            }
        });

        /*
        //Load folder structure from command-line arguments.
        if (args.length == 3) {
            LIBRARY_FOLDER = args[0];
            out("Setting library folder (from command args) to " + LIBRARY_FOLDER);
            DOWNLOADS_FOLDER = args[1];
            out("Setting downloads folder (from command args) to " + DOWNLOADS_FOLDER);
            OUTPUT_FOLDER = args[2];
            out("Setting output folder (from command args) to " + OUTPUT_FOLDER);
        } else if (args.length == 0) {
            out("[WARNING] Using default folder structure. Errors may be raised if the configuration is not correct.");
        } else {
            err("Invalid arguments. Must use syntax:\n\tjava -jar rvm.jar \"path/to/library/folder\" \"path/to/downloads/folder\" \"path/to/output/folder\"");
            exit(1);
        }
         */

        //Start the rendering GUI to show the progress
        guiFrame = new JFrame();
        guiFrame.setTitle("RedditVideoMaker");
        gui = new Rendering();
        guiFrame.setContentPane(gui.panel);
        guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        guiFrame.setSize(960, 540);
        guiFrame.setLocationRelativeTo(null);
        guiFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        guiFrame.setAlwaysOnTop(Config.getAlwaysOnTop());
        guiFrame.setVisible(false);

        configFrame = new JFrame();
        configFrame.setTitle("Settings");
        configFrame.setContentPane(gui.options.frame);
        configFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        configFrame.setSize(500, 250);
        configFrame.setLocationRelativeTo(null);
        configFrame.setExtendedState(JFrame.NORMAL);
        configFrame.setAlwaysOnTop(false);
        configFrame.setVisible(true);

        openOptionsMenu();
    }

    /**
     * Patterns to recognize useful filenames.
     */
    //private static Pattern pngPattern = Pattern.compile("(rvm_dl_[a-z0-9]{4}_thing_t[13]_[a-z0-9]{7}(_[0-9]{1,2})?.png)");
    private static Pattern manifestPattern = Pattern.compile("(rvm_manifest_[a-z0-9]{4}.json)");

    /**
     * Open the options menu.
     * This is achieved by setting the already-existing
     * "guiFrame"'s visibility to false and the
     * already-existing "configFrame"'s visibility
     * to true (effectively showing the window).
     *
     * @see Options
     */
    static void openOptionsMenu() {
        guiFrame.setVisible(false);
        configFrame.setVisible(true);
    }

    private static String getDescriptionBlurb(String sr, String[] titles) {
        String title;
        if (titles.length == 1) {
            //Singular
            title = titles[0];
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : titles) {
                sb.append("➡️ ").append(s).append("\n");
            }
            title = sb.toString();
        }

        Map<String, String> descs = new HashMap<>();
        int n = titles.length;
        descs.put("r/AskReddit", "Welcome to r/AskReddit, where redditors answer the " + pluralize(n, "question", "questions"));
        descs.put("r/ProRevenge", "Welcome to r/ProRevenge, where redditors share their stories of going out" +
                " of their way to get revenge. Today's " + pluralize(n, "story", "stories"));
        descs.put("r/pettyrevenge", "Welcome to r/PettyRevenge, where redditors talk about their experiences " +
                "with going out of their way to get minor revenge. Today's " + pluralize(n, "story", "stories"));
        descs.put("r/MaliciousCompliance", "Welcome to r/MaliciousCompliance, where redditors talk about the times " +
                "they complied with someone's orders for the worse. Today's " + pluralize(n, "story", "stories"));
        descs.put("r/NuclearRevenge", "Welcome to r/NuclearRevenge, where redditors share their revenge stories, which " +
                "are extreme and sometimes legally questionable. Today's " + pluralize(n, "story", "stories"));
        descs.put("r/tifu", "Welcome to r/TIFU (Today I F*cked Up), where redditors share their stories of daily life " +
                "going horribly wrong. Today's " + pluralize(n, "story", "stories"));
        descs.put("r/talesfromtechsupport", "Welcome to r/TalesFromTechSupport, where redditors share their " +
                "stories from working in IT or other similar technology jobs. Today's " + pluralize(n, "story", "stories"));
        descs.put("r/TalesFromRetail", "Welcome to r/TalesFromRetail, where redditors share their " +
                "stories from working in retail or other similar service jobs. Today's " + pluralize(n, "story", "stories"));
        return descs.get(sr) + ":" + pluralize(n, " ", "\n") + title;
    }

    private static String pluralize(int n, String singular, String plural) {
        if (n == 1) return singular;
        else return plural;
    }

    /**
     * Run a command and return the last line outputted by it's stdOut stream.
     * The environment for this command will be the Downloads folder specified
     * by the configuration.
     *
     * @param com The command to execute.
     * @return The last line outputted after the command is executed and terminates.
     * @throws IOException          If there was an IO error with the command, i.e. the file could not be found.
     * @throws InterruptedException If the process's thread is interrupted.
     */
    private static String getOutputFromCommand(String com) throws IOException, InterruptedException {
        out("> " + com);
        Process proc = Runtime.getRuntime().exec(com, null, new File(DOWNLOADS_FOLDER));
        String line;
        String output = null;
        BufferedReader bf = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        while ((line = bf.readLine()) != null) {
            out(line);
            output = line;
        }
        int exitCode = proc.waitFor();
        out("Process ended with exit code " + exitCode);
        return output;
    }

    @NotNull
    static ArrayList<File> randomizeFilesInFolder(String folder) {
        File dir = new File(folder);
        ArrayList<File> files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(dir.listFiles())));
        for (File f : files) {
            if (f.getName().contains(".ini")) {
                //Remove 'desktop.ini' from the returned list
                files.remove(f);
                break;
            }
        }
        Collections.shuffle(files);
        return files;
    }

    static boolean requestUserYesOrNo(String label) {
        return JOptionPane.showConfirmDialog(getGUI(), label) == JOptionPane.YES_OPTION;
    }

    @NotNull
    static String requestUserInput(String label) {
        return JOptionPane.showInputDialog(getGUI(), label);
    }

    /**
     * Get the JPanel part of the GUI.
     *
     * @return The JPanel (implicitly casted to Component)
     */
    @Nullable
    static Component getGUI() {
        if (gui != null) {
            return gui.panel;
        }
        return null;
    }

    /**
     * Run a console command and record the output
     * from it in the log.
     *
     * @param com The command to run.
     * @throws IOException          If there was an IO error running the process.
     * @throws InterruptedException If the process thread is interrupted.
     */
    private static void exec(String com) throws IOException, InterruptedException {
        out("> " + com);
        Process process = Runtime.getRuntime().exec(com, null, new File(DOWNLOADS_FOLDER));
        String line;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = stdIn.readLine()) != null) {
            out("[runCommand: out] " + line);
        }
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = stdError.readLine()) != null) {
            err("[runCommand: error] " + line);
        }
        int exitCode = process.waitFor();
        out("Process finished with exit code " + exitCode);
    }

    /**
     * Move a file by renaming it to a different file name/path.
     *
     * @param original The path of the original file.
     * @param dest     The path of the new file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void moveFile(String original, String dest) {
        out("Moving file " + original + " to " + dest);
        new File(dest).getParentFile().mkdirs();
        new File(original).renameTo(new File(dest));
    }

    /**
     * After the options menu is closed, the main
     * program is started. The "guiFrame" is started, which shows
     * progress and requests user input. This is where all
     * encoding takes place. This method requires the configuration
     * to be correct and be loaded properly.
     *
     * @see Config
     * @see Options
     */
    static void onAfterOptionsMenuClosed() {
        configFrame.dispose();
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                guiFrame.setVisible(true);
                guiFrame.invalidate();

                DOWNLOADS_FOLDER = Config.getDownloadsFolder();
                LIBRARY_FOLDER = Config.getLibraryFolder();
                OUTPUT_FOLDER = Config.getOutputFolder();

                guiFrame.setSize(960, 540);
                guiFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                int screenshots = 0;

                try {
                    String DLid = getDLid();
                    VideoManifest vm = getManifest(DLid);

                    String background = Config.getBackground();
                    if (background.equals("null")) {
                        background = Config.getRandomBackground();
                    }

                    ArrayList<String> outputFiles = new ArrayList<>();

                    double length = 0.0d;
                    gui.progressBar.setMaximum(vm.comments.length);
                    gui.progressBar.setIndeterminate(false);
                    gui.title.setText("Starting up");
                    gui.progressLabel.setText("Starting...");

                    //Generate TTS audio using Balabolka's command-line API + our SAPI5 voice (Daniel English-UK)
                    //Also find the approximate length of the video given all of the audio clips.
                    int i = 0;
                    long startTime1 = System.currentTimeMillis();
                    for (VideoManifestComment c : vm.comments) {
                        i++;
                        gui.progressBar.setValue(i);
                        gui.progressLabel.setText(i + " / " + vm.comments.length);
                        String outputFile = DOWNLOADS_FOLDER + "/rvm_dl_" + c.DLid + "_thing_" + c.thingId + ".mp3";
                        if (!new File(outputFile).exists()) {
                            String escapedText = c.text.replace("\"", ", ").replace("\n", ", "); //Replace quotes with commas for a similar effect w/o screwing up the program
                            exec(LIBRARY_FOLDER + "/bin/balcon/balcon.exe -n \"ScanSoft Daniel_Full_22kHz\" -t \"" + escapedText + "\" -w \"" + outputFile + "\" -sb 100 -fr 48 -bt 16 -ch 2 -v 60");
                        } else {
                            out(outputFile + " already exists! Skipping...");
                        }
                        if (Config.getCalcLength()) {
                            length += Double.parseDouble(getOutputFromCommand("\"" + LIBRARY_FOLDER + "/bin/ffprobe.exe\" -v error -select_streams a:0 -show_entries stream=duration -of default=noprint_wrappers=1:nokey=1 \"" + DOWNLOADS_FOLDER + "/rvm_dl_" + c.DLid + "_thing_" + c.thingId + ".mp3\""));
                        }
                        calculateRemainingTime(startTime1, vm.comments.length, i);
                    }
                    gui.timeRemaining.setText("Waiting for confirmation");

                    //Request confirmation to start rendering the videos/audio track/thumbnail.
                    boolean result = requestUserYesOrNo("Proceed?\n\nVideo Length: " +
                            (Config.getCalcLength() ? convertToTimeString((int) (length + 20)) : "(Not enabled)") +
                            "\nBackground: " + background + "\nComments: " + (vm.comments.length - 1)
                            + "\nTitle(s): " + Arrays.toString(vm.titles) + "\nSubreddit(s): " + Arrays.toString(vm.subreddits));
                    if (!result) {
                        exit(0);
                    }

                    gui.timeRemaining.setText("");

                    //Make sure all of the files specified in the manifest exist so that we don't have any mysterious problems later.
                    for (VideoManifestComment comment : vm.comments) {
                        if (!new File(DOWNLOADS_FOLDER + "/" + comment.name).exists()) {
                            err("Required file " + comment.name + " does not exist.");
                            if (requestUserYesOrNo("You are missing a required file from the download: \"" +
                                    comment.name + "\"\n\nClick Yes to go to the editor, or" +
                                    " No/Cancel to stop the program.")) {
                                guiFrame.dispose();
                                new EditManifest(vm.comments);
                                return null;
                            }
                            exit(1);
                        } else {
                            out("Required file " + comment.name + " exists.");
                        }
                    }

                    //Start by asking the user to pick a post title to be shown in the video.
                    out("Selecting a video title to use...");
                    int response;
                    if (vm.titles.length == 1) {
                        response = 0;
                    } else {
                        response = JOptionPane.showOptionDialog(null,
                                "Please pick a title to be the most prominently shown in the video's metadata: ",
                                "Select an option", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                                null, vm.titles, vm.titles[0]);
                    }

                    out("Selected option: " + response + " (" + vm.titles[response] + ")");

                    String title = vm.titles[response];
                    String subreddit = vm.subreddits[response];
                    String URL = vm.URLs[response];

                    gui.progressBar.setIndeterminate(true);
                    gui.title.setText("Generating thumbnail");
                    gui.progressLabel.setText("1 / 1");
                    //Make the thumbnail for the video (which is also used in the first comment temp video)
                    ArrayList<String> thumbnails = new ArrayList<>();
                    for (VideoManifestComment comment : vm.comments) {
                        if (comment.isTitle) {
                            thumbnails.add(ThumbnailGenerator.generateThumbnail(comment.DLid, comment.text, comment.subreddit));
                        }
                    }

                    //Generate music track
                    out("Generating music track...");
                    gui.progressBar.setIndeterminate(true);
                    gui.title.setText("Generating audio track");
                    gui.progressLabel.setText("1 / 1");
                    ArrayList<File> audioFiles = randomizeFilesInFolder(LIBRARY_FOLDER + "/audio");
                    ArrayList<String> audioFilesStrings = new ArrayList<>();
                    StringBuilder audioConcatData = new StringBuilder();
                    for (i = 0; i < audioFiles.size(); i++) {
                        audioConcatData.append("[").append(i).append(":a]");
                        audioFilesStrings.add(audioFiles.get(i).getAbsolutePath());
                    }
                    StringBuilder inputAudioFiles = new StringBuilder();
                    for (String f : audioFilesStrings) {
                        inputAudioFiles.append("-hwaccel auto -i \"").append(f).append("\" ");
                    }
                    exec("cmd /c start \"(1/1) Generating Audio Track\" /min /wait \"" + LIBRARY_FOLDER + "/bin/ffmpeg\" " + inputAudioFiles.toString() + " -filter_complex \"" + audioConcatData + "concat=n=" + audioFiles.size() + ":v=0:a=1[outa]\" -map \"[outa]\" -n rvm_audio_" + DLid + ".mp3");

                    i = 1;
                    gui.title.setText("Rendering temporary videos");
                    gui.progressBar.setMaximum(vm.comments.length);
                    gui.progressBar.setIndeterminate(false);
                    gui.progressLabel.setText("Starting...");
                    long startTime = System.currentTimeMillis();
                    gui.renderPreview.setVisible(true);
                    for (VideoManifestComment comment : vm.comments) {
                        String out = "rvm_temp_" + comment.DLid + "_thing_" + comment.thingId + ".mp4";
                        File outFile = new File(DOWNLOADS_FOLDER + "/" + out);
                        out("Checking if " + outFile.getAbsolutePath() + " exists... " + outFile.exists());
                        outputFiles.add(out);
                        if (!outFile.exists()) {
                            long cmdStartTime = new Date().getTime();
                            gui.progressLabel.setText(i + " / " + vm.comments.length);
                            gui.progressBar.setValue(i);
                            //For each comment, join the audio & image together with a blurred video background.
                            String mp3 = "rvm_dl_" + comment.DLid + "_thing_" + comment.thingId + ".mp3";
                            String png = "rvm_dl_" + comment.DLid + "_thing_" + comment.thingId + ".png";

                            ImageIcon icon = new ImageIcon(png);
                            gui.renderPreview.setIcon(getScaledImage(icon, (icon.getIconWidth() / icon.getIconHeight()) * 400, 400));

                            if (comment.isTitle) {
                                png = Main.DOWNLOADS_FOLDER + "/rvm_final_" + comment.DLid + "_thumbnail.png";
                            }
                            String com = "\"" + LIBRARY_FOLDER + "/bin/ffprobe.exe\" -v error -select_streams a:0 -show_entries stream=duration -of default=noprint_wrappers=1:nokey=1 -sexagesimal \"" + DOWNLOADS_FOLDER + "/rvm_dl_" + comment.DLid + "_thing_" + comment.thingId + ".mp3\"";
                            String videoLength = getOutputFromCommand(com);
                            //Cut everything to the length of the audio because `-shortest` is very buggy in this scenario.
                            String cmd = "cmd /c start \"(" + i + "/" + vm.comments.length + ") Generating Temporary Videos\" /min /wait \"" + LIBRARY_FOLDER + "/bin/ffmpeg\" -t " + videoLength + " -i \"" + background + "\" -hwaccel auto -t " + videoLength + " -i \"" + mp3 + "\" -r 1 -hwaccel auto -i \"" + png + "\" -filter_complex \"[0:v]boxblur=10:5 [bblur], [2:v]scale=1920:-1 [" + (comment.isTitle ? "ovrl1" : "ovrl") + "]," + (comment.isTitle ? "[ovrl1]colorkey=0x292929:0.05:0.1[ovrl]," : "") + "[bblur][ovrl]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2\" -acodec copy -map 1:a -c:a aac -shortest -r 30 -n " + out;
                            exec(cmd);
                            long cmdEndTime = new Date().getTime();
                            long cmdTime = (cmdEndTime - cmdStartTime);
                            out("Process took " + cmdTime + "ms.");
                            if (cmdTime <= 500) {
                                err("Abnormally short generation (probably an error) detected!");
                                if (!requestUserYesOrNo("An error was detected while running the command:\n\n" + cmd + "\n\nContinue? (If you select Yes, there may be more problems later)")) {
                                    exit(1);
                                }
                            }
                        } else {
                            out("File " + out + " already exists! Skipping...");
                            i++;
                            continue;
                        }
                        i++;
                        out("Calculating remaining time...");
                        if (!(i >= vm.comments.length)) calculateRemainingTime(startTime, vm.comments.length, i);
                        out("Finished method call.");
                    }
                    gui.renderPreview.setVisible(false);
                    gui.timeRemaining.setText("");

                    gui.title.setText("Combining final video");
                    gui.progressBar.setIndeterminate(true);
                    gui.progressLabel.setText("Gathering data...");
                    //CONCAT ALL OF THE TEMP VIDEOS INTO ONE
                    StringBuilder concatData = new StringBuilder();
                    for (i = 0; i < outputFiles.size(); i++) {
                        if (outputFiles.get(i).endsWith(".mp4")) {
                            if (outputFiles.get(i).contains("outros")) {
                                //Outro sequences have no audio attached to them and may not have an audio stream.
                                concatData.append("[").append(i).append(":0]");
                            } else {
                                //Non-outro .mp4 files have audio AND video streams.
                                concatData.append("[").append(i).append(":0][").append(i).append(":1]");
                            }
                        } else if (outputFiles.get(i).endsWith(".mp3")) {
                            //.mp3 files only have audio streams.
                            concatData.append("[").append(i).append(":0]");
                        } else {
                            //There was a format we didn't account for.
                            err("There was a problem with the final concatenation: Unknown file extension for file " + outputFiles.get(i));
                            exit(1);
                        }
                    }

                    gui.progressBar.setIndeterminate(true);
                    gui.progressBar.setMaximum(3);
                    gui.progressBar.setValue(1);
                    gui.progressLabel.setText("1 / 3");

                    String audioFile = "rvm_audio_" + DLid + ".mp3";
                    //After all if the individual videos are made, concatenate them into one.
                    exec("cmd /c start \"(1/3) Combining Final Video\" /min /wait \"" + LIBRARY_FOLDER + "/bin/ffmpeg\" -i " + String.join(" -i ", outputFiles) + " -i " + audioFile + " -filter_complex \"" + concatData + "concat=n=" + outputFiles.size() + ":v=1:a=1[outv][outa];[outa]aformat=sample_fmts=fltp:sample_rates=22050:channel_layouts=stereo[a1];[" + outputFiles.size() + ":0]aformat=sample_fmts=fltp:sample_rates=22050:channel_layouts=stereo[a2];[a2]volume=-20.0dB[a2adj];[a1]volume=5.0dB[a1adj];[a1adj][a2adj]amerge=inputs=2[apresync];[apresync]aresample=async=1000[afinal];[outv]mpdecimate[vfinal]\" -map \"[vfinal]\" -map \"[afinal]\" -vsync 2 -shortest -n -r 30 rvm_final_" + DLid + "_precut.mp4");
                    gui.progressBar.setValue(2);
                    gui.progressLabel.setText("2 / 3");
                    String com = "\"" + LIBRARY_FOLDER + "/bin/ffprobe.exe\" -v error -select_streams v:0 -show_entries stream=duration -of default=noprint_wrappers=1:nokey=1 -sexagesimal rvm_final_" + DLid + "_precut.mp4";

                    String finalPath = DOWNLOADS_FOLDER + "/rvm_final_" + DLid + ".mp4";

                    //Cut the video to the length of the longest video stream (workaround for an ffmpeg but where `-shortest` doesn't work on audio)
                    exec("cmd /c start \"(2/3) Combining Final Video\" /min /wait \"" + LIBRARY_FOLDER + "/bin/ffmpeg\" -hwaccel auto -i rvm_final_" + DLid + "_precut.mp4 -to " + getOutputFromCommand(com) + " -c copy -n rvm_final_" + DLid + "_preoutro.mp4");
                    String outro = randomizeFilesInFolder(LIBRARY_FOLDER + "/outros").get(0).getAbsolutePath();
                    gui.progressLabel.setText("3 / 3");
                    gui.progressBar.setValue(3);
                    //Add the outro to the fixed video.
                    exec("cmd /c start \"(3/3) Combining Final Video\" /min /wait \"" + LIBRARY_FOLDER + "/bin/ffmpeg\" -hwaccel auto -i rvm_final_" + DLid + "_preoutro.mp4 -i " + outro + " -filter_complex \"[0:0][0:1][1:0][1:1]concat=n=2:v=1:a=1[preVideo][preAudio];[preAudio]loudnorm[audio1];[audio1]volume=10.0dB[audio2]\" -map \"[preVideo]\" -map \"[audio2]\" -n \"" + finalPath + "\"");

                    //Upload the video to YouTube
                    gui.progressBar.setIndeterminate(true);
                    gui.title.setText("Starting upload to YouTube");
                    gui.timeRemaining.setText("Waiting for permission");
                    List<String> tags = new ArrayList<>(Arrays.asList(
                            "Exquisite Reddit", "AMA", "Q&A", vm.subreddits[0]
                    ));
                    int currentLength = 0;
                    for (String tag : tags) {
                        currentLength += tag.length();
                    }
                    for (String t : vm.titles) {
                        for (String s : t.split(" ")) {
                            //For every word in every title, if the character limit permits, add a tag with the contents of this word.
                            currentLength += s.length();
                            if (currentLength > 500) {
                                //We've reached the character limit, just break the loop and continue.
                                break;
                            }
                            //If this is still running than we haven't reached the limit yet, add the tag to the list.
                            tags.add(s);
                        }
                    }
                    //tags.addAll(Arrays.asList(title.split(" ")));
                    out("Setting video tags: " + tags.toString());

                    String sr = subreddit.substring(1, subreddit.length() - 1);
                    String videoTitle = sr + " | " + title;
                    StringBuilder description = new StringBuilder(getDescriptionBlurb(sr, vm.titles) + ((vm.titles.length == 1) ? "\n\nOriginal Post: " + URL : "\n\nOriginal Posts:\n"));
                    if (vm.URLs.length != 1) {
                        for (String url : vm.URLs) {
                            description.append("➡️ ").append(url).append("\n");
                        }
                    }
                    description.append("\n\n#ExquisiteReddit | #").append(sr.substring(2));

                    gui.timeRemaining.setText("Updating title");
                    while (videoTitle.length() > 100) {
                        videoTitle = requestUserInput("The video title is over 100 characters. Please reformat the title.\nOriginal Title: " + videoTitle);
                    }

                    try {
                        gui.title.setText("Uploading to YouTube");
                        gui.timeRemaining.setText("");
                        gui.progressBar.setIndeterminate(false);
                        gui.progressBar.setMaximum(100);
                        gui.progressBar.setValue(0);
                        String videoId = UploadVideo.main(finalPath, videoTitle, description.toString(), tags);
                        gui.title.setText("Applying thumbnail");
                        gui.progressBar.setValue(2);
                        gui.progressLabel.setText("2 / 2");
                        SetThumbnail.main(videoId, thumbnails.get(response));

                        out("Upload finished!");

                        boolean openInYTStudio = requestUserYesOrNo("The upload has finished. Would you like to open your " +
                                "video in YouTube Studio?");

                        if (openInYTStudio) {
                            new Thread(() -> {
                                try {
                                    String url = "https://studio.youtube.com/video/" + videoId + "/edit";
                                    if (System.getProperty("user.home").equals("C:\\Users\\tntaw"))
                                        exec("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe --profile-directory=\"Profile 2\" " + url);
                                    else Desktop.getDesktop().browse(new URI(url));
                                } catch (IOException | InterruptedException | URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof GoogleJsonResponseException) {
                            err("Google JSON Response Error: " + e.getMessage() + "\nDetails: " + ((GoogleJsonResponseException) e).getDetails());

                            out("PATH:\n\n" + finalPath + "\n\nTITLE: \n\n" + videoTitle +
                                    "\n\nDESCRIPTION:\n\n" + description + "\n\nTAGS:\n\n" + tags);
                        }
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }

                    if (requestUserYesOrNo("Move files to archive folder? (Recommended)")) {
                        //Move all of the temp files to a folder for archival purposes.
                        gui.title.setText("Cleaning up");
                        gui.progressLabel.setText("Starting...");
                        gui.progressBar.setIndeterminate(false);
                        gui.progressBar.setMaximum(vm.comments.length);
                        i = 0;
                        for (VideoManifestComment vmc : vm.comments) {
                            i++;
                            gui.progressBar.setValue(i);
                            gui.progressLabel.setText(i + " / " + vm.comments.length);
                            moveFile(DOWNLOADS_FOLDER + "/" + vmc.name, OUTPUT_FOLDER + "/Archive/" + vmc.DLid + "/" + vmc.name);
                            moveFile(DOWNLOADS_FOLDER + "/rvm_temp_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp4", OUTPUT_FOLDER + "/Archive/" + vmc.DLid + "/rvm_temp_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp4");
                            moveFile(DOWNLOADS_FOLDER + "/rvm_dl_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp3", OUTPUT_FOLDER + "/Archive/" + vmc.DLid + "/rvm_dl_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp3");
                            if (vmc.isTitle) {
                                moveFile(DOWNLOADS_FOLDER + "/rvm_final_" + DLid + "_thumbnail.png", OUTPUT_FOLDER + "/Archive/" + DLid + "/rvm_final_" + vmc.DLid + "_thumbnail.png");
                            }
                        }
                        gui.progressBar.setIndeterminate(true);
                        //Move manifest, final pre-cut video, and final post-cut video
                        moveFile(DOWNLOADS_FOLDER + "/rvm_manifest_" + DLid + ".json", OUTPUT_FOLDER + "/Archive/" + DLid + "/rvm_manifest_" + DLid + ".json");
                        moveFile(DOWNLOADS_FOLDER + "/rvm_final_" + DLid + "_precut.mp4", OUTPUT_FOLDER + "/Archive/" + DLid + "/rvm_final_" + DLid + "_precut.mp4");
                        moveFile(DOWNLOADS_FOLDER + "/rvm_final_" + DLid + "_preoutro.mp4", OUTPUT_FOLDER + "/Archive/" + DLid + "/rvm_final_" + DLid + "_preoutro.mp4");
                        moveFile(DOWNLOADS_FOLDER + "/rvm_audio_" + DLid + ".mp3", OUTPUT_FOLDER + "/Archive/" + DLid + "/rvm_audio_" + DLid + ".mp3");
                        moveFile(DOWNLOADS_FOLDER + "/rvm_final_" + DLid + ".mp4", OUTPUT_FOLDER + "/Final/" + DLid + ".mp4");
                    } else {
                        out("Moving files skipped. Everything is still in the Downloads folder, which may screw up this program in the future.");
                    }

                    gui.progressBar.setIndeterminate(false);
                    gui.progressBar.setMaximum(1);
                    gui.progressBar.setValue(1);
                    gui.progressLabel.setText("");
                    gui.title.setText("Done");

                    // yay~yay~yay
                    // WE'RE DONE!
                    // yay~yay~yay

                    out("Everything's done!");

                } catch (IOException | InterruptedException | NullPointerException e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    System.err.println("An error has occurred: " + e.getClass().getCanonicalName());
                    err("An error has occurred: " + e.getClass().getCanonicalName());
                    err(sw.toString());
                } finally {
                    System.err.println("Main GUI finished executing.");
                }
                return null;
            }
        };

        worker.execute();
    }

    private static String convertToTimeString(int v) {
        if (v <= 0) return "0m 0s";
        int tempSeconds = v;
        int minutes = 0;
        while (tempSeconds > 60) {
            minutes++;
            tempSeconds -= 60;
        }
        return minutes + "m " + tempSeconds + "s";
    }

    private static Icon getScaledImage(ImageIcon icon, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(icon.getImage(), 0, 0, w, h, null);
        g2.dispose();

        return new ImageIcon(resizedImg);
    }

    /**
     * Formats seconds into seconds + minutes and sets the
     * time remaining label to the result.
     *
     * @param seconds The amount of seconds remaining.
     */
    private static void setRemainingTimeText(int seconds) {
        SwingUtilities.invokeLater(() -> gui.timeRemaining.setText(convertToTimeString(seconds)));
    }

    /**
     * Log output to System.out and the text area in the GUI.
     *
     * @param str The String to output
     */
    static void out(String str) {
        System.out.println(str);
        if (getGUI() != null) gui.logArea.append("[out] " + str + "\n");
        log.append("[out] ").append(str).append("\n");
    }

    /**
     * Log output to System.err and the text area in the GUI.
     *
     * @param str The String to output
     */
    static void err(String str) {
        System.err.println(str);
        if (getGUI() != null) gui.logArea.append("[err] " + str + "\n");
        log.append("[err] ").append(str).append("\n");
    }

    /**
     * Exit using System.exit after saving
     * the current log to a file.
     */
    static void exit(int code) {
        File logFile = new File(System.getenv("LOCALAPPDATA") + "/rvm_logs/log_" + System.currentTimeMillis() + ".txt");
        logFile.getParentFile().mkdirs();
        out("Saving log to " + logFile.getAbsolutePath());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(log.toString());
            writer.close();
        } catch (IOException e) {
            err("Error saving log file: " + e.getMessage());
        }
        System.exit(code);
    }

    /**
     * Estimate the remaining time based on the current time, start time, current index, and total length.
     * Every second, if the method hasn't been called again, estimate the remaining time again so it is more "accurate"
     * for longer tasks (where each index takes a long period of time, for example a video encode.)
     *
     * @param startTime    The time (System.currentTimeMillis()) of when the process started.
     * @param currentIndex The current step of the process.
     * @param length       The total number of steps required for the process to complete.
     *                     When the task completes, `currentIndex` should be equal to `length`.
     * @throws IllegalArgumentException  If the current time < `startTime`.
     * @throws IndexOutOfBoundsException If `currentIndex` > `length`
     */
    static void calculateRemainingTime(long startTime, long length, long currentIndex)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        //Calculate the remaining time
        long elapsedTime = System.currentTimeMillis() - startTime;

        if (currentIndex > length) throw new IndexOutOfBoundsException("currentIndex > length.");
        if (elapsedTime < 0) throw new IllegalArgumentException("startTime occurs after the current time.");

        if (currentIndex == 0) currentIndex = 1;

        long totalTime = (length * (elapsedTime / currentIndex));
        long remainingTime = totalTime - elapsedTime;
        if (remainingTime < 1000 && remainingTime > 100) remainingTime = 1000;
        final int[] seconds = {(int) (remainingTime / 1000)};
        setRemainingTimeText(seconds[0]);
        if (timer != null) {
            timer.cancel();
        }
        //Tick it down by one second every second so it stays accurate
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    seconds[0] -= 1;
                    setRemainingTimeText(seconds[0]);
                } catch (ArithmeticException e) {
                    //Attempted to divide by 0. Just continue so it returns "Estimating..." instead of an error.
                }
            }
        }, 0, 1000);
        SwingUtilities.invokeLater(() -> gui.timeRemaining.setText("Estimating..."));
    }

    static String getDLid() {
        ArrayList<String> ids = new ArrayList<>();
        File downloadsFolder = new File(Config.getDownloadsFolder());
        out("Searching " + downloadsFolder.getAbsolutePath() + " for matching files...");
        File[] files = downloadsFolder.listFiles();
        if (files == null) {
            err("Downloads folder not found! Was it configured correctly?");
            exit(1);
        } else {
            for (File f : files) {
                String name = f.getName().toLowerCase();
                Matcher manifestMatcher = manifestPattern.matcher(name);
                if (manifestMatcher.matches()) {
                    out("Found manifest file: " + name);
                    //Extract download ID
                    ids.add(name.substring(13, 17));
                }
            }
            if (ids.size() == 0) return null;
            if (ids.size() == 1) return ids.get(0);

            int response = JOptionPane.showOptionDialog(null,
                    "Choose a Download ID to use: ",
                    "Select an option", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, ids.toArray(new String[]{}), ids.get(0));
            if (ids.get(response) == null) {
                exit(65);
            }

            return ids.get(response);
        }
        return null;
    }

    static VideoManifest getManifest(String DLid) throws FileNotFoundException {

        if (DLid == null) {
            JOptionPane.showMessageDialog(getGUI(), "Couldn't find a recent download! Download a manifest and screenshots " +
                    "using the RVM Chrome extension on a thread at https://reddit.com/r/AskReddit/");
            //Desktop.getDesktop().browse(new URI("https://reddit.com/r/AskReddit"));
            exit(1);
        }

        //Look at the manifest and find the post title/subreddit
        File manifest = new File(Config.getDownloadsFolder() + "/rvm_manifest_" + DLid + ".json");
        Gson gson = new Gson();
        return gson.fromJson(new BufferedReader(new FileReader(manifest)), VideoManifest.class);
    }
}
