public class VideoManifest {
    public VideoManifestComment[] comments;
    public String title;
    public String subreddit;
    public String URL;

    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("\nTitle: ").append(title).append(" (").append(subreddit).append(")");
        int i = 0;
        for (VideoManifestComment c : comments) {
            toReturn.append("\n\tComment #").append(i).append(": ").append(c.text).append(" (").append(c.name).append(")");
            i++;
        }
        return toReturn.toString();
    }
}
