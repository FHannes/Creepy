import net.fhannes.creepy.CreepyDBAgent;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Counts the number of visited urls in a database outputted by the crawler.
 */
public class Main extends CreepyDBAgent {

    public Main(File dbFile) throws ClassNotFoundException, SQLException {
        super(dbFile);
    }

    public int getCount() throws SQLException {
        Statement stmt = getDB().createStatement();
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM urls WHERE last IS NOT NULL");
            ResultSet rs = stmt.executeQuery(sql.toString());
            while (rs.next())
                return rs.getInt(1);
        } finally {
            stmt.close();
            getDB().commit();
        }
        return -1;
    }

    public static final void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        Main prgmCount = new Main(new File(args[0]));
        System.out.println(prgmCount.getCount());
    }

}
