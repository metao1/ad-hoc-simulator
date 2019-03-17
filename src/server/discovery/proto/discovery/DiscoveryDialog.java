package server.discovery.proto.discovery;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

public class DiscoveryDialog extends JDialog {

    /**
     * Default Serial Version ID.
     */
    private static final long serialVersionUID = 1L;
    DefaultTableModel model = new DefaultTableModel();
    JTable nodeRouteTable = new JTable(model) {
        private static final long serialVersionUID = 1L;

        public boolean isCellEditable(
                int row, int column) {
            return false;
        }
    };
    private JLabel sourceNodeLabel = new JLabel("Source Node: ");
    private JLabel currentNodeTick = new JLabel("Clock Ticks: ");
    private JLabel nodeInfo = new JLabel("Route Table:");
    private JLabel sourceLabel;
    private JLabel TimeLabel;
    // Creating the border layout
    private BorderLayout Layout = new BorderLayout();
    private GridLayout nodeInfoLayout = new GridLayout(3, 2);
    private BorderLayout nodeRouteTableLayout = new BorderLayout();
    private BorderLayout nodeTimeLayout = new BorderLayout();
    private BorderLayout nodeSouceLayout = new BorderLayout();
    // creating panels
    private JPanel Panel = new JPanel(Layout);
    private JPanel nodeInfoPanel = new JPanel(nodeInfoLayout);
    private JPanel nodeSourcePanel = new JPanel(nodeSouceLayout);
    private JPanel nodeTimePanel = new JPanel(nodeTimeLayout);
    private JPanel routePanel = new JPanel(
            nodeRouteTableLayout);
    private JScrollPane scroller;

    public DiscoveryDialog(JFrame frame, String SourceId, int timeTick,
                           HashMap<String, RouteEntry> routeTable) {
        super(frame, "Node Attributes: " + SourceId);

        // Set the default dimension of the node attributes window
        this.setPreferredSize(new Dimension(320, 300));

        // Set the model columns
        model.addColumn("DEST");
        model.addColumn("# HOPS");
        model.addColumn("NXT HOP");
        model.addColumn("SEQ #");
        model.addColumn("INST TIME");

        sourceLabel = new JLabel(SourceId);

        // Set the time tick label options
        String timeTickString = "" + timeTick;
        TimeLabel = new JLabel(timeTickString);

        getContentPane().add(Panel);

        // Add the component to the message Panel
        routePanel.add(nodeInfo, BorderLayout.NORTH);
        routePanel.add(nodeRouteTable, BorderLayout.CENTER);

        // Add the text area to the scroller
        scroller = new JScrollPane(nodeRouteTable);

        // Add component to the node time information panel
        nodeTimePanel.add(TimeLabel, BorderLayout.CENTER);
        nodeTimePanel.add(currentNodeTick, BorderLayout.WEST);

        // Add component to the node time information panel
        nodeSourcePanel.add(sourceLabel, BorderLayout.CENTER);
        nodeSourcePanel.add(sourceNodeLabel, BorderLayout.WEST);

        // Adding to the Grid
        nodeInfoPanel.add(nodeSourcePanel);
        nodeInfoPanel.add(nodeTimePanel);
        nodeInfoPanel.add(nodeInfo);

        // Add all the individual panels to the main panel
        Panel.add(nodeInfoPanel, BorderLayout.NORTH);
        Panel.add(scroller, BorderLayout.CENTER);

        formatRouteTable(routeTable, timeTick);

        // Display the Panel
        this.pack();
        this.setModal(false);
        this.setAlwaysOnTop(true);
        this.setLocationRelativeTo(frame);
    }

    void updateInformation(int currentTick, HashMap<String, RouteEntry> routeTable) {
        formatRouteTable(routeTable, currentTick);
        String timeTick = "" + currentTick;
        TimeLabel.setText(timeTick);
    }

    private void formatRouteTable(HashMap<String, RouteEntry> routeTable,
                                  int currentTick) {
        Iterator<RouteEntry> iter = routeTable.values().iterator();
        RouteEntry entry;
        String destinationIP, hopCount, nextHop, sequenceNum, insttime;

        // Clean the table out to refresh the table
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        // Add all the rows back in
        while (iter.hasNext()) {
            entry = iter.next();

            destinationIP = entry.getDestIP();
            hopCount = "" + entry.getHopCount();
            nextHop = entry.getNextHopIP();
            sequenceNum = "" + entry.getSeqNum();
            insttime = "" + entry.getInstTime();

            model.addRow(new String[]{destinationIP, hopCount, nextHop,
                    sequenceNum, insttime});
        }
        return;
    }
}
