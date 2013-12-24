package net.fhannes.creepy;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CreepyWorker extends CreepyDBAgent implements Runnable {

    private static final Pattern pHtmlParse = Pattern.compile("<a(?:[^>](?<!href=\"))+?href=\"(?!javascript|mailto)([^\"#?]+)[^>]+>.+?</a>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final CreepyJob job;

    public CreepyWorker(File dbFile, CreepyJob job) throws SQLException, ClassNotFoundException {
        super(dbFile);
        this.job = job;
    }

    @Override
    public void run() {
        try {
            // TODO: How best to handle redirects?
            URLConnection conn = job.getURL().getURL().openConnection();
            if (conn.getContentType().toLowerCase().startsWith("text/html")) {
                // TODO: Implement html-aware link parser?
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                try {
                    StringBuilder buffer = new StringBuilder(1048576);
                    char[] data = new char[10240];
                    int len;
                    while ((len = br.read(data)) != -1)
                        buffer.append(data, 0, len);
                    Matcher matchLinks = pHtmlParse.matcher(buffer.toString());
                    while (matchLinks.find()) {
                        String link = matchLinks.group(1);
                        CreepyURL newURL = null;
                        if (CreepyURL.isRelative(link))
                            newURL = job.getURL().makeRelative(link);
                        else
                            newURL = new CreepyURL(link);
                        if (newURL.isValid())
                            job.addURL(newURL);
                    }
                    job.finish();
                    // TODO: Add links between urls
                } finally {
                    br.close();
                }
            } else
                deleteURL(job.getURL());
        } catch (Exception e) {
            try {
                deleteURL(job.getURL());
            } catch (SQLException e1) { }
        }
    }

}
