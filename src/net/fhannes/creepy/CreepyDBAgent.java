package net.fhannes.creepy;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *
 */
public class CreepyDBAgent {

    protected final Connection db;

    public CreepyDBAgent(File dbFile) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        db = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        db.setAutoCommit(false);
    }

    @Override
    protected void finalize() throws Throwable {
        db.close();
        super.finalize();
    }

    /**
     * Adds a new URL to the urls table in the database.
     *
     * @param url The given URL identifier
     */
    public void addURL(CreepyURL url) throws SQLException {
        if (!url.isValid())
            return;
        Statement stmt = db.createStatement();
        try {
            StringBuilder sql = new StringBuilder("INSERT OR IGNORE INTO urls (url) VALUES ('").append(url).append("')");
            stmt.executeUpdate(sql.toString());
        } finally {
            stmt.close();
            db.commit();
        }
    }

    /**
     * Adds new URLs to the urls table in the database.
     *
     * @param urls The given list of URL identifier
     */
    public void addURL(List<CreepyURL> urls) throws SQLException {
        Statement stmt = db.createStatement();
        try {
            for (CreepyURL url : urls) {
                if (!url.isValid())
                    continue;
                StringBuilder sql = new StringBuilder("INSERT OR IGNORE INTO urls (url) VALUES ('").append(url).append("')");
                stmt.executeUpdate(sql.toString());
            }
        } finally {
            stmt.close();
            db.commit();
        }
    }

    /**
     * Deletes an URL from the urls table in the database.
     *
     * @param url The given URL identifier
     */
    public void deleteURL(CreepyURL url) throws SQLException {
        if (!url.isValid())
            return; // TODO: Throw exception or ignore?
        Statement stmt = db.createStatement();
        try {
            StringBuilder sql = new StringBuilder("DELETE FROM urls WHERE url = '").append(url.toString()).append('\'');
            stmt.executeUpdate(sql.toString());
        } finally {
            stmt.close();
            db.commit();
        }
    }

}
