package net.fhannes.creepy;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
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

import java.net.URI;
import java.net.URL;

/**
 *
 */
public class CreepyWorker implements Runnable {

    private final static RequestConfig requestConfig = RequestConfig.custom().
            setConnectionRequestTimeout(5000).
            setConnectTimeout(5000).
            setSocketTimeout(5000).build();

    private final CloseableHttpClient httpClient;
    private final HttpContext httpContext = HttpClientContext.create();
    private URI uri = null;

    private final CreepyJob job;

    public CreepyWorker(CloseableHttpClient httpClient, CreepyJob job) {
        this.httpClient = httpClient;
        try {
            URL url = new URL(job.getURL());
            uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
        } catch (Exception e) {
            job.fail();
        }
        this.job = job;
    }

    @Override
    public void run() {
        if (job.hasFailed())
            return;
        try {
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            // TODO: How best to handle redirects?
            CloseableHttpResponse response = null;
            response = httpClient.execute(httpGet, httpContext);
            HttpEntity entity = response.getEntity();
            try {
                if (entity.getContentType().getValue().startsWith("text/html")) {
                    String content = EntityUtils.toString(entity);
                    Document doc = Jsoup.parse(content, job.getURL().toString());
                    Elements links = doc.select("a[href]");
                    for (Element eLink : links)
                        job.addURL(eLink.attr("abs:href"));
                    job.finish();
                } else
                    job.fail();
            } finally {
                EntityUtils.consume(entity);
                response.close();
                httpGet.releaseConnection();
            }
        } catch (Exception e) {
            job.fail(); // TODO: Handle timeouts?
        }
    }

}
