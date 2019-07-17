package com.metao.server.discovery.gui;


import com.metao.server.discovery.Defaults;
import com.metao.server.discovery.InputHandler;
import com.metao.server.discovery.OutputHandler;
import com.metao.server.discovery.Utilities;
import com.metao.server.discovery.event.ServerDiscoveryEvent;
import com.metao.server.discovery.event.ServerDiscoveryEvent.EventType;
import com.metao.server.discovery.logger.Logger;
import com.metao.server.discovery.logger.Parser;
import com.metao.server.discovery.proto.NodeFactory.NodeType;
import com.metao.server.discovery.replayer.Replayer;
import com.metao.server.discovery.replayer.Replayer.ReplayMode;
import com.metao.server.discovery.replayer.Replayer.ReplayerListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;

public class ServerDiscoveryAppMenu implements ReplayerListener, ComponentListener {
    private static JLabel currentQuantumLabel = new JLabel();
    //Creating the  bar and all of its elements
    private JMenuBar menuBar = new JMenuBar();
    private JMenu simMenu = new JMenu("Simulation");
    private JMenu newMenu = new JMenu("New");
    private JMenu createNetworkMenu = new JMenu("Create Network");
    private JMenu modeMenu = new JMenu("Mode");
    private JMenu controlMenu = new JMenu("Control");
    private JMenuItem saveMenuItem = new JMenuItem("Save Log");
    private JMenuItem saveScreenMenuItem = new JMenuItem("Take Screenshot");
    private JMenuItem clearNodesMenuItem = new JMenuItem("Delete All Nodes");
    private JMenuItem exitMenuItem = new JMenuItem("Exit");
    private JMenuItem importMenuItem = new JMenuItem("Import for Replay...");
    private JMenuItem playMenuItem = new JMenuItem("Play");
    private JMenuItem pauseMenuItem = new JMenuItem("Pause");
    private JMenuItem resumeMenuItem = new JMenuItem("Resume");
    private JMenuItem stopMenuItem = new JMenuItem("Stop");
    private JMenuItem addSingleNodeMenuItem = new JMenuItem("Add Single Node");
    private JMenuItem deleteNodeMenuItem = new JMenuItem("Delete Selected Node");
    private JMenuItem addMultipleNodesMenuItem = new JMenuItem("Add Multiple Nodes");
    private JMenuItem loadTopologyMenuItem = new JMenuItem("Load Topology from File...");
    private JLabel typeLabel = new JLabel("Simulation Type: ");
    private JLabel modeLabel = new JLabel("Mode: ");
    private JLabel engineStatusLabel = new JLabel("Engine Status: ");
    private JLabel simModeLabel = new JLabel();
    private JLabel simTypeLabel = new JLabel();
    private JLabel simEngineStatusLabel = new JLabel();
    private JPanel buttonArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private JButton playButton = new JButton();
    private JButton resumeButton = new JButton();
    private JButton pauseButton = new JButton();
    private JButton stopButton = new JButton();
    private JCheckBoxMenuItem debugCheckBox = new JCheckBoxMenuItem("Debug Enabled");
    private JCheckBoxMenuItem graphicsCheckBox = new JCheckBoxMenuItem("Graphics Enabled");
    private JPanel speedArea = new JPanel();
    private JPanel simTypeArea = new JPanel();
    private JPanel statusPanel = new JPanel();
    private JPanel controlPanel = new JPanel();
    private JPanel nodeAttributesPanel = new JPanel();
    // Labels slider bar for the speed adjustment
    private JLabel speedLabel = new JLabel("Simulation Speed");
    private JSlider slideBar = new JSlider(JSlider.HORIZONTAL, 1, 20, 10);
    private JPanel menuPanel = new JPanel();
    private SimArea simArea;
    private JPanel currentQuantumArea = new JPanel();
    private JProgressBar replayPBar = new JProgressBar();
    private GUI guiInstance;
    private Replayer replayer = null;

