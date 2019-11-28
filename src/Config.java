import java.util.prefs.Preferences;

class Config {
    private static Preferences prefs = Preferences.userRoot().node("RedditVideoMaker");
    private static ConfigEvents listeners;

    static void setListeners(ConfigEvents newValue) {
        listeners = newValue;
    }

    static String getBackground() {
        return prefs.get("background", null);
    }

    static void setBackground(String background) {
        prefs.put("background", background);
        listeners.onBackgroundChanged(background);
    }

    static String getRandomBackground() {
        return Main.randomizeFilesInFolder(Main.DOWNLOADS_FOLDER).get(0).getAbsolutePath();
    }

    static boolean getAlwaysOnTop() {
        return prefs.getBoolean("isAlwaysonTop", false);
    }

    static void setAlwaysOnTop(boolean isAlwaysOnTop) {
        prefs.putBoolean("isAlwaysOnTop", isAlwaysOnTop);
        listeners.onAlwaysOnTopChanged(isAlwaysOnTop);
    }

    static String getDownloadsFolder() {
        return prefs.get("downloadsFolder", "D:/Downloads");
    }

    static void setDownloadsFolder(String downloadsFolder) {
        prefs.put("downloadsFolder", downloadsFolder);
        listeners.onDownloadsFolderChanged(downloadsFolder);
    }

    static String getLibraryFolder() {
        return prefs.get("libraryFolder", "D:/Library");
    }

    static void setLibraryFolder(String libraryFolder) {
        prefs.put("libraryFolder", libraryFolder);
        listeners.onLibraryFolderChanged(libraryFolder);
    }

    static String getOutputFolder() {
        return prefs.get("outputFolder", "D:/Library");
    }

    static void setOutputFolder(String outputFolder) {
        prefs.put("outputFolder", outputFolder);
        listeners.onOutputFolderChanged(outputFolder);
    }

    static boolean getCalcLength() {
        return prefs.getBoolean("calcVideoLength", true);
    }

    static void setCalcLength(boolean calcLength) {
        prefs.putBoolean("calcVideoLength", calcLength);
    }
}
