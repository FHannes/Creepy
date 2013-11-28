import net.fhannes.creepy.Creepy;
import net.fhannes.creepy.CreepyURL;
import org.tmatesoft.sqljet.core.SqlJetException;

import java.io.File;

public class Main {

    public static void main(String[] args) throws SqlJetException {
        Creepy crawly = new Creepy(new File(args[0]));
        crawly.addURL(new CreepyURL("http://www.google.com"));
    }

}
