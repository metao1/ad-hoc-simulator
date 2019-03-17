package server.discovery.gui;

import server.discovery.Defaults;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;


public class ColorLegend extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel labelsGrid = new JPanel(new GridLayout(5, 1, 0, 0));
    private JPanel colorsGrid = new JPanel(new GridLayout(5, 1, 0, 0));

    public ColorLegend() {
        super();
        //Add the control message row
        addLegendItem("Control Data Message:", Defaults.CNTRLMSG_COLOR);
        addLegendItem("User Data Message:", Defaults.NARRMSG_COLOR);
        addLegendItem("Broadcast Message:", Defaults.BROADCAST_COLOR);
        addLegendItem("Selected Node Range:", Defaults.SELECTED_RANGE_COLOR);
        addLegendItem("Node Range:", Defaults.RANGE_COLOR);

        add(labelsGrid);
        add(colorsGrid);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Legend",
                TitledBorder.CENTER, TitledBorder.TOP, Defaults.BOLDFACED_FONT));
    }

    private void addLegendItem(String name, Color c) {
        JPanel colorPanel = new JPanel();
        JPanel colorContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        colorPanel.setBackground(c);
        colorPanel.setPreferredSize(new Dimension(20, 5));
        colorContainer.add(colorPanel);

        JLabel label = new JLabel(name);
        label.setFont(Defaults.BOLDFACED_FONT);
        labelsGrid.add(label);
        colorsGrid.add(colorContainer);
    }
}
