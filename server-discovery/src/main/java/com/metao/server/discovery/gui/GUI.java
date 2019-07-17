package com.metao.server.discovery.gui;

import com.metao.server.discovery.*;
import com.metao.server.discovery.event.ServerDiscoveryEvent;
import com.metao.server.discovery.replayer.Replayer;
import com.metao.server.discovery.replayer.Replayer.ReplayMode;
import com.metao.server.discovery.replayer.Replayer.ReplayerListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Queue;

public class GUI extends JFrame implements OutputConsumer {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final ThreadSafeReplayerListener threadSafeReplayerListener = new ThreadSafeReplayerListener();
    private JPanel simPanel = new JPanel();
    private JPanel logPanel = new JPanel();
    private JPanel bottomRightPanel = new JPanel();
    private LogArea logArea = new LogArea();
    private NodeAttributesArea nodeAttributesArea = new NodeAttributesArea();
    private ColorLegend colorLegend = new ColorLegend();
    private SimArea simArea = new SimArea();
    private ServerDiscoveryAppMenu menuArea;

    public GUI() {
        super(Defaults.TITLE_STRING);

        // Close the program when the frame is exited
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        //Initialize images
        ImageFactory.checkInit();

        //Add a listener for resize events
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                setSizes();
            }
        });


        // Setup a new layout for the outermost part of the frame. We'll use the
        // border layout.
        this.setLayout(new BorderLayout());


        // Allocate as follows:
        /*
         * _________________ | | | | | | | CENTER | <-|-- EAST | | | | | |
         * |_____________|___|
         */
        // Add a center panel, this will serve us merely in a layout capacity.
        JPanel subpanel = new JPanel();

        //attach the menu area.
        attachMenus();

        this.add(subpanel, BorderLayout.CENTER);

        // Add the east panel.
        bottomRightPanel.setLayout(new BorderLayout());
        bottomRightPanel.add(colorLegend, BorderLayout.CENTER);


        /*
         * Elaborate upon the layout of the subpanel. Do this: ______________ | | |
         * | | CENTER | |_____________| | | |__SOUTH______|
         */
        // Use another borderlayout for the subpanel.
        subpanel.setLayout(new BorderLayout());

        // Add the GuiCanvas to the Center part
        simPanel.setLayout(new BorderLayout());
        simPanel.add(simArea, BorderLayout.CENTER);
        subpanel.add(simPanel, BorderLayout.CENTER);

        // Add the Status log panel to the bottom part.
        logPanel.setLayout(new BorderLayout());
        logPanel.add(logArea, BorderLayout.CENTER);
        logPanel.add(bottomRightPanel, BorderLayout.EAST);
        subpanel.add(logPanel, BorderLayout.SOUTH);

        // initialize communication paths between the gui objects
        coupleComponents();

        // setup the borders
        setBorders();

        // pack everything
        pack();

    }

    //This method is called when the window is "realized" onto the screen. Perfect time
    //to setup the relative sizes of components.
    @Override
    public void addNotify() {
        super.addNotify();
        setSizes();
    }

    private void setSizes() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        Rectangle r = graphicsEnvironment.getMaximumWindowBounds();
        setMaximizedBounds(r);
        Dimension windowSize = new Dimension(r.width, r.height);

        this.setPreferredSize(windowSize);
        logPanel.setPreferredSize(new Dimension(logPanel.getPreferredSize().width, 210));
        simPanel.setPreferredSize(new Dimension((int) (windowSize.width),
                (int) (windowSize.height * .8)));
    }

    private void setBorders() {
        Border raisedBevel, loweredBevel, compound;
        raisedBevel = BorderFactory.createRaisedBevelBorder();
        loweredBevel = BorderFactory.createLoweredBevelBorder();
        compound = BorderFactory.createCompoundBorder(raisedBevel, loweredBevel);
        simPanel.setBorder(compound);

    }

    private void coupleComponents() {

        // The node attributes panel needs to listen for node changes (mouse overs,
        // selects, etc)
        simArea.addNodeListener(nodeAttributesArea);

        // The node attributes panel and serverDiscoveryAppMenu needs be able to augment the sim area
        // (selecting a new node, etc)
        nodeAttributesArea.setSimArea(simArea);
        menuArea.setSimArea(simArea);
        simArea.setNodeAttributesArea(nodeAttributesArea);
    }

    public void setNodeInspector(NodeInspector ni) {
        // Give it to the nodeAttributesArea instance
        nodeAttributesArea.setNodeInspector(ni);

    }

    public void consumeOutput(ServerDiscoveryEvent e) {
        // schedule the event to be processed later so as to not disturb the gui's
        // event thread
        ThreadSafeConsumer c = new ThreadSafeConsumer();

        // Copy the event to the thread safe consumer instance
        c.e = e;

        // Invoke it later; This will push the runnable instance onto the
        // Java Event Dispatching thread
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(c);
        } else {
            c.run();
        }
    }

    private void attachMenus() {
        //Insantiate the menu area
        menuArea = new ServerDiscoveryAppMenu(this, (NodeControls) nodeAttributesArea);
        add(menuArea.getActionPanel(), BorderLayout.NORTH);
        this.setJMenuBar(menuArea.getMenuBar());
    }

    public ThreadSafeReplayerListener getReplayerListener() {
        return threadSafeReplayerListener;
    }

    private class ThreadSafeConsumer implements Runnable {
        public ServerDiscoveryEvent e;

        public void run() {
            switch (e.eventType) {
                case OUT_ADD_NODE:
                    // Add the node
                    simArea.addNewNode(e.nodeX, e.nodeY, e.nodeRange, e.nodeId);
                    nodeAttributesArea.nodeAdded(e.nodeId);

                    //select the node
                    simArea.selectNode(e.nodeId);
                    nodeAttributesArea.setNodeById(e.nodeId);
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_MOVE_NODE:
                    //Move the node
                    simArea.moveNode(e.nodeId, e.nodeX, e.nodeY);

                    //select the node
                    simArea.selectNode(e.nodeId);
                    nodeAttributesArea.setNodeById(e.nodeId);

                    //show the event in the visual log
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_SET_NODE_RANGE:
                    // Refresh the node attributes panel
                    nodeAttributesArea.setNodeById(e.nodeId);
                    simArea.setNodeRange(e.nodeId, e.nodeRange);
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_SET_NODE_PROMISCUITY:
                    // Refresh the node attributes panel
                    nodeAttributesArea.setNodeById(e.nodeId);
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_MSG_RECEIVED:
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    JOptionPane.showMessageDialog(null, "Successful Message Transmission!\n" +
                            "Source Node: " + e.sourceId + "\n" +
                            "Destination Node: " + e.destinationId + "\n" +
                            "Message: " + e.transmittedMessage);
                    break;

                case OUT_INSERT_MESSAGE:
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;


                case OUT_NARRMSG_RECEIVED:
                    // Animate the event
                    simArea.traceMessage(e.sourceId, e.destinationId, Defaults.NARRMSG_COLOR, 5, Defaults.NARRMSG_THICKNESS, 1);
                    logArea.appendLog("NODE INFO", e.informationalMessage, e.currentQuantum);
                    break;


                case OUT_NARRMSG_TRANSMITTED:
                case OUT_CONTROLMSG_TRANSMITTED:
                    //If the destination is BROADCAST, animate it.
                    if (e.destinationId.equals(Message.BCAST_STRING)) {
                        simArea.nodeBroadcast(e.sourceId);
                        logArea.appendLog("NODE INFO", e.informationalMessage, e.currentQuantum);
                    }
                    break;


                case OUT_CONTROLMSG_RECEIVED:
                    // Animate the event
                    simArea.traceMessage(e.sourceId, e.destinationId, Defaults.CNTRLMSG_COLOR, 1, Defaults.CNTRLMSG_THICKNESS, 0);
                    logArea.appendLog("NODE INFO", e.informationalMessage, e.currentQuantum);
                    break;


                case OUT_DEL_NODE:
                    // Remove the node
                    simArea.deleteNode(e.nodeId);
                    nodeAttributesArea.nodeDeleted(e.nodeId);
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_DEBUG:
                    logArea.appendLog("SIM DEBUG", e.informationalMessage, e.currentQuantum);
                    break;
                case OUT_ERROR:
                    logArea.appendLog("SIM ERROR", e.informationalMessage, e.currentQuantum);
                    break;
                case OUT_NODE_INFO:
                    logArea.appendLog("NODE INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_QUANTUM_ELAPSED:
                    menuArea.quantumElapsed();
                    nodeAttributesArea.updateNodeDialogs();
                    break;

                case OUT_SIM_SPEED:
                    simArea.setSimSpeed(e.newSimSpeed);
                    break;

                case OUT_START_SIM:
                    //Notify the menu that a sim has started
                    menuArea.simStarted();
                    //Tell the simarea what the simulation speed is
                    simArea.setSimSpeed(e.newSimSpeed);
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_STOP_SIM:
                    //Notify the menu that the sim has stopped
                    menuArea.simStopped();
                    simArea.simStopped();
                    nodeAttributesArea.simStopped();
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);

                    //Prompt the user to save the log
                    int ret = JOptionPane.showConfirmDialog(null,
                            "Simulation Completed. Would you like to save the log file?",
                            "Simulation Completed.", JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        Utilities.runSaveLogDialog(simArea);
                    }

                    break;

                case OUT_PAUSE_SIM:
                    //Notify the menu the the sim has paused
                    menuArea.simPaused();
                    simArea.simPaused();
                    nodeAttributesArea.simPaused();
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_RESUME_SIM:
                    //Notify the menu that the sim has resumed
                    menuArea.simResumed();
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_CLEAR_SIM:
                    //Clear the sim area.
                    nodeAttributesArea.clear();
                    simArea.clear();
                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);
                    break;

                case OUT_NEW_SIM:
                    //Clear the sim area.
                    simArea.clear();

                    //Clear the node attributes area.
                    nodeAttributesArea.clear();

                    //clear the console
                    logArea.clear();

                    //Unlock the sim area
                    simArea.setLocked(false);

                    //Let the menu area know that a new sim has been created
                    menuArea.newSim(e.nodeType);

                    logArea.appendLog("SIM INFO", e.informationalMessage, e.currentQuantum);

            }
        }
    }

    public class ThreadSafeReplayerListener implements ReplayerListener {
        @Override
        public void replayerStarted(Queue<ServerDiscoveryEvent> Q, Replayer instance) {
            ReplayerStartedActionHandler action = new ReplayerStartedActionHandler(Q, instance);
            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    SwingUtilities.invokeAndWait(action);
                } catch (Exception e) {
                    Utilities.showError("An error occurred while starting the replayer. Please file a bug report.");
                    System.exit(1);
                }
            } else {
                action.run();
            }
        }

        @Override
        public void replayerFinished(boolean aborted) {
            ReplayerFinishedActionHandler action = new ReplayerFinishedActionHandler(aborted);
            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    SwingUtilities.invokeAndWait(action);
                } catch (Exception e) {
                    Utilities.showError("An error occurred while starting the replayer. Please file a bug report.");
                    System.exit(1);
                }
            } else {
                action.run();
            }

            if (action.shouldStopSimulation) {
                InputHandler.dispatch(ServerDiscoveryEvent.inStopSim());
            }
        }

        class ReplayerStartedActionHandler implements Runnable {

            private final Replayer instance;
            private final Queue<ServerDiscoveryEvent> Q;

            ReplayerStartedActionHandler(Queue<ServerDiscoveryEvent> Q, Replayer instance) {
                this.Q = Q;
                this.instance = instance;
            }

            @Override
            public void run() {
                //Send the signal to the menuArea
                menuArea.replayerStarted(Q, instance);

                //SetLockedReplayMode on all components
                if (instance.getMode() == ReplayMode.LOCKED) {
                    simArea.setLockedReplayMode(true);
                    menuArea.setLockedReplayMode(true);
                    nodeAttributesArea.setLockedReplayMode(true);
                }

                JOptionPane.showMessageDialog(simArea, "The replay has been sucessfully loaded. \n" +
                        "Please select \"Play\" from the menu bar to begin.");
            }
        }

        class ReplayerFinishedActionHandler implements Runnable {

            private final boolean aborted;
            private boolean shouldStopSimulation = false;

            ReplayerFinishedActionHandler(boolean aborted) {
                this.aborted = aborted;
            }

            @Override
            public void run() {

                //Unset LockedReplayMode on all components
                simArea.setLockedReplayMode(false);
                menuArea.setLockedReplayMode(false);
                nodeAttributesArea.setLockedReplayMode(false);


                if (!aborted) {
                    //Prompt the user to end the simulation
                    int ret = JOptionPane.showConfirmDialog(null,
                            "Replay finished. Would you like to continue running the simulation?",
                            "Replay finished.", JOptionPane.YES_NO_OPTION);


                    if (ret != JOptionPane.YES_OPTION) {
                        shouldStopSimulation = true;
                        return;
                    }
                }


                //Send the signal to the menuArea
                menuArea.replayerFinished(aborted);

                menuArea.setSimModeLabel("Normal");
            }
        }
    }

}
 