    // Node controls components
    private JButton AttributesButton;
    private JLabel NodeLabel = new JLabel("Node ID:");
    private JComboBox NodeComboBox;
    private JLabel XSpinnerLabel = new JLabel("X:");
    private JSpinner XSpinner;
    private JLabel YSpinnerLabel = new JLabel("Y:");
    private JSpinner YSpinner;
    private JLabel RangeSpinnerLabel = new JLabel("Range:");
    private JSpinner RangeSpinner;
    private JCheckBox PromiscuityCheckBox;
    private long quantums = 0;

    public ServerDiscoveryAppMenu(GUI g, NodeControls nodeControls) {

        guiInstance = g;

        //listen for component events so we can resize the menu panel later
        g.addComponentListener(this);

        // Set the preferred size of the simTypeLabel so it displays correctly on Linux.
        simTypeLabel.setPreferredSize(new Dimension(40, 10));

        //Make the menubar slightly darker.
        float[] rgba = menuBar.getBackground().getRGBComponents(null);
        for (int i = 0; i < 4; i++) {
            rgba[i] -= .08f;
            if (rgba[i] < 0f) rgba[i] = 0f;
        }
        menuBar.setBackground(new Color(rgba[0], rgba[1], rgba[2], rgba[3]));

        menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Add elements to the create network
        createNetworkMenu.add(addSingleNodeMenuItem);
        createNetworkMenu.add(addMultipleNodesMenuItem);
        createNetworkMenu.add(loadTopologyMenuItem);
        createNetworkMenu.addSeparator();
        createNetworkMenu.add(deleteNodeMenuItem);
        createNetworkMenu.add(clearNodesMenuItem);

        // Add elements to the mode
        debugCheckBox.setState(Defaults.DEBUG_ENABLED);
        modeMenu.add(debugCheckBox);
        modeMenu.add(graphicsCheckBox);

        // Add elements to the control
        controlMenu.add(playMenuItem);
        controlMenu.add(pauseMenuItem);
        controlMenu.add(resumeMenuItem);
        controlMenu.add(stopMenuItem);

        // Add elements to the sim  and their sub menus
        simMenu.add(newMenu);
        simMenu.add(createNetworkMenu);

        //Add node  items dynamically using reflection
        addNewSimMenuItems(newMenu);
        simMenu.addSeparator();
        simMenu.add(saveMenuItem);
        simMenu.add(saveScreenMenuItem);
        simMenu.addSeparator();
        graphicsCheckBox.setState(true);
        saveMenuItem.setEnabled(false);
        addMultipleNodesMenuItem.setEnabled(false);
        addSingleNodeMenuItem.setEnabled(false);
        deleteNodeMenuItem.setEnabled(false);
        clearNodesMenuItem.setEnabled(false);
        loadTopologyMenuItem.setEnabled(false);
        simMenu.add(importMenuItem);
        simMenu.add(exitMenuItem);

        menuBar.add(simMenu);
        menuBar.add(modeMenu);
        menuBar.add(controlMenu);

        // Add the simulation type  labels
        JPanel simModeArea = new JPanel();
        JPanel simEngineArea = new JPanel();

        simTypeArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        simModeArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        simEngineArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typeLabel.setFont(Defaults.BOLDFACED_FONT);
        modeLabel.setFont(Defaults.BOLDFACED_FONT);
        engineStatusLabel.setFont(Defaults.BOLDFACED_FONT);

        simModeArea.add(modeLabel);
        simModeArea.add(simModeLabel);
        simTypeArea.add(typeLabel);
        simTypeArea.add(simTypeLabel);
        simEngineArea.add(engineStatusLabel);
        simEngineArea.add(simEngineStatusLabel);

        JPanel statusSubPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(2, 1, 0, 5));
        statusSubPanel.setLayout(new GridLayout(2, 2, 0, 5));
        replayPBar.setVisible(false);
        replayPBar.setString("Replay Progress");
        replayPBar.setStringPainted(true);
        statusSubPanel.add(simTypeArea);
        statusSubPanel.add(simEngineArea);
        statusSubPanel.add(simModeArea);
        statusSubPanel.add(currentQuantumArea);

