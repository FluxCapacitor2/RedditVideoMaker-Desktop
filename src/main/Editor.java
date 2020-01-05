package main;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Editor {

    public static void main(String[] args) {
        Pattern manifestPattern = Pattern.compile("(rvm_manifest_[a-z0-9]{4}.json)");


        Gson gson = new Gson();
        try {
            File downloadsFolder = new File("D:/Downloads/");
            for (File f : Objects.requireNonNull(downloadsFolder.listFiles())) {
                String name = f.getName().toLowerCase();
                Matcher manifestMatcher = manifestPattern.matcher(name);
                if (manifestMatcher.matches()) {
                    System.out.println("Found manifest file: " + name);
                    VideoManifest manifest = gson.fromJson(new FileReader(f), VideoManifest.class);
                    String oldJson = gson.toJson(manifest, VideoManifest.class);
                    System.out.println("MANIFEST FOUND: " + name + "\n" + manifest.toString());
                    String remove = Main.requestUserInput("Remove any comments? Enter the first or \"end\" to stop. (Comment information " +
                            "is printed in the console)");
                    if (!remove.equals("end")) {
                        int n = Integer.parseInt(remove);
                        ArrayList<VideoManifestComment> comments = new ArrayList<>(Arrays.asList(manifest.comments));
                        comments.remove(n);
                        manifest.comments = comments.toArray(new VideoManifestComment[]{});
                        String json = gson.toJson(manifest);
                        System.out.println("OLD JSON: \n " + oldJson);
                        System.out.println("NEW JSON: \n" + json);
                        FileWriter fw = new FileWriter(f, false);
                        fw.write(json);
                        fw.close();
                        main(new String[]{});
                        //TODO Make it delete the files associated with the deleted comment so there aren't any stragglers in the Downloads folder.
                    } else {
                        System.exit(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
