package net.fhannes.creepy;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import java.io.File;

/**
 *
 */
public class CreepyDBAgent {

    protected final SqlJetDb db;

    public CreepyDBAgent(File dbFile) throws SqlJetException {
        db = SqlJetDb.open(dbFile, true);
    }

    /**
     * Adds a new URL to the urls table in the database.
     *
     * @param url The given URL identifier
     * @throws org.tmatesoft.sqljet.core.SqlJetException
     */
    public void addURL(CreepyURL url) throws SqlJetException {
        if (!url.isValid())
            return; // TODO: Throw exception or ignore?
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            db.getTable("urls").insert(url.toString());
        } finally {
            db.commit();
        }
    }


    /**
     * Deletes an URL from the urls table in the database.
     *
     * @param url The given URL identifier
     * @throws org.tmatesoft.sqljet.core.SqlJetException
     */
    public void deleteURL(CreepyURL url) throws SqlJetException {
        if (!url.isValid())
            return; // TODO: Throw exception or ignore?
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            // TODO: Implement delete
        } finally {
            db.commit();
        }
    }

}
