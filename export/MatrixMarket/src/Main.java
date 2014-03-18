import net.fhannes.creepy.CreepyDBAgent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * This program will export a database made by the crawler to a MatrixMarket format.
 */
public class Main extends CreepyDBAgent {

    public Main(File dbFile) throws ClassNotFoundException, SQLException {
        super(dbFile);
    }

    public void export(int urlCount, File matrixFile, File urlsFile) throws SQLException, IOException {
        Set<Integer> acceptedIDs = new HashSet<>();

        BufferedWriter urlsWriter = new BufferedWriter(new FileWriter(urlsFile));
        urlsWriter.append(urlCount + "\n");
        Statement stmt = getDB().createStatement();
        try {
            StringBuilder sql = new StringBuilder("SELECT id, url FROM urls ORDER BY id ASC LIMIT ").
                    append(urlCount);
            ResultSet rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                acceptedIDs.add(rs.getInt(1));
                urlsWriter.append(rs.getString(2) + "\n");
            }
        } finally {
            stmt.close();
            getDB().commit();
            urlsWriter.close();
        }

        BufferedWriter marketWriter = new BufferedWriter(new FileWriter(matrixFile));
        marketWriter.append(urlCount + "\n");
        stmt = getDB().createStatement();
        try {
            StringBuilder sql = new StringBuilder("SELECT source, target FROM links WHERE (source IN (SELECT id FROM ")
                    .append("urls ORDER BY id ASC LIMIT ").append(urlCount).append(")) ORDER BY source, target");
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                int target = rs.getInt(2);
                if (acceptedIDs.contains(target)) // Workaround because query was too slow
                    marketWriter.append((rs.getInt(1) - 1) + " " + (target - 1) + "\n");
            }
        } finally {
            stmt.close();
            getDB().commit();
            marketWriter.close();
        }
    }

    public static final void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        Main prgmExport = new Main(new File(args[0]));
        prgmExport.export(Integer.parseInt(args[1]), new File(args[2] + "-matrixmarket.txt"), new File(args[2] + "-urls.txt"));
    }

}
