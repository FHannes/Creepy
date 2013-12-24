import net.fhannes.creepy.Creepy;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException, MalformedURLException {
        Creepy crawly = new Creepy(new File(args[0]));
        crawly.addURL("http://www.scar-divi.com");
        long time = System.currentTimeMillis();
        int links = 0;
        while (true) {
            links += crawly.process();
            System.out.println(links + " link(s) processed, rate: " + links / ((float) (System.currentTimeMillis() - time) / 1000) + " links/s");
        }
    }

}
