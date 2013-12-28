package net.fhannes.creepy;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Creepy extends CreepyDBAgent {

    private final int threadCount;
    private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
    private ExecutorService threads;

    public Creepy(File file) throws SQLException, ClassNotFoundException {
        this(file, Runtime.getRuntime().availableProcessors());
    }

    public Creepy(File file, int threadCount) throws SQLException, ClassNotFoundException {
        super(file);

        this.threadCount = threadCount;

        Statement stmt = db.createStatement();
        try {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS urls (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "url STRING NOT NULL UNIQUE," +
                    "last DATETIME DEFAULT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS links (" +
                    "source INTEGER," +
                    "target INTEGER," +
                    "FOREIGN KEY (source) REFERENCES urls(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (target) REFERENCES urls(id) ON DELETE CASCADE)");
        } finally {
            stmt.close();
            db.commit();
        }
    }

    public int process() throws SQLException, ClassNotFoundException, InterruptedException, MalformedURLException {
        threads = Executors.newFixedThreadPool(threadCount);
        List<CreepyJob> jobs = makeJobs(100);
        for (CreepyJob job : jobs)
            threads.submit(new CreepyWorker(httpClient, job));
        threads.shutdown();
        threads.awaitTermination(10, TimeUnit.MINUTES);
        PreparedStatement stmtURL = db.prepareStatement("INSERT OR IGNORE INTO urls (url) VALUES (?)");
        PreparedStatement stmtLink = db.prepareStatement("INSERT OR IGNORE INTO links (source, target) VALUES (?, (SELECT id FROM urls WHERE url = ?))");
        PreparedStatement updateURL = db.prepareStatement("UPDATE urls SET last = CURRENT_TIMESTAMP WHERE id = ?");
        PreparedStatement deleteURL = db.prepareStatement("DELETE FROM urls WHERE id = ?");
        try {
            for (CreepyJob job : jobs) {
                if (!job.isFinished())
                    continue;
                if (!job.hasFailed()) {
                    Iterator<String> itFoundURLs = job.urlIterator();
                    while (itFoundURLs.hasNext()) {
                        String url = itFoundURLs.next();
                        stmtURL.setString(1, url);
                        stmtURL.addBatch();
                        stmtLink.setLong(1, job.getID());
                        stmtLink.setString(2, url);
                        stmtLink.addBatch();
                        updateURL.setLong(1, job.getID());
                        updateURL.addBatch();
                    }
                } else {
                    deleteURL.setLong(1, job.getID());
                    deleteURL.addBatch();
                }
            }
        } finally {
            stmtURL.executeBatch();
            stmtLink.executeBatch();
            updateURL.executeBatch();
            deleteURL.executeBatch();
            db.commit();
            stmtURL.close();
            stmtLink.close();
        }
        return jobs.size();
    }

}
