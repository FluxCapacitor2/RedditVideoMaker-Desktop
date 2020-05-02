package main;

public class VideoManifestComment {


    //A unique filename relative to `Config.getDownloadsFolder()`
    public String[] sentences;
    //The text in the comment, divided up by punctuation.
    public String[] text;
    //Reddit's "Thing ID" for the comment. Used to identify comments.
    public String thingId;
    //The download ID of the comment. Used to make filenames completely unique.
    public String DLid;
    //true if the comment is a post title and should be substituted by the thumbnail during rendering.
    public boolean isTitle;
    //The name of the subreddit, in the format "/r/SUBREDDITNAME/""
    public String subreddit;
    //The index of the comment in the individual post.
    public int indexInPost;
    //If the comment is a parent. Used to determine when a transition plays.
    public boolean isParent;
}