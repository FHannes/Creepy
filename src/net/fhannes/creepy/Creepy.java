package net.fhannes.creepy;

import java.io.File;
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
                    "FOREIGN KEY (source) REFERENCES urls(id)," +
                    "FOREIGN KEY (target) REFERENCES urls(id))");
        } finally {
            stmt.close();
            db.commit();
        }
    }

    public int process() throws SQLException, ClassNotFoundException, InterruptedException {
        threads = Executors.newFixedThreadPool(threadCount);
        List<CreepyJob> jobs = makeJobs(100);
        for (CreepyJob job : jobs)
            threads.execute(new CreepyWorker(dbFile, job));
        threads.shutdown();
        threads.awaitTermination(10, TimeUnit.MINUTES);
        Statement stmt = db.createStatement();
        try {
            for (CreepyJob job : jobs) {
                if (!job.isFinished())
                    continue;
                Iterator<CreepyURL> itFoundURLs = job.urlIterator();
                while (itFoundURLs.hasNext()) {
                    CreepyURL url = itFoundURLs.next();
                    stmt.executeUpdate(new StringBuilder("INSERT OR IGNORE INTO urls (url) VALUES ('").
                            append(url).
                            append("')").
                            toString());
                    stmt.executeUpdate(new StringBuilder("INSERT OR IGNORE INTO links (source, target) VALUES (").
                            append(job.getID()).
                            append(", (SELECT id FROM urls WHERE url = '").
                            append(url).
                            append("'))").
                            toString());
                }
                stmt.executeUpdate(new StringBuilder("UPDATE urls SET last = CURRENT_TIMESTAMP WHERE id = ").
                        append(job.getID()).toString());
            }
        } finally {
            stmt.close();
            db.commit();
        }
        return jobs.size();
    }

}
