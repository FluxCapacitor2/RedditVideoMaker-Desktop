package main;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private static final String OUTRO_SONG = "New Land by ALBIS";
    /**
     * The GUI class for the swing window.
     * This is used to create the swing window (JFrame),
     * which displays the current action, progress,
     * time remaining, etc.
     *
     * @see Rendering The host class for the main GUI of this app.
     */
    static Rendering gui = null;
    static JFrame configFrame;
    /**
     * The JFrame object that represents the window
     * created by the program. It is a variable
     * here because it is modified later.
     *
     * @see Rendering
     * @see Options
     */
    private static JFrame guiFrame;
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

    private static ArrayList<String> thumbnails;

    private static int response;

    private static VideoManifest vm;

    private static String DLid;

    private static StringBuilder audioCredits;

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
    private static Pattern manifestPattern = Pattern.compile("(rvm_manifest_[a-z0-9]{4,20}.json)");

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
                sb.append("\uD83D\uDCDD ").append(s).append("\n");
            }
            title = sb.toString();
        }

        Map<String, String> descs = new HashMap<>();
        int n = titles.length;
        descs.put("r/AskReddit", "Welcome to r/AskReddit, where redditors answer the "
                + pluralize(n, "question", "questions"));
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
        descs.put("r/entitledparents", "Welcome to r/EntitledParents, where redditors share their stories of dealing " +
                "with entitled parents who think the world caters to them. Today's " + pluralize(n, "story", "stories"));
        descs.put("r/StoriesAboutKevin", "Welcome to r/StoriesAboutKevin, where redditors post stories of \"Kevins\", " +
                "people that are absolutely braindead and probably couldn't survive on their own. Today's " +
                pluralize(n, "story", "stories"));
        return descs.getOrDefault(sr, "Welcome to " + sr + ". Today's " + pluralize(n, "story", "stories")) + ":" + pluralize(n, " ", "\n") + title;
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
        Process proc = Runtime.getRuntime().exec(com, null, new File(Config.getDownloadsFolder()));
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

    private static boolean requestUserYesOrNo(String label) {
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
    static void exec(String com) throws IOException, InterruptedException {
        out("> " + com);
        Process process = Runtime.getRuntime().exec(com, null, new File(Config.getDownloadsFolder()));
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
            protected Object doInBackground() {
                guiFrame.setVisible(true);
                guiFrame.invalidate();

                guiFrame.setSize(960, 540);
                guiFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                try {
                    DLid = getDLid();
                    vm = getManifest(DLid);

                    String background = Config.getBackground();
                    if (background.equals("null")) {
                        background = Config.getRandomBackground();
                    }

                    ArrayList<String> outputFiles = new ArrayList<>();

                    double length = 0.0d;
                    gui.progressBar.setMaximum(vm.comments.length);
                    gui.progressBar.setIndeterminate(false);
                    gui.title.setText("Starting up");
                    gui.progressLabel.setText("0%");

                    //Generate TTS audio using Balabolka's command-line API + our SAPI5 voice (Daniel English-UK)
                    //Also find the approximate length of the video given all of the audio clips.
                    int i = 0;
                    long startTime1 = System.currentTimeMillis();
                    for (VideoManifestComment c : vm.comments) {
                        if (c.text.isEmpty()) {
                            vm.comments[i].text = " ";
                            c = vm.comments[i];
                        }
                        i++;
                        gui.progressBar.setValue(i);
                        gui.progressLabel.setText(i + " / " + vm.comments.length);
                        String outputFile = Config.getDownloadsFolder() + "/rvm_dl_" + c.DLid + "_thing_" + c.thingId + ".mp3";
                        if (!new File(outputFile).exists()) {
                            String escapedText = c.text.replace("\"", ", ").replace("\n", ", "); //Replace quotes with commas for a similar effect w/o screwing up the program
                            exec(Config.getLibraryFolder() + "/bin/balcon/balcon.exe -n \"ScanSoft Daniel_Full_22kHz\" -t \"" + escapedText + "\" -w \"" + outputFile + "\" -sb 100 -fr 48 -bt 16 -ch 2 -v 60");
                        } else {
                            out(outputFile + " already exists! Skipping...");
                        }
                        //if (Config.getCalcLength()) {
                        length += Double.parseDouble(getOutputFromCommand("\"" + Config.getLibraryFolder() + "/bin/ffprobe.exe\" -v error -select_streams a:0 -show_entries stream=duration -of default=noprint_wrappers=1:nokey=1 \"" + Config.getDownloadsFolder() + "/rvm_dl_" + c.DLid + "_thing_" + c.thingId + ".mp3\""));
                        //}
                        calculateRemainingTime(startTime1, vm.comments.length, i);
                    }
                    gui.timeRemaining.setText("Waiting for confirmation");

                    //Request confirmation to start rendering the videos/audio track/thumbnail.
                    boolean result = requestUserYesOrNo("Proceed?\n\nVideo Length: " +
                            convertToTimeString((int) (length + 20)) +
                            "\nBackground: " + background + "\nComments: " + (vm.comments.length - 1)
                            + "\nTitle(s): " + Arrays.toString(vm.titles) + "\nSubreddit(s): " + Arrays.toString(vm.subreddits));
                    if (!result) {
                        exit(0);
                    }

                    gui.timeRemaining.setText("");

                    //Make sure all of the files specified in the manifest exist so that we don't have any mysterious problems later.
                    for (VideoManifestComment comment : vm.comments) {
                        if (!new File(Config.getDownloadsFolder() + "/" + comment.name).exists()) {
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
                    /*
                    out("Selecting a video title to use...");
                    if (vm.titles.length == 1) {
                        response = 0;
                    } else {
                        response = JOptionPane.showOptionDialog(null,
                                "Please pick a title to be the most prominently shown in the video's metadata: ",
                                "Select an option", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                                null, vm.titles, vm.titles[0]);
                    }
                     */
                    //This option is now presented in the manifest.
                    for (i = 0; i < vm.isFeatured.length; i++) {
                        if (vm.isFeatured[i]) {
                            response = i;
                            break;
                        }
                    }

                    out("Selected option: " + response + " (" + vm.titles[response] + ")");

                    String title = vm.titles[response];
                    String subreddit = vm.subreddits[response];
                    String URL = vm.URLs[response];

                    gui.progressBar.setIndeterminate(true);
                    gui.title.setText("Generating thumbnail");
                    gui.progressLabel.setText("1 / 1");
                    //Make the thumbnail for the video (which is also used in the first comment temp video)
                    thumbnails = new ArrayList<>();
                    i = 0;
                    for (VideoManifestComment comment : vm.comments) {
                        if (comment.isTitle) {
                            thumbnails.add(ThumbnailGenerator.generateThumbnail(comment.DLid + "_id_" + i, vm.thumbnailTexts[i], comment.subreddit));
                            i++;
                        }
                    }

                    String sr = subreddit.substring(1, subreddit.length() - 1);
                    String videoTitle = sr + " | " + title;

                    gui.timeRemaining.setText("Updating title");
                    while (videoTitle.length() > 100) {
                        videoTitle = requestUserInput("The video title is over 100 characters. Please reformat the title.\nOriginal Title: " + videoTitle);
                    }

                    //Generate music track
                    out("Generating music track...");
                    gui.progressBar.setIndeterminate(true);
                    gui.title.setText("Generating audio track");
                    gui.progressLabel.setText("1 / 1");
                    ArrayList<File> audioFiles = randomizeFilesInFolder(Config.getLibraryFolder() + "/audio");
                    ArrayList<File> audioFiles2 = new ArrayList<>();
                    audioCredits = new StringBuilder("\uD83C\uDFB6 Music credits:\n");
                    double position = 0;
                    for (File f : audioFiles) {
                        String len = getOutputFromCommand("\"" + Config.getLibraryFolder() + "/bin/ffprobe.exe\" -v error -select_streams a:0 -show_entries stream=duration -of default=noprint_wrappers=1:nokey=1 \"" + f.getAbsolutePath() + "\"");
                        double songLength = Double.parseDouble(len);
                        audioCredits
                                .append("\uD83C\uDFB5 ")
                                .append(convertToTimestamp(position))
                                .append(" ")
                                .append(f.getName(), 0, f.getName().length() - 4)
                                .append("\n");
                        position += songLength;
                        audioFiles2.add(f);
                        if (position >= length) break;
                    }
                    audioCredits
                            .append("\uD83C\uDFB5 ")
                            .append(convertToTimestamp(length))
                            .append(" ")
                            .append(OUTRO_SONG)
                            .append("\n");

                    //exec("cmd /c start \"(1/1) Generating Audio Track\" /min /wait \"" + Config.getLibraryFolder() + "/bin/ffmpeg\" " + inputAudioFiles.toString() + " -filter_complex \"" + audioConcatData + "concat=n=" + audioFiles2.size() + ":v=0:a=1[outa]\" -map \"[outa]\" -y rvm_audio_" + DLid + ".mp3");

                    FFmpeg ffmpeg = createFFmpeg();

                    for (File f : audioFiles2) {
                        ffmpeg.addInput(UrlInput.fromPath(f.toPath()));
                    }
                    double finalPosition = position;
                    Filter audioConcatFilter = Filter.withName("concat")
                            .addArgument("n", String.valueOf(audioFiles2.size()))
                            .addArgument("v", "0")
                            .addArgument("a", "1")
                            .addOutputLink("outa");
                    for (i = 0; i < audioFiles2.size(); i++) {
                        audioConcatFilter.addInputLink(i + ":0");
                    }
                    long startTime2 = System.currentTimeMillis();
                    ffmpeg
                            .setComplexFilter(FilterGraph.of(
                                    FilterChain.of(audioConcatFilter)
                            ))
                            .setProgressListener(p -> {
                                gui.progressBar.setMaximum((int) finalPosition);
                                gui.progressBar.setValue(Math.toIntExact(p.getTimeMillis() / 1000));
                                gui.progressBar.setIndeterminate(false);
                                calculateRemainingTime(startTime2, (long) (finalPosition), p.getTimeMillis() / 1000);
                            })
                            .addOutput(UrlOutput.toPath(new File(Config.getDownloadsFolder() + "/rvm_audio_" + DLid + ".mp3").toPath()))
                            .setOverwriteOutput(true)
                            .setLogLevel(LogLevel.INFO)
                            .addArguments("-map", "[outa]")
                            .setOutputListener(s -> {
                                out("[FFmpeg CLI] > " + s);
                                return false;
                            });

                    gui.individualProgressBar.setVisible(false);
                    gui.progressBar.setIndeterminate(false);
                    ffmpeg.execute();

                    i = 1;
                    gui.title.setText("Rendering temporary videos");
                    gui.progressBar.setMaximum(vm.comments.length);
                    gui.progressBar.setIndeterminate(false);
                    gui.progressLabel.setText("0%");
                    gui.individualProgressBar.setVisible(true);
                    gui.individualProgressBar.setIndeterminate(false);
                    long startTime = System.currentTimeMillis();
                    gui.renderPreview.setVisible(true);
                    for (VideoManifestComment comment : vm.comments) {
                        long individualStartTime = System.currentTimeMillis();
                        String out = "rvm_temp_" + comment.DLid + "_thing_" + comment.thingId + ".mp4";
                        File outFile = new File(Config.getDownloadsFolder() + "/" + out);
                        out("Checking if " + outFile.getAbsolutePath() + " exists... " + outFile.exists());
                        outputFiles.add(out);
                        if (!outFile.exists()) {
                            gui.progressBar.setValue(i);
                            //For each comment, join the audio & image together with a blurred video background.
                            String mp3 = "rvm_dl_" + comment.DLid + "_thing_" + comment.thingId + ".mp3";
                            String png = "rvm_dl_" + comment.DLid + "_thing_" + comment.thingId + ".png";

                            double tempVideoLength = Double.parseDouble(getOutputFromCommand("\"" +
                                    Config.getLibraryFolder() + "/bin/ffprobe.exe\" -v error -show_entries format=duration " +
                                    "-of default=noprint_wrappers=1:nokey=1 " + mp3));

                            out("Temporary video length found: " + tempVideoLength);

                            ImageIcon icon = new ImageIcon(png);
                            gui.renderPreview.setIcon(getScaledImage(icon, (icon.getIconWidth() / icon.getIconHeight()) * 400, 400));

                            if (comment.isTitle) {
                                png = "rvm_final_" + comment.DLid + "_thumbnail.png";
                            }

                            FilterChain filterChain = FilterChain.of(
                                    Filter.withName("scale")
                                            .addInputLink("2:0")
                                            .addArgument("width", "1920")
                                            .addArgument("height", "-1")
                                            .addOutputLink("scaledImage")
                            );
                            if (!comment.isTitle) {
                                filterChain.addFilter(Filter.withName("overlay")
                                        .addInputLink("1:v")
                                        .addInputLink("scaledImage")
                                        .addArgument("x", "(main_w-overlay_w)/2")
                                        .addArgument("y", "(main_h-overlay_h)/2")
                                        .addOutputLink("finalVideo")
                                );
                            } else {
                                filterChain.addFilter(Filter.withName("colorkey")
                                        .addInputLink("scaledImage")
                                        .addArgument("color", "0x292929")
                                        .addArgument("similarity", "0.05")
                                        .addArgument("blend", "0.1")
                                        .addOutputLink("chromaKeyedImage")
                                );
                                filterChain.addFilter(Filter.withName("overlay")
                                        .addInputLink("1:v")
                                        .addInputLink("chromaKeyedImage")
                                        .addArgument("x", "(main_w-overlay_w)/2")
                                        .addArgument("y", "(main_h-overlay_h)/2")
                                        .addOutputLink("finalVideo")
                                );
                            }

                            FFmpeg ffmpeg1 = createFFmpeg();
                            ffmpeg1
                                    .addInput(
                                            UrlInput.fromPath(new File(Config.getDownloadsFolder() + "/" + mp3).toPath())
                                                    .addArguments("-hwaccel", "nvdec")
                                    )
                                    .addInput(
                                            UrlInput.fromPath(new File(background).toPath())
                                                    .addArguments("-hwaccel", "nvdec")
                                    )
                                    .addInput(
                                            UrlInput.fromPath(new File(Config.getDownloadsFolder() + "/" + png).toPath())
                                                    .setFrameRate(1)
                                                    .addArguments("-hwaccel", "nvdec")
                                    )
                                    .setComplexFilter(FilterGraph.of(
                                            filterChain
                                    ))
                                    .addArguments("-c:v", "h264_nvenc")
                                    .addArgument("-shortest")
                                    .addArguments("-map", "[finalVideo]")
                                    .addArguments("-map", "0:a")
                                    .addArguments("-c:a", "aac")
                                    .setOverwriteOutput(true)
                                    .addOutput(UrlOutput.toPath(outFile.toPath()))
                                    .setLogLevel(LogLevel.INFO)
                                    .setProgressListener(p -> {
                                        gui.individualProgressBar.setVisible(true);
                                        gui.indProgressLabel.setVisible(true);
                                        if (p.getTimeMillis() != null) {
                                            gui.individualProgressBar.setMaximum((int) tempVideoLength);
                                            gui.individualProgressBar.setValue(Math.toIntExact(p.getTimeMillis() / 1000));
                                            gui.individualProgressBar.setIndeterminate(false);
                                            calculateIndRemainingTime(individualStartTime, (long) tempVideoLength, p.getTimeMillis() / 1000);
                                        } else {
                                            gui.individualProgressBar.setIndeterminate(true);
                                        }
                                    })
                                    .setOutputListener(s -> {
                                        out("[FFmpeg CLI] > " + s);
                                        return false;
                                    });

                            ffmpeg1.execute();
                        } else {
                            out("File " + out + " already exists! Skipping...");
                            i++;
                            continue;
                        }
                        i++;
                        if (!(i >= vm.comments.length)) calculateRemainingTime(startTime, vm.comments.length, i);
                    }
                    gui.renderPreview.setVisible(false);
                    gui.timeRemaining.setText("");

                    gui.indProgressLabel.setVisible(false);
                    gui.individualProgressBar.setVisible(false);

                    gui.title.setText("Combining final video");
                    gui.progressBar.setIndeterminate(true);
                    gui.progressLabel.setText("0%");
                    gui.indProgressLabel.setVisible(false);
                    gui.indTimeRemaining.setVisible(false);

                    gui.progressBar.setIndeterminate(true);

                    String audioFile = "rvm_audio_" + DLid + ".mp3";
                    //After all if the individual videos are made, concatenate them into one.

                    FFmpeg ffmpeg2 = createFFmpeg();
                    String outro = randomizeFilesInFolder(Config.getLibraryFolder() + "/outros").get(0).getAbsolutePath();

                    for (String f : outputFiles) {
                        ffmpeg2.addInput(
                                UrlInput.fromPath(new File(Config.getDownloadsFolder() + "/" + f).toPath())
                                //.addArguments("-hwaccel", "nvdec")
                        );
                    }
                    ffmpeg2
                            .addInput(
                                    UrlInput.fromPath(new File(Config.getDownloadsFolder() + "/" + audioFile).toPath())
                                    //.addArguments("-hwaccel", "nvdec")
                            )
                            .addInput(
                                    UrlInput.fromPath(new File(outro).toPath())
                                    //.addArguments("-hwaccel", "nvdec")
                            );
                    Filter concatFilter = Filter.withName("concat");
                    for (i = 0; i < outputFiles.size(); i++) {
                        if (outputFiles.get(i).endsWith(".mp4")) {
                            if (outputFiles.get(i).contains("outros")) {
                                //Outro sequences have no audio attached to them and may not have an audio stream.
                                concatFilter.addInputLink(i + ":0");
                            } else {
                                //Non-outro .mp4 files have audio AND video streams.
                                concatFilter.addInputLink(i + ":0").addInputLink(i + ":1");
                            }
                        } else if (outputFiles.get(i).endsWith(".mp3")) {
                            //.mp3 files only have audio streams.
                            concatFilter.addInputLink(i + ":0");
                        }
                    }

                    double finalLength = length;
                    long startTime3 = System.currentTimeMillis();
                    ffmpeg2
                            .setComplexFilter(FilterGraph.of(
                                    FilterChain.of(
                                            concatFilter
                                                    .addArgument("n", String.valueOf(outputFiles.size()))
                                                    .addArgument("v", "1")
                                                    .addArgument("a", "1")
                                                    .addOutputLink("outv")
                                                    .addOutputLink("outa"),
                                            Filter.withName("aformat")
                                                    .addInputLink("outa")
                                                    .addArgument("sample_fmts", "fltp")
                                                    .addArgument("sample_rates", "22050")
                                                    .addArgument("channel_layouts", "stereo")
                                                    .addOutputLink("a1"),
                                            Filter.withName("aformat")
                                                    .addInputLink(outputFiles.size() + ":0")
                                                    .addArgument("sample_fmts", "fltp")
                                                    .addArgument("sample_rates", "22050")
                                                    .addArgument("channel_layouts", "stereo")
                                                    .addOutputLink("a2"),
                                            Filter.withName("volume")
                                                    .addInputLink("a2")
                                                    .addArgument("volume", "-20.0dB")
                                                    .addOutputLink("a2adj"),
                                            Filter.withName("volume")
                                                    .addInputLink("a1")
                                                    .addArgument("volume", "5.0dB")
                                                    .addOutputLink("a1adj"),
                                            Filter.withName("amerge")
                                                    .addInputLink("a1adj")
                                                    .addInputLink("a2adj")
                                                    .addArgument("inputs", "2")
                                                    .addOutputLink("apresync"),
                                            Filter.withName("aresample")
                                                    .addInputLink("apresync")
                                                    .addArgument("async", "1000")
                                                    .addOutputLink("afinal"),
                                            Filter.withName("mpdecimate")
                                                    .addInputLink("outv")
                                                    .addOutputLink("vfinal"),
                                            Filter.withName("concat")
                                                    .addInputLink("vfinal")
                                                    .addInputLink("afinal")
                                                    .addInputLink((outputFiles.size() + 1) + ":0")
                                                    .addInputLink((outputFiles.size() + 1) + ":1")
                                                    .addArgument("n", "2")
                                                    .addArgument("a", "1")
                                                    .addArgument("v", "1")
                                                    .addOutputLink("v")
                                                    .addOutputLink("a"),
                                            Filter.withName("mpdecimate")
                                                    .addInputLink("v")
                                                    .addOutputLink("v2")
                                    )
                            ))
                            .addOutput(UrlOutput.toPath(new File(Config.getDownloadsFolder() + "/rvm_final_" + DLid + ".mp4").toPath()))
                            .addArguments("-map", "[v2]")
                            .addArguments("-map", "[a]")
                            .addArguments("-r", "30")
                            .addArguments("-c:v", "h264_nvenc")
                            .addArguments("-c:a", "aac")
                            .setOverwriteOutput(true)
                            .setLogLevel(LogLevel.INFO)
                            .setProgressListener(p -> {
                                if (p.getTimeMillis() != null) {
                                    gui.progressBar.setIndeterminate(false);
                                    gui.progressBar.setMaximum((int) finalLength + 20);
                                    gui.progressBar.setValue(Math.toIntExact(p.getTimeMillis() / 1000));
                                    calculateRemainingTime(startTime3, (long) (finalLength + 20), p.getTimeMillis() / 1000);
                                } else {
                                    gui.progressBar.setIndeterminate(true);
                                }
                            }).setOutputListener(o -> {
                        out("[FFmpeg CLI] > " + o);
                        return false;
                    })
                            .execute();

                    String finalPath = Config.getDownloadsFolder() + "/rvm_final_" + DLid + ".mp4";

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
                            //Make sure we remove punctuation or else YouTube will parse the tags wrong and they will be
                            //all combined into one "metatag"
                            tags.add(s.replaceAll("[,?!.\\-=()`~\\[\\]{}*/<>]", ""));
                        }
                    }
                    //tags.addAll(Arrays.asList(title.split(" ")));
                    out("Setting video tags: " + tags.toString());

                    StringBuilder description = new StringBuilder(getDescriptionBlurb(sr, vm.titles) +
                            ((vm.titles.length == 1) ? "\n\n\uD83D\uDCF0 Original Post: " + URL : "\n\n\uD83D\uDCF0 Original Posts:\n"));
                    if (vm.URLs.length != 1) {
                        for (String url : vm.URLs) {
                            description.append("➡️ ").append(url).append("\n");
                        }
                    }

                    int subCount;

                    YouTube youtube = ApiUtils.getService();
                    YouTube.Channels.List request = youtube.channels()
                            .list("statistics");
                    ChannelListResponse response = request.setId("UC9yNvUdCqYvfo4qqxiSiKaA").execute();
                    if (response.getItems().size() > 0) {
                        subCount = response.getItems().get(0).getStatistics().getSubscriberCount().intValue();
                    } else {
                        subCount = 0;
                    }

                    description
                            .append("\n\n")
                            .append(audioCredits.toString())
                            .append("\n\n\uD83D\uDC26 Make sure to follow us on Twitter at " +
                                    "https://twitter.com/TweetsExquisite for dank memes and channel updates.");
                    if (subCount > 0) {
                        description.append("\n\n\uD83D\uDCAF Thank you all for ").append(subCount).append(" subscribers!");
                    }
                    description.append("\n↩️ Make sure to leave your own stories in the comments below! We read and " +
                            "try to respond to every single comment on our videos.");
                    description
                            .append("\n\n#ExquisiteReddit | #")
                            .append(sr.substring(2));

                    description.append("\n\nPosts and comments may be edited for clarity or length. " +
                            "We make no guarantee on the validity of the content showcased in our videos.");

                    gui.title.setText("Uploading to YouTube");
                    gui.timeRemaining.setText("");
                    gui.progressBar.setIndeterminate(false);
                    gui.progressBar.setMaximum(100);
                    gui.progressBar.setValue(0);
                    gui.indProgressLabel.setVisible(false);
                    gui.indTimeRemaining.setVisible(false);
                    UploadVideo.main(finalPath, videoTitle, description.toString(), tags);
                    //The rest of the program is finished in `onVideoIdGathered()`
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    System.err.println("An error has occurred: " + e.getClass().getCanonicalName());
                    err("An error has occurred: " + e.getClass().getCanonicalName());
                    err(sw.toString());
                } finally {
                    System.err.println("Main GUI finished executing.");
                    err("Main GUI finished executing.");
                    exit(0);
                }
                return null;
            }
        };

        worker.execute();
    }

    private static FFmpeg createFFmpeg() {
        return FFmpeg.atPath(new File(Config.getLibraryFolder() + "/bin").toPath());
    }

    static void onVideoIdGathered(String videoId) throws GeneralSecurityException, IOException {
        gui.title.setText("Applying thumbnail");
        gui.progressBar.setValue(2);
        gui.progressLabel.setText("2 / 2");
        SetThumbnail.main(videoId, thumbnails.get(response));

        out("Upload finished!");

        if (requestUserYesOrNo("The upload has finished. Would you like to open your " +
                "video in YouTube Studio?")) {
            new Thread(() -> {
                try {
                    String url = "https://studio.youtube.com/video/" + videoId + "/edit";
                    String url2 = null;
                    YouTube yt = ApiUtils.getService();
                    VideoListResponse response = yt.videos().list("status").setId(videoId).execute();
                    String status = response.getItems().get(0).getProcessingDetails().getProcessingStatus();
                    if (status.equals("succeeded")) {
                        //It processed. We can direct the user to the end screen editor.
                        url2 = "https://www.youtube.com/endscreen?v=" + videoId;
                    }
                    if (System.getProperty("user.home").equals("C:\\Users\\tntaw")) {
                        exec("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe --profile-directory=\"Profile 2\" " + url);
                        if (url2 != null)
                            exec("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe --profile-directory=\"Profile 2\" " + url2);
                    } else {
                        Desktop.getDesktop().browse(new URI(url));
                        if (url2 != null) Desktop.getDesktop().browse(new URI(url2));
                    }
                } catch (IOException | InterruptedException | URISyntaxException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        if (requestUserYesOrNo("Move files to archive folder? (Recommended)")) {
            //Move all of the temp files to a folder for archival purposes.
            gui.title.setText("Cleaning up");
            gui.progressBar.setIndeterminate(false);
            gui.progressBar.setMaximum(vm.comments.length);
            int i = 0;
            long startTime = System.currentTimeMillis();
            for (VideoManifestComment vmc : vm.comments) {
                i++;
                gui.progressBar.setValue(i);
                moveFile(Config.getDownloadsFolder() + "/" + vmc.name, Config.getOutputFolder() + "/Archive/" + vmc.DLid + "/" + vmc.name);
                moveFile(Config.getDownloadsFolder() + "/rvm_temp_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp4", Config.getOutputFolder() + "/Archive/" + vmc.DLid + "/rvm_temp_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp4");
                moveFile(Config.getDownloadsFolder() + "/rvm_dl_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp3", Config.getOutputFolder() + "/Archive/" + vmc.DLid + "/rvm_dl_" + vmc.DLid + "_thing_" + vmc.thingId + ".mp3");
                if (vmc.isTitle) {
                    moveFile(Config.getDownloadsFolder() + "/rvm_final_" + DLid + "_thumbnail.png", Config.getOutputFolder() + "/Archive/" + DLid + "/rvm_final_" + vmc.DLid + "_thumbnail.png");
                }
                calculateRemainingTime(startTime, vm.comments.length, i);
            }
            gui.progressBar.setIndeterminate(true);
            //Move manifest, final pre-cut video, and final post-cut video
            moveFile(Config.getDownloadsFolder() + "/rvm_manifest_" + DLid + ".json", Config.getOutputFolder() + "/Archive/" + DLid + "/rvm_manifest_" + DLid + ".json");
            moveFile(Config.getDownloadsFolder() + "/rvm_final_" + DLid + "_precut.mp4", Config.getOutputFolder() + "/Archive/" + DLid + "/rvm_final_" + DLid + "_precut.mp4");
            moveFile(Config.getDownloadsFolder() + "/rvm_final_" + DLid + "_preoutro.mp4", Config.getOutputFolder() + "/Archive/" + DLid + "/rvm_final_" + DLid + "_preoutro.mp4");
            moveFile(Config.getDownloadsFolder() + "/rvm_audio_" + DLid + ".mp3", Config.getOutputFolder() + "/Archive/" + DLid + "/rvm_audio_" + DLid + ".mp3");
            moveFile(Config.getDownloadsFolder() + "/rvm_final_" + DLid + ".mp4", Config.getOutputFolder() + "/Final/" + DLid + ".mp4");
            gui.progressBar.setIndeterminate(false);
            gui.progressBar.setValue(1);
            gui.progressBar.setMaximum(1);
        } else {
            out("Moving files skipped. Everything is still in the Downloads folder, which may screw up this program in the future.");
        }

        gui.progressBar.setIndeterminate(false);
        gui.progressBar.setMaximum(1);
        gui.progressBar.setValue(1);
        gui.individualProgressBar.setValue(1);
        gui.individualProgressBar.setMaximum(1);
        gui.individualProgressBar.setVisible(false);
        gui.progressLabel.setText("");
        gui.indProgressLabel.setText("");
        gui.indTimeRemaining.setText("");
        gui.timeRemaining.setText("");
        gui.title.setText("Done");

        // yay~yay~yay
        // WE'RE DONE!
        // yay~yay~yay

        out("Everything's done!");
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

    @SuppressWarnings("SameParameterValue")
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
    private static void setRemainingTimeText(int seconds, JLabel tv) {
        SwingUtilities.invokeLater(() -> tv.setText(convertToTimeString(seconds)));
    }

    /**
     * Log output to System.out and the text area in the GUI.
     *
     * @param str The String to output
     */
    static void out(String str) {
        Date d = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:SS");
        String date = dateFormat.format(d);
        System.out.println(str);
        if (getGUI() != null) gui.logArea.append(date + " [out] " + str + "\n");
        log.append(date).append(" [out] ").append(str).append("\n");
    }

    /**
     * Log output to System.err and the text area in the GUI.
     *
     * @param str The String to output
     */
    static void err(String str) {
        Date d = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:SS");
        String date = dateFormat.format(d);
        System.err.println(str);
        if (getGUI() != null) gui.logArea.append(date + " [err] " + str + "\n");
        log.append(date).append(" [err] ").append(str).append("\n");
    }

    /**
     * Exit using System.exit after saving
     * the current log to a file.
     */
    static void exit(int code) {
        File logFile = new File(System.getenv("LOCALAPPDATA") + "/rvm_logs/log_" + System.currentTimeMillis() + ".txt");
        //noinspection ResultOfMethodCallIgnored
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

    /*
    By default, set the overall progress bar.
     */
    static void calculateRemainingTime(long startTime, long length, long currentIndex) {
        calculateRemainingTime(startTime, length, currentIndex, gui.timeRemaining);
        gui.progressLabel.setText((int) ((float) currentIndex / (float) length * 100) + "%");
        //out("Set current progress to " + ((float) currentIndex / (float) length * 100) + "% (" +
        //        (float) currentIndex + " / " + (float) length + ")");
    }

    /*
    By default, set the overall progress bar.
     */
    private static void calculateIndRemainingTime(long startTime, long length, long currentIndex) {
        calculateRemainingTime(startTime, length, currentIndex, gui.indTimeRemaining);
        gui.indProgressLabel.setText((int) ((float) currentIndex / (float) length * 100) + "%");
        //out("Set current (individual) progress to " + ((float) currentIndex / (float) length * 100) +
        //        "% (" + (float) currentIndex + " / " + (float) length + ")");
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
    private static void calculateRemainingTime(long startTime, long length, long currentIndex, JLabel tv)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        //Calculate the remaining time
        long elapsedTime = System.currentTimeMillis() - startTime;

        //if (currentIndex > length) throw new IndexOutOfBoundsException("currentIndex > length.");
        //if (elapsedTime < 0) throw new IllegalArgumentException("startTime occurs after the current time.");

        if (currentIndex == 0) currentIndex = 1;

        long totalTime = (length * (elapsedTime / currentIndex));
        long remainingTime = totalTime - elapsedTime;
        if (remainingTime < 1000 && remainingTime > 100) remainingTime = 1000;
        final int[] seconds = {(int) (remainingTime / 1000)};
        setRemainingTimeText(seconds[0], tv);
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
                    setRemainingTimeText(seconds[0], tv);
                } catch (ArithmeticException e) {
                    //Attempted to divide by 0. Just continue so it returns "Estimating..." instead of an error.
                }
            }
        }, 0, 1000);
        //SwingUtilities.invokeLater(() -> gui.timeRemaining.setText("Estimating..."));
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
                    ids.add(name.substring(13, name.lastIndexOf(".json")));
                }
            }
            if (ids.size() == 0) return null;
            if (ids.size() == 1) return ids.get(0);

            int resp = JOptionPane.showOptionDialog(null,
                    "Choose a Download ID to use: ",
                    "Select an option", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, ids.toArray(new String[]{}), ids.get(0));
            if (ids.get(resp) == null) {
                exit(65);
            }

            return ids.get(resp);
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

    private static String convertToTimestamp(double timestamp) {
        int minutes = 0;
        while (timestamp >= 60) {
            timestamp -= 60;
            minutes++;
        }
        String seconds = ((int) timestamp) + "";
        //Pad the seconds with a zero if seconds < 10
        if (seconds.length() == 1) seconds = "0" + seconds;
        return minutes + ":" + seconds;
    }
}