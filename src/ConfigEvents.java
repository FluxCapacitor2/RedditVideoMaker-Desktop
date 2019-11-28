abstract class ConfigEvents {
    abstract void onBackgroundChanged(String newValue);

    abstract void onAlwaysOnTopChanged(boolean newValue);

    abstract void onDownloadsFolderChanged(String newValue);

    abstract void onLibraryFolderChanged(String newValue);

    abstract void onOutputFolderChanged(String newValue);
}
