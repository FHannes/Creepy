package net.fhannes.creepy;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;

/**
 *
 */
public class CreepyWorker extends CreepyDBAgent implements Runnable {

    private final CloseableHttpClient httpClient;
    private final HttpContext httpContext = HttpClientContext.create();
    private final HttpGet httpGet;

    private final CreepyJob job;

    public CreepyWorker(CloseableHttpClient httpClient, File dbFile, CreepyJob job) throws SQLException, ClassNotFoundException {
        super(dbFile);
        this.httpClient = httpClient;
        URI uri = null;
        try {
            URL url = new URL(URLDecoder.decode(job.getURL()));
            uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
        } catch (Exception e) {
            // TODO: job failed!
        }
        this.httpGet = new HttpGet(uri);
        this.job = job;
    }

    @Override
    public void run() {
        try {
            // TODO: How best to handle redirects?
            CloseableHttpResponse response = httpClient.execute(httpGet, httpContext);
            try {
                HttpEntity entity = response.getEntity();
                if (entity.getContentType().getValue().startsWith("text/html")) {
                    String content = EntityUtils.toString(entity);
                    Document doc = Jsoup.parse(content, job.getURL().toString());
                    Elements links = doc.select("a[href]");
                    for (Element eLink : links)
                        job.addURL(eLink.attr("abs:href"));
                    job.finish();
                    // TODO: Add links between urls
                } else
                    deleteURL(job.getURL());
            } finally {
                response.close();
            }
        } catch (Exception e) {
            try {
                deleteURL(job.getURL());
            } catch (SQLException e1) { }
        }
    }

}
