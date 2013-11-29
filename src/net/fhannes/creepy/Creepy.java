package net.fhannes.creepy;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
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

    public void process(int maxLinks) throws SQLException, ClassNotFoundException, InterruptedException {
        threads = Executors.newFixedThreadPool(threadCount);
        List<CreepyURL> list = getURLs(maxLinks);
        for (CreepyURL url : list)
            threads.execute(new CreepyWorker(dbFile, url));
        threads.shutdown();
        threads.awaitTermination(10, TimeUnit.MINUTES);
    }

}
