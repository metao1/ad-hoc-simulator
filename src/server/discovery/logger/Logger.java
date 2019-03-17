/**
 *
 */
package server.discovery.logger;

import server.discovery.InputConsumer;
import server.discovery.OutputConsumer;
import server.discovery.Utilities;
import server.discovery.event.ServerDiscoveryEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Mehrdad Karami Very basic logger. To use, reference the log method in a static
 * context i.e. Logger.log(). Use a ServerDiscoveryEvent as the only parameter.
 * Logger relies on the getLogString() functionality provided by the
 * ServerDiscoveryEvent. The logger is a primary consumer of events dispatched
 * through the output handler. As such, it implements the serverDiscoveryConsumer
 * interface. Use the getInstance() method to reference the logger in a
 * serverDiscoveryConsumer context.
 */
public class Logger implements OutputConsumer, InputConsumer {

    public static String newline = System.getProperty("line.separator");
    private static Logger instance_ = new Logger();
    private static FileWriter fstream;
    private static BufferedWriter out;

    private Logger() {
    }

    public static synchronized void log(ServerDiscoveryEvent e) {

        // if file handle is not init, do it
        if (fstream == null) {
            try {
                deleteLogFile();
                fstream = new FileWriter(Utilities.getTmpLogPath());
            } catch (IOException e2) {
                Utilities.showError("(Fatal) Could not write to the ServerDiscovery temporary file due to an IO exception :" + e2.getMessage());
                System.exit(1);
            }
            out = new BufferedWriter(fstream);
            // append the head of the ServerDiscovery log file
            try {
                out.append(ServerDiscoveryEvent.getLogHeader() + newline);
            } catch (IOException e1) {
                Utilities.showError("(Fatal) Could not write to the ServerDiscovery temporary file due to an IO exception :" + e1.getMessage());
                System.exit(1);
            }

            //Arrange for the file to be deleted on exit
            File tmpFile = new File(Utilities.getTmpLogPath());
            tmpFile.deleteOnExit();
        }

        try {
            out.append(e.getLogString());
        } catch (IOException e1) {
            Utilities.showError("(Fatal) Could not write to the ServerDiscovery temporary file due to an IO exception :" + e1.getMessage());
            System.exit(1);
        }

    }

    public static void deleteLogFile() {
        // Make sure the file handle is closed.
        closeLogFile();
        File tmp = null;
        try {
            tmp = new File(Utilities.getTmpLogPath());
            if (tmp.exists()) {
                tmp.delete();
            }
        } catch (Exception e) {
            // Fail quietly since the file doesn't exist yet
        }

    }

    private static void closeLogFile() {
        if (fstream != null) {
            try {
                if (out != null) {
                    out.flush();
                }
                fstream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        fstream = null;
    }

    public static Logger getInstance() {
        return instance_;
    }

    public void flushLogFile() {
        if (fstream != null) {
            try {
                if (out != null) {
                    out.flush();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // Fulfills the serverDiscoveryConsumer contract
    public void consumeOutput(ServerDiscoveryEvent e) {


        // log the event
        Logger.log(e);


        switch (e.eventType) {

            case OUT_STOP_SIM:
                // Close the log file
                closeLogFile();
                break;
        }


    }

    @Override
    public void consumeInput(ServerDiscoveryEvent e) {
        Logger.log(e);

    }

    ;
}
