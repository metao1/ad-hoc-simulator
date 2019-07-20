/**
 *
 */
package com.metao.server.discovery;

import com.metao.server.discovery.event.ServerDiscoveryEvent;
import com.metao.server.discovery.gui.GUI;
import com.metao.server.discovery.gui.ImageFactory;
import com.metao.server.discovery.logger.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * @author Mehrdad Karami
 */
public class ServerDiscovery {

    public ServerDiscovery() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        //Init images
        ImageFactory.checkInit();
        Utilities.setSwingFont(Defaults.FONT);

        //Set defaults
        if (!Defaults.DEBUG_ENABLED) {
            OutputHandler.addFilteredEvent(ServerDiscoveryEvent.EventType.OUT_DEBUG);
        }


        // Setup the logger to consume serverDiscoveryEvents from both the input handler and
        // the output handler. From henceforth, all serverDiscoveryEvents that pass through
        // the Input and Output handlers will be logged.
        InputHandler.addInputConsumer(Logger.getInstance());
        OutputHandler.addOutputConsumer(Logger.getInstance());

        // Instantiate the simulator engine
        SimEngine s = new SimEngine();

        // Make the time keeping component of the simulator engine viewable to serverDiscoveryEvents
        ServerDiscoveryEvent.setSimTimeKeeper(s);

        // Name the simulator engine as an input consumer
        InputHandler.addInputConsumer(s);

        // Instantiate the gui SYNCHRONOUSLY on the event dispatching thread
        GUIStarter gs = new GUIStarter();
        try {
            SwingUtilities.invokeAndWait(gs);
        } catch (Exception e) {
            Utilities.showError("A fatal error was encountered while trying to load the GUI. Please file a bug report.");
            System.exit(1);
        }

        GUI g = gs.getGui();


        // Setup the node inpsector for the gui. This gives the gui a backdoor into the
        // simulation, where it can view node attributes
        g.setNodeInspector(s);

        // Name the GUI as an output consumer
        OutputHandler.addOutputConsumer(g);

        //sleep if the splash screen is up
        if (SplashScreen.getSplashScreen() != null) {
            try {
                System.gc();
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
        g.setVisible(true);

    }
}
