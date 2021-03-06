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

        Statement stmt = getDB().createStatement();
        try {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS urls (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "url STRING NOT NULL UNIQUE," +
                    "last DATETIME DEFAULT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS links (" +
                    "source INTEGER," +
                    "target INTEGER," +
                    "FOREIGN KEY (source) REFERENCES urls(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (target) REFERENCES urls(id) ON DELETE CASCADE," +
                    "PRIMARY KEY (source ASC, target ASC) ON CONFLICT IGNORE)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_source ON links (source ASC);");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_target ON links (target ASC);");
        } finally {
            stmt.close();
            getDB().commit();
        }
    }

    public int process() throws SQLException, ClassNotFoundException, InterruptedException, MalformedURLException {
        threads = Executors.newFixedThreadPool(threadCount);
        List<CreepyJob> jobs = makeJobs(threadCount * 3);
        for (CreepyJob job : jobs)
            threads.submit(new CreepyWorker(httpClient, job));
        threads.shutdown();
        threads.awaitTermination(10, TimeUnit.MINUTES);
        PreparedStatement stmtURL = getDB().prepareStatement("INSERT OR IGNORE INTO urls (url) VALUES (?)");
        PreparedStatement stmtLink = getDB().prepareStatement("INSERT OR IGNORE INTO links (source, target) VALUES (?, (SELECT id FROM urls WHERE url = ?))");
        PreparedStatement updateURL = getDB().prepareStatement("UPDATE urls SET last = CURRENT_TIMESTAMP WHERE id = ?");
        PreparedStatement deleteURL = getDB().prepareStatement("DELETE FROM urls WHERE id = ?");
        try {
            for (CreepyJob job : jobs)
                if (!job.hasFailed()) {
                    if (!job.isFinished())
                        continue;
                    Iterator<String> itFoundURLs = job.urlIterator();
                    while (itFoundURLs.hasNext()) {
                        String url = itFoundURLs.next();
                        stmtURL.setString(1, url);
                        stmtURL.addBatch();
                        stmtLink.setLong(1, job.getID());
                        stmtLink.setString(2, url);
                        stmtLink.addBatch();
                    }
                    updateURL.setLong(1, job.getID());
                    updateURL.addBatch();
                } else {
                    deleteURL.setLong(1, job.getID());
                    deleteURL.addBatch();
                }
        } finally {
            stmtURL.executeBatch();
            stmtLink.executeBatch();
            updateURL.executeBatch();
            deleteURL.executeBatch();
            getDB().commit();
            stmtURL.close();
            stmtLink.close();
        }
        return jobs.size();
    }

}
