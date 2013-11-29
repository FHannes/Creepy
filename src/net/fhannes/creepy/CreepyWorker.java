package net.fhannes.creepy;

import java.io.File;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CreepyWorker extends CreepyDBAgent implements Runnable {

    private static final Pattern pHtmlParse = Pattern.compile("<\\s*a.*?href\\s*=\\s*\"([^\"#?]*).*?\">",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final CreepyURL url;

    public CreepyWorker(File dbFile, CreepyURL url) throws SQLException, ClassNotFoundException {
        super(dbFile);
        this.url = url;
    }

    @Override
    public void run() {
        try {
            URLConnection conn = url.getURL().openConnection();
            if (conn.getContentType().toLowerCase().startsWith("html/text")) {
                // TODO: Implement html-aware link parser?
                Matcher matchLinks = pHtmlParse.matcher(conn.getContent().toString());
                List<CreepyURL> links = new ArrayList<CreepyURL>();
                while (matchLinks.matches()) {
                    String link = matchLinks.group(1);
                    CreepyURL newURL = null;
                    if (CreepyURL.isRelative(link))
                        newURL = url.makeRelative(link);
                    else
                        newURL = new CreepyURL(link);
                    links.add(newURL);
                }
                addURL(links);
                //updateURL(url); // TODO: Update url timestamp to indicate (last) check
            } else
                deleteURL(url);
        } catch (Exception e) { }
    }

}
