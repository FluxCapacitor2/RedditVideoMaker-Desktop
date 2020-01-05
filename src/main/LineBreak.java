package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Esko Piirainen (on StackOverflow)
 * Edited by FluxCapacitor to fit the needs of RedditVideoMaker.
 */
class LineBreak {

    static List<String> wrap(String text, FontMetrics metrics, int maxWidth) {
        StringTokenizer st = new StringTokenizer(text);

        List<String> list = new ArrayList<String>();
        String line = "";
        String lineBeforeAppend = "";
        while (st.hasMoreTokens()) {
            String seg = st.nextToken();
            lineBeforeAppend = line;
            line += seg + " ";
            int width = metrics.stringWidth(line);
            if (width < maxWidth) {
                continue;
            } else { //new Line.
                list.add(lineBeforeAppend);
                line = seg + " ";
            }
        }
        //the remaining part.
        if (line.length() > 0) {
            list.add(line);
        }
        return list;
    }
}
