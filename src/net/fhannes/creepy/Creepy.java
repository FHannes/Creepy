package net.fhannes.creepy;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import java.io.File;

/**
 *
 */
public class Creepy {

    private SqlJetDb db;

    public Creepy(File file) throws SqlJetException {
        db = SqlJetDb.open(file, true);
        db.getOptions().setAutovacuum(true);

        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            db.createTable("CREATE TABLE IF NOT EXISTS urls (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "url STRING NOT NULL," +
                    "last DATETIME DEFAULT NULL)");
            db.createIndex("CREATE INDEX url_idx ON urls(url)");
            db.createTable("CREATE TABLE IF NOT EXISTS links (" +
                    "source INTEGER," +
                    "target INTEGER," +
                    "FOREIGN KEY (source) REFERENCES urls(id)," +
                    "FOREIGN KEY (target) REFERENCES urls(id))");
        } finally {
            db.commit();
        }
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
     * @throws SqlJetException
     */
    public void addURL(String url) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            db.getTable("urls").insert(URLUtils.normalizeURL(url));
        } finally {
            db.commit();
        }
    }
}
