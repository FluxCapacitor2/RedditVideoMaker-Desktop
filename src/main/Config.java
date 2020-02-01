package main;

import java.util.prefs.Preferences;

public class Config {
    public static Preferences prefs = Preferences.userRoot().node("RedditVideoMaker");

    static String getBackground() {
        return prefs.get("background", null);
    }

    static void setBackground(String background) {
        prefs.put("background", background);
    }

    static String getRandomBackground() {
        return Main.randomizeFilesInFolder(getLibraryFolder() + "/backgrounds/").get(0).getAbsolutePath();
    }

    public static String getDownloadsFolder() {
        return prefs.get("downloadsFolder", "D:/Downloads");
    }

    static void setDownloadsFolder(String downloadsFolder) {
        prefs.put("downloadsFolder", downloadsFolder);
    }

    public static String getLibraryFolder() {
        return prefs.get("libraryFolder", "D:/Library");
    }

    static void setLibraryFolder(String libraryFolder) {
        prefs.put("libraryFolder", libraryFolder);
    }

    static String getOutputFolder() {
        return prefs.get("outputFolder", "D:/Library");
    }

    static void setOutputFolder(String outputFolder) {
        prefs.put("outputFolder", outputFolder);
    }

    static boolean getCalcLength() {
        return prefs.getBoolean("calcVideoLength", true);
    }

    static void setCalcLength(boolean calcLength) {
        prefs.putBoolean("calcVideoLength", calcLength);
    }
}
