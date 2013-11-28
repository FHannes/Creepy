package net.fhannes.creepy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */
public class Worker implements Runnable {

    private final URL url;

    public Worker(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    @Override
    public void run() {
        try {
            URLConnection conn = url.openConnection();

        } catch (IOException e) { }
    }

}
