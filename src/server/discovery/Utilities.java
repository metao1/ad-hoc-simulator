package server.discovery;

import server.discovery.logger.Logger;
import server.discovery.proto.NodeFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utilities {
    static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static final NodeFactory.NodeType[] nodeTypes = NodeFactory.NodeType.values();

    public static void setSwingFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }

    public static void showError(String error) {
        JOptionPane.showMessageDialog(null, error, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(String info, String title) {
        JOptionPane.showMessageDialog(null, info, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static String popupAskUser(String question, String[] answers, String title) {
        int answer = JOptionPane.showOptionDialog(null,
                question, title, 0,
                JOptionPane.QUESTION_MESSAGE, null, answers, answers[0]);
        // Return null if the user closed the dialog box
        if (answer == JOptionPane.CLOSED_OPTION) {
            return null;
        }

        // Return their selection
        return answers[answer];

    }

    public static NodeFactory.NodeType popupAskNodeType() {
        // Get every node type
        NodeFactory.NodeType nTypes[] = getNodeTypes();

        int answer = JOptionPane.showOptionDialog(null,
                "Select a simulation type.", "Select a simulation type.", 0,
                JOptionPane.QUESTION_MESSAGE, null, nTypes, nTypes[0]);

        // Return null if the user closed the dialog box
        if (answer == JOptionPane.CLOSED_OPTION) {
            return null;
        }

        // Return their selection
        return nTypes[answer];
    }

    public static String getTmpLogPath() {
        String tmpDir = System.getProperty("java.io.tmpdir");

        // On some JVMs, a trailing file separator doesn't exist. Correct this.
        if (!tmpDir.endsWith(System.getProperty("file.separator"))) {
            tmpDir = tmpDir + System.getProperty("file.separator");
        }

        return tmpDir + "serverDiscoverylog.tmp";
    }

    public static void runSaveLogDialog(Container parent) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Log Files",
                "log");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            // Define the new files to be saved.
            File logFile = new File(getTmpLogPath());
            File saveFile = new File(chooser.getSelectedFile().getPath() + ".log");

            if (!logFile.exists()) {
                JOptionPane.showMessageDialog(parent, "There is nothing to save yet.");
                return;
            }

            // Check to see if we will overwrite the file
            if (saveFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(null,
                        "File already exists, do you want to overwrite?");
                if (overwrite == JOptionPane.CANCEL_OPTION
                        || overwrite == JOptionPane.CLOSED_OPTION
                        || overwrite == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            // Initialize the file readers and writers
            FileReader in = null;
            FileWriter out = null;

            // Try to open each file
            try {
                int c;
                // Make sure everything has been flushed out of the buffer
                // and has been written to the temporary file.
                Logger logger = Logger.getInstance();
                logger.flushLogFile();

                in = new FileReader(logFile);
                out = new FileWriter(saveFile);

                // Write each line of the first file to the file chosen.
                while ((c = in.read()) != -1) {
                    out.write(c);
                }

                // Close both files.
                in.close();
                out.close();

            } catch (FileNotFoundException e1) {
                showError("Log file could not be saved at " + chooser.getSelectedFile().getPath());
            } catch (IOException e1) {
                showError("Log file could not be saved due to an IO error.");
            }
        }
    }

    public static NodeFactory.NodeType[] getNodeTypes() {
        return nodeTypes;
    }

    public static String timeStamp() {
        Calendar cal = Calendar.getInstance();
        return sdf.format(cal.getTime());
    }

    public static void captureScreen(Component Area) {

        // Find out where the user would like to save their screen shot
        String fileName = null;
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Screen Shots", "png");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File saveFile = new File(chooser.getSelectedFile().getPath() + ".png");
            fileName = saveFile.toString();

            // Check to see if we will overwrite the file
            if (saveFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(null,
                        "File already exists, do you want to overwrite?");
                if (overwrite == JOptionPane.CANCEL_OPTION
                        || overwrite == JOptionPane.CLOSED_OPTION
                        || overwrite == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }
        // If they didn't hit approve, return
        else {
            return;
        }

        // Determine the exact coordinates of the screen that is to be captured
        Dimension screenSize = Area.getSize();
        Rectangle screenRectangle = new Rectangle();
        screenRectangle.height = screenSize.height;
        screenRectangle.width = screenSize.width;
        screenRectangle.x = Area.getLocationOnScreen().x;
        screenRectangle.y = Area.getLocationOnScreen().y;

        // Here we have to make the GUI Thread sleep for 1/4 of a second
        // just to give the save dialog enough time to close off of the
        // screen. On slower computers they were capturing the screen
        // before the dialog was out of the way.
        try {
            Thread.currentThread();
            Thread.sleep(250);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        // Attempt to capture the screen at the defined location.
        try {
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRectangle);
            ImageIO.write(image, "png", new File(fileName));
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not save screen shoot at: "
                    + fileName);
            e.printStackTrace();
        }
    }

}