        statusPanel.add(statusSubPanel);
        statusPanel.add(replayPBar);
        statusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Status",
                TitledBorder.CENTER, TitledBorder.TOP, Defaults.BOLDFACED_FONT));

        // Add the Play, pause, and stop buttons
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

        customizeButton(stopButton, "stop.png", "hoverstop.png");
        customizeButton(playButton, "play.png", "hoverplay.png");
        customizeButton(pauseButton, "pause.png", "hoverpause.png");
        customizeButton(resumeButton, "pause.png", "hoverpause.png");

        buttonArea.add(stopButton);
        buttonArea.add(pauseButton);
        buttonArea.add(resumeButton);
        buttonArea.add(playButton);

        controlPanel.add(buttonArea);
        controlPanel.add(speedArea);

        controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Simulation Controls",
                TitledBorder.CENTER, TitledBorder.TOP, Defaults.BOLDFACED_FONT));

        AttributesButton = nodeControls.getNodeAttributesButton();
        NodeComboBox = nodeControls.getNodeComboBox();
        XSpinner = nodeControls.getXSpinner();
        YSpinner = nodeControls.getYSpinner();
        RangeSpinner = nodeControls.getRangeSpinner();
        PromiscuityCheckBox = nodeControls.getPromiscuityCheckBox();


        // Node Attributes panel layout
        nodeAttributesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Node Controls",
                TitledBorder.CENTER, TitledBorder.TOP, Defaults.BOLDFACED_FONT));

        nodeAttributesPanel.setLayout(new GridLayout(3, 2, 11, 5));

        JPanel XPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 0));
        JPanel YPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 0));
        JPanel RangePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JPanel NodePanel = new JPanel(new GridLayout(1, 2));

        NodeLabel.setFont(Defaults.BOLDFACED_FONT);
        NodePanel.add(NodeLabel);
        NodePanel.add(NodeComboBox);


        XSpinnerLabel.setFont(Defaults.BOLDFACED_FONT);
        XPanel.add(XSpinnerLabel);
        XPanel.add(XSpinner);


        YSpinnerLabel.setFont(Defaults.BOLDFACED_FONT);
        YPanel.setFont(Defaults.BOLDFACED_FONT);
        YPanel.add(YSpinnerLabel);
        YPanel.add(YSpinner);

        RangeSpinnerLabel.setFont(Defaults.BOLDFACED_FONT);
        RangePanel.add(RangeSpinnerLabel);
        RangePanel.add(RangeSpinner);

        nodeAttributesPanel.add(NodePanel);
        nodeAttributesPanel.add(XPanel);
        AttributesButton.setFont(Defaults.BOLDFACED_FONT);
        nodeAttributesPanel.add(AttributesButton);
        nodeAttributesPanel.add(YPanel);
        PromiscuityCheckBox.setFont(Defaults.BOLDFACED_FONT);
        nodeAttributesPanel.add(PromiscuityCheckBox);
        nodeAttributesPanel.add(RangePanel);

        menuPanel.add(statusPanel);
        menuPanel.add(controlPanel);
        menuPanel.add(nodeAttributesPanel);
        //menuPanel.add(Logo);
        resumeButton.setVisible(false);
        resumeMenuItem.setVisible(false);
        playButton.setEnabled(false);
        playMenuItem.setEnabled(false);
        stopButton.setEnabled(false);
        stopMenuItem.setEnabled(false);
        pauseButton.setEnabled(false);
        pauseMenuItem.setEnabled(false);

        // Add the quantums elapsed area
        currentQuantumArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel qLabel = new JLabel("Current Quantum: ");
        qLabel.setFont(Defaults.BOLDFACED_FONT);
        currentQuantumArea.add(qLabel);

        currentQuantumArea.add(currentQuantumLabel);

        // Add the slider bar, set its properties and values.
        JPanel sliderArea = new JPanel();
        sliderArea.add(slideBar);
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        speedLabel.setFont(Defaults.BOLDFACED_FONT);
        subPanel.add(speedLabel);
        subPanel.add(sliderArea);

        speedArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        speedArea.add(subPanel);

        slideBar.setSnapToTicks(true);
        slideBar.setPaintTicks(true);
        slideBar.setMinorTickSpacing(1);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(1), new JLabel("Slower"));
        labelTable.put(new Integer(20), new JLabel("Faster"));
        slideBar.setLabelTable(labelTable);
        slideBar.setPaintLabels(true);
        slideBar.setPreferredSize(new Dimension(150, slideBar.getPreferredSize().height));
        menuPanel.setOpaque(false);
        menuPanel.setVisible(true);

        // Setup a few simple keyboard shortcuts
        // These respond when ctrl is held
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        debugCheckBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));

        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        graphicsCheckBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        clearNodesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        addSingleNodeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

        deleteNodeMenuItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));

        // Responds if alt is held
        playMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        pauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        resumeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));

        clearNodesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inClearSim());
            }
        });

        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utilities.runSaveLogDialog(menuBar.getParent());
            }
        });


        saveScreenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utilities.captureScreen(simArea.getParent());
            }
        });

        debugCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (!debugCheckBox.getState()) {
                    OutputHandler.addFilteredEvent(EventType.OUT_DEBUG);
                } else {
                    OutputHandler.removeFilteredEvent(EventType.OUT_DEBUG);
                }
            }
        });

        graphicsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                simArea.setGraphicsEnabled(graphicsCheckBox.getState());
            }
        });

        addSingleNodeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inAddNode(Defaults.X, Defaults.Y, Defaults.RANGE, Defaults.IS_PROMISCUOUS));
            }
        });

        deleteNodeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedNode = simArea.getSelectedNodeID();
                if (selectedNode == null) {
                    return;
                }
                InputHandler.dispatch(ServerDiscoveryEvent.inDeleteNode(selectedNode));
            }
        });

        loadTopologyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Log Files", "log");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(menuBar.getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String name = chooser.getSelectedFile().getPath();

                    //Parse the setup events into memory
                    Queue<ServerDiscoveryEvent> Q = Parser.parseSetup(name);

                    if (Q == null) {
                        Utilities.showError("Log file can not be parsed.");
                        return;
                    }

                    //Dispatch every event in the Q
                    for (ServerDiscoveryEvent d : Q) {
                        InputHandler.dispatch(d);
                    }
                }
            }
        });

        importMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Log Files", "log");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(menuBar.getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    String name = chooser.getSelectedFile().getPath();

                    //Parse the replay events into memory
                    Queue<ServerDiscoveryEvent> Q = Parser.parseReplay(name);

                    if (Q == null) {
                        Utilities.showError("Log file can not be parsed.");
                        return;
                    }

                    //Okay. New simulation. Have to ask the user what type of sim they want..
                    NodeType nt = Utilities.popupAskNodeType();
                    if (nt == null) {
                        //User canceled..
                        return;
                    }

                    //Start a new simualation
                    InputHandler.dispatch(ServerDiscoveryEvent.inNewSim(nt));

                    //Ask the user what mode of replay they want
                    ReplayMode mode = Replayer.askReplayMode();
                    if (mode == null) {
                        return;
                    }

                    //Instantiate a new replayer with the replay events
                    //Name the gui as the replayerListener.
                    replayer = new Replayer(Q, (Replayer.ReplayerListener) guiInstance.getReplayerListener(), mode);

                }
            }
        });

        addMultipleNodesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Random r = new Random();
                double X = simArea.maxNodePoint().x;
                double Y = simArea.maxNodePoint().y;
                int numberOfNodes = 0;
                String input = JOptionPane.showInputDialog(null, "How many nodes would you like to add?");

                // If they hit cancel return.
                if (input == null) {
                    return;
                }

                try {
                    numberOfNodes = Integer.parseInt(input);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalide Entry, Numeric Only.");
                    return;
                }

                for (int i = 1; i <= numberOfNodes; i++) {
                    int range = r.nextInt(400) + 50; // Min range of 50
                    int x = r.nextInt((int) X);
                    int y = r.nextInt((int) Y);
                    InputHandler.dispatch(ServerDiscoveryEvent.inAddNode(x, y, range, Defaults.IS_PROMISCUOUS));
                }
            }

        });

        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Before we exit make sure to clean up the temporary log file.
                Logger.deleteLogFile();
                System.exit(0);
            }
        });

        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inStartSim(getSlideBarSpeed()));
            }
        });

        playMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inStartSim(getSlideBarSpeed()));
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inPauseSim());
            }
        });

        pauseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inPauseSim());
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inStopSim());
            }
        });

        stopMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inStopSim());
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inResumeSim());
            }
        });

        resumeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InputHandler.dispatch(ServerDiscoveryEvent.inResumeSim());
            }
        });

        slideBar.addChangeListener(new ChangeListener() {
            private int lastVal = 0;

            public void stateChanged(ChangeEvent arg0) {
                int val = getSlideBarSpeed();
                if (lastVal == val) {
                    return;
                }
                lastVal = val;
                InputHandler.dispatch(ServerDiscoveryEvent.inSimSpeed(val));
            }
        });
    }

    private int getSlideBarSpeed() {
        //Interpret the slidebar speed
        return slideBar.getMaximum() - slideBar.getValue() + 1;
    }

    private void addNewSimMenuItems(JMenu parentMenu) {
        //get sim types
        NodeType[] nTypes = Utilities.getNodeTypes();

        for (NodeType nt : nTypes) {
            //Make a new  item
            JMenuItem simTypeMenuItem = new JMenuItem(nt.toString());

            //Add the event handler
            simTypeMenuItem.addActionListener(new NewSimClickHandler(nt));

            //Add it to the parent
            parentMenu.add(simTypeMenuItem);
        }
    }

    public void simStarted() {
        //Disable the play button, enable the pause/stop buttons
        playButton.setEnabled(false);
        playMenuItem.setEnabled(false);
        pauseButton.setEnabled(true);
        pauseMenuItem.setEnabled(true);
        stopButton.setEnabled(true);
        stopMenuItem.setEnabled(true);

        //Do not let user save while replay is running.
        saveMenuItem.setEnabled(false);

        //Update the engine label
        simEngineStatusLabel.setText("Running");
    }

    public void newSim(NodeType nodeType) {
        //Disable/enable  items
        newMenu.setEnabled(false);
        importMenuItem.setEnabled(false);
        addMultipleNodesMenuItem.setEnabled(true);
        addSingleNodeMenuItem.setEnabled(true);
        deleteNodeMenuItem.setEnabled(true);
        clearNodesMenuItem.setEnabled(true);
        loadTopologyMenuItem.setEnabled(true);
        saveMenuItem.setEnabled(true);

        //Enable the Play button, disable tstop and pause
        stopButton.setEnabled(false);
        stopMenuItem.setEnabled(false);
        playButton.setEnabled(true);
        playMenuItem.setEnabled(true);
        pauseButton.setEnabled(false);
        pauseMenuItem.setEnabled(false);

        //Zero out the current quantum
        quantums = 0;
        currentQuantumLabel.setText(Long.toString(quantums));

        //Show the new sim label
        simTypeLabel.setText(nodeType.toString());

        //Show the sim mode; Normal by default.
        simModeLabel.setText("Interactive");

        //Show engine status; Stopped by default
        simEngineStatusLabel.setText("Stopped");

        //Make sure the replaybar is hidden
        replayPBar.setVisible(false);
    }

    public void simStopped() {
        stopButton.setEnabled(false);
        stopMenuItem.setEnabled(false);
        playButton.setEnabled(false);
        playMenuItem.setEnabled(false);
        pauseButton.setEnabled(false);
        pauseMenuItem.setEnabled(false);

        //Enable/disable  items
        newMenu.setEnabled(true);
        importMenuItem.setEnabled(true);
        addMultipleNodesMenuItem.setEnabled(false);
        addSingleNodeMenuItem.setEnabled(false);
        deleteNodeMenuItem.setEnabled(false);
        clearNodesMenuItem.setEnabled(false);
        loadTopologyMenuItem.setEnabled(false);

        saveMenuItem.setEnabled(true);

        //If the replayer is running, abort it.
        if (replayer != null && replayer.isRunning()) {
            replayer.abort();
        }

        //Update the engine label
        simEngineStatusLabel.setText("Stopped");

    }

    public void simPaused() {
        playButton.setEnabled(false);
        playMenuItem.setEnabled(false);
        stopButton.setEnabled(false);
        stopMenuItem.setEnabled(false);
        pauseButton.setVisible(false);
        pauseMenuItem.setVisible(false);
        resumeButton.setVisible(true);
        resumeMenuItem.setVisible(true);

        saveMenuItem.setEnabled(true);

        //Update the engine label
        simEngineStatusLabel.setText("Paused");
    }

    public void simResumed() {
        stopButton.setEnabled(true);
        stopMenuItem.setEnabled(true);
        pauseButton.setVisible(true);
        pauseMenuItem.setVisible(true);
        resumeButton.setVisible(false);
        resumeMenuItem.setVisible(false);

        //Do not let user save while simulation is running.
        saveMenuItem.setEnabled(false);

        //Update the engine label
        simEngineStatusLabel.setText("Running");
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public JPanel getActionPanel() {
        return menuPanel;
    }


    public void setSimArea(SimArea simArea) {
        this.simArea = simArea;
    }

    public void quantumElapsed() {
        quantums += 1;
        currentQuantumLabel.setText(Long.toString(quantums));
        if (replayPBar.isVisible()) {
            replayPBar.setValue((int) quantums);
        }

    }

    @Override
    public void replayerStarted(Queue<ServerDiscoveryEvent> Q, Replayer instance) {
        if (Q.size() == 0) return;

        replayPBar.setVisible(true);

        //Get the last event of the replay. Use it to determine the upper bound for the progress bar.
        ServerDiscoveryEvent e = null;
        Iterator<ServerDiscoveryEvent> i = Q.iterator();
        while (i.hasNext()) {
            e = i.next();
        }

        replayPBar.setMaximum((int) e.currentQuantum - 1);

        //Set the mode
        if (instance.getMode() == ReplayMode.LOCKED) {
            simModeLabel.setText("Locked Replay");
        } else {
            simModeLabel.setText("Interactive Replay");
        }

    }

    public void setSimModeLabel(String modeText) {
        simModeLabel.setText(modeText);
    }

    @Override
    public void replayerFinished(boolean aborted) {
        //hide the replay progress bar
        replayPBar.setVisible(false);
        replayPBar.setValue(0);

    }

    public void setLockedReplayMode(boolean b) {
        createNetworkMenu.setEnabled(!b);
    }

    @Override
    public void componentHidden(ComponentEvent arg0) {
        resizeMenuPanel();
    }

    @Override
    public void componentMoved(ComponentEvent arg0) {
        resizeMenuPanel();
    }

    @Override
    public void componentResized(ComponentEvent arg0) {
        resizeMenuPanel();
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
        resizeMenuPanel();
    }

    private void resizeMenuPanel() {
        //Set all of the component heights to the maximum component height
        int maxH = Math.max(statusPanel.getPreferredSize().height, controlPanel.getPreferredSize().height);
        maxH = Math.max(maxH, nodeAttributesPanel.getPreferredSize().height);

        statusPanel.setPreferredSize(new Dimension(350, maxH));
        controlPanel.setPreferredSize(new Dimension(controlPanel.getPreferredSize().width, maxH));
        nodeAttributesPanel.setPreferredSize(new Dimension(nodeAttributesPanel.getPreferredSize().width, maxH));
        menuPanel.invalidate();

    }

    private void customizeButton(JButton b, String ImageName, String RolloverImage) {
        ImageIcon Icon = null;
        ImageIcon hoverIcon = null;
        try {
            Icon = new ImageIcon(getClass().getResource("/" + ImageName));
            hoverIcon = new ImageIcon(getClass().getResource("/" + RolloverImage));
            b.setIcon(Icon);
            b.setRolloverIcon(hoverIcon);
            Color transparent = new Color(0, 0, 0, 0);
            b.setOpaque(false);
            b.setBackground(transparent);
            b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            b.setRolloverEnabled(true);
            b.setFocusable(false);
        } catch (Exception e) {
            // Rather than throw an error, if the image can't be loaded just set the
            // button to a default button with the name of the image in all caps
            // minus the .png.
            b.setText(ImageName.substring(0, ImageName.length() - 4).toUpperCase());
        }
    }

    class NewSimClickHandler implements ActionListener {

        private NodeType nodeType;

        NewSimClickHandler(NodeType nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //Dispatch the new sim event
            InputHandler.dispatch(ServerDiscoveryEvent.inNewSim(nodeType));
        }
    }
}

