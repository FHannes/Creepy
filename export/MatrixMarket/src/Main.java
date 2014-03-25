import net.fhannes.creepy.CreepyDBAgent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This program will export a database made by the crawler to a MatrixMarket format.
 */
public class Main extends CreepyDBAgent {

    public Main(File dbFile) throws ClassNotFoundException, SQLException {
        super(dbFile);
    }

    public void export(int urlCount, File matrixFile, File urlsFile, boolean byRow) throws SQLException, IOException {
        Map<Integer, Integer> idMap = new HashMap<>();
        int idx = 0;

        if (urlsFile.exists())
            urlsFile.delete();
        BufferedWriter urlsWriter = new BufferedWriter(new FileWriter(urlsFile));
        urlsWriter.append(urlCount + "\n");
        Statement stmt = getDB().createStatement();
        try {
            StringBuilder sql = new StringBuilder("SELECT id, url FROM urls ORDER BY id ASC LIMIT ").
                    append(urlCount);
            ResultSet rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                idMap.put(rs.getInt(1), idx++);
                urlsWriter.append(rs.getString(2) + "\n");
            }
        } finally {
            stmt.close();
            getDB().commit();
            urlsWriter.close();
        }

        if (matrixFile.exists())
            matrixFile.delete();
        BufferedWriter marketWriter = new BufferedWriter(new FileWriter(matrixFile));
        marketWriter.append(urlCount + "\n");
        stmt = getDB().createStatement();
        try {
            StringBuilder sql = new StringBuilder("SELECT source, target FROM links WHERE (source IN (SELECT id FROM ")
                    .append("urls ORDER BY id ASC LIMIT ").append(urlCount).append(")) ORDER BY ");
            if (byRow)
                sql.append("source, target");
            else
                sql.append("target, source");
            ResultSet rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                int target = rs.getInt(2);
                if (idMap.containsKey(target)) // Workaround because query was too slow
                    marketWriter.append(idMap.get(rs.getInt(1)) + " " + idMap.get(target) + "\n");
            }
        } finally {
            stmt.close();
            getDB().commit();
            marketWriter.close();
        }
    }

    public static final void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        String path = null, output = null;
        int count = 0;
        boolean byRow = true;
        for (int idx = 0; idx < args.length; idx++) {
            String arg = args[idx];
            switch (arg) {
                case "-db":
                    path = args[++idx];
                    break;
                case "-out":
                    output = args[++idx];
                    break;
                case "-count":
                    count = Integer.parseInt(args[++idx]);
                    break;
                case "-bycol":
                    byRow = false;
                    break;
            }
        }
        if (path == null || output == null || count <= 1) {
            System.out.println("Params: Main -db PATH -count EXPORT_COUNT -out OUTPUT_NAME [-bycol]");
            return;
        }
        Main prgmExport = new Main(new File(path));
        prgmExport.export(count, new File(output + "-matrixmarket.txt"), new File(output + "-urls.txt"), byRow);
    }

}
