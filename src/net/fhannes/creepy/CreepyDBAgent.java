package net.fhannes.creepy;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CreepyDBAgent {

    private final File dbFile;
    private final Connection db;

    public CreepyDBAgent(File dbFile) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        this.dbFile = dbFile;
        this.db = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        this.db.setAutoCommit(false);
    }

    public File getDBFile() {
        return dbFile;
    }

    public Connection getDB() {
        return db;
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
     * @throws SQLException
     */
    public void addURL(String url) throws SQLException {
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
     * Deletes an URL from the urls table in the database.
     *
     * @param url The given URL identifier
     * @throws SQLException
     */
    public void deleteURL(String url) throws SQLException {
        Statement stmt = db.createStatement();
        try {
            StringBuilder sql = new StringBuilder("DELETE FROM urls WHERE url = '").append(url.toString()).append('\'');
            stmt.executeUpdate(sql.toString());
        } finally {
            stmt.close();
            db.commit();
        }
    }

    /**
     * Updates the timestamp of a url in the database.
     *
     * @param url The given URL identifier
     * @throws SQLException
     */
    public void updateLastCheck(String url) throws SQLException {
        Statement stmt = db.createStatement();
        try {
            StringBuilder sql = new StringBuilder("UPDATE urls SET last = CURRENT_TIMESTAMP WHERE url = '").
                    append(url).append('\'');
            stmt.executeUpdate(sql.toString());
        } finally {
            stmt.close();
            db.commit();
        }
    }

    public List<CreepyJob> makeJobs(int maxCount) throws SQLException {
        List<CreepyJob> list = new ArrayList<CreepyJob>();
        Statement stmt = db.createStatement();
        try {
            StringBuilder sql = new StringBuilder("SELECT id, url FROM urls WHERE last IS NULL ORDER BY id ASC LIMIT ").
                    append(maxCount);
            ResultSet rs = stmt.executeQuery(sql.toString());
            while (rs.next())
                list.add(new CreepyJob(rs.getLong(1), rs.getString(2)));
        } finally {
            stmt.close();
            db.commit();
        }
        return list;
    }

}
