import net.fhannes.creepy.Creepy;
import net.fhannes.creepy.CreepyURL;

import java.io.File;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Creepy crawly = new Creepy(new File(args[0]));
        crawly.addURL(new CreepyURL("http://www.google.com"));
    }

}
