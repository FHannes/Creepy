package net.fhannes.creepy;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class Creepy extends CreepyDBAgent {

    private final ExecutorService threads;

    public Creepy(File file) throws SQLException, ClassNotFoundException {
        this(file, Runtime.getRuntime().availableProcessors());
    }

    public Creepy(File file, int threadCount) throws SQLException, ClassNotFoundException {
        super(file);

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

        threads = Executors.newFixedThreadPool(threadCount);
    }

    public void process(int maxLinks) {
    }

}
