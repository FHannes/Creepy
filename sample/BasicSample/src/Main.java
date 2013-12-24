import net.fhannes.creepy.Creepy;
import net.fhannes.creepy.CreepyURL;

import java.io.File;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
        Creepy crawly = new Creepy(new File(args[0]));
        crawly.addURL(new CreepyURL("http://www.scar-divi.com"));
        int links = 0;
        while (true) {
            links += crawly.process();
            System.out.println(links);
        }
    }

}
