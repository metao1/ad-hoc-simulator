/**
 *
 */
package server.discovery.gui;

import server.discovery.InputHandler;
import server.discovery.Message;
import server.discovery.event.ServerDiscoveryEvent;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class SendNodeMessageDialog extends JDialog implements ActionListener {
    /**
     * @author Mehrdad Karami
     */
    private static final long serialVersionUID = 1L;

    // Components
    private JButton submitButton = new JButton("Submit");
    private JButton cancelButton = new JButton("Cancel");
    private JLabel sourcelabel = new JLabel("Source Node:");
    private JLabel destLabel = new JLabel("Destination Node:");
    private JLabel messageLabel = new JLabel("Message:");
    private JLabel nodeLabel;
    private JTextArea message = new JTextArea();
    private JComboBox nodeBox = new JComboBox();
    private String sourceNode;

    // Layouts for the Panels
    private BorderLayout Layout = new BorderLayout();
    private GridLayout nodeInfoLayout = new GridLayout(2, 2);
    private BorderLayout buttonLayout = new BorderLayout();
    private BorderLayout messageLayout = new BorderLayout();

    // Panels to organize components
    private JPanel Panel = new JPanel(Layout);
    private JPanel nodeInfoPanel = new JPanel(nodeInfoLayout);
    private JPanel buttonPanel = new JPanel(buttonLayout);
    private JPanel messagePanel = new JPanel(messageLayout);


    public SendNodeMessageDialog(JFrame frame, String SourceID, Vector<String> nodeList) {
        super(frame, "ServerDiscovery - Send a Message");
        this.setAlwaysOnTop(true);

        // Add each node in the ComboBox from the node list
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i) != SourceID) //excluding the source node from the dropdown list
            {
                nodeBox.addItem(nodeList.get(i));
            }
        }
        // Save the source id
        sourceNode = SourceID;
        nodeLabel = new JLabel(SourceID);

        getContentPane().add(Panel);

        // Set some parameters on the message box
        message.setRows(10);
        message.setColumns(20);
        message.setLineWrap(true);
        message.setAutoscrolls(true);
        message.setText("Sample Message");

        // Add our components to the GridLayout style panel
        nodeInfoPanel.add(sourcelabel);
        nodeInfoPanel.add(nodeLabel);
        nodeInfoPanel.add(destLabel);
        nodeInfoPanel.add(nodeBox);

        // Setup some borders
        Border raisedBevel, loweredBevel, compound;
        raisedBevel = BorderFactory.createRaisedBevelBorder();
        loweredBevel = BorderFactory.createLoweredBevelBorder();
        compound = BorderFactory.createCompoundBorder(raisedBevel, loweredBevel);

        // Give the border to the message box component
        message.setBorder(compound);

        // Add the component to the message Panel
        messagePanel.add(messageLabel, BorderLayout.NORTH);
        messagePanel.add(message, BorderLayout.CENTER);

        // Add the buttons to the buttons panel
        submitButton.setPreferredSize(new Dimension(85, 25));
        cancelButton.setPreferredSize(new Dimension(85, 25));
        buttonPanel.add(submitButton, BorderLayout.EAST);
        buttonPanel.add(cancelButton, BorderLayout.WEST);

        // Add all the individual panels to the main panel
        Panel.add(nodeInfoPanel, BorderLayout.NORTH);
        Panel.add(messagePanel, BorderLayout.CENTER);
        Panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners to the buttons
        submitButton.addActionListener(this);
        cancelButton.addActionListener(this);

        // Display the Panel
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (submitButton == e.getSource()) {
            // Get our source and destinations nodes
            String destination = (String) nodeBox.getSelectedItem();
            String source = sourceNode;
            String messageText = message.getText();

            // If they are equal warn the user and return
            if (source == destination) {
                JOptionPane.showMessageDialog(Panel, "Source and Destination cannot match.");
                return;
            }

            // If the message box is blank throw and error
            if (messageText.length() == 0) {
                JOptionPane.showMessageDialog(Panel, "Must specifiy a message.");
                return;
            }
            // If they are different construct the message and send the input signal
            Message mess = new Message(destination, source, messageText);
            InputHandler.dispatch(ServerDiscoveryEvent.inInsertMessage(mess));

            // Close the frame
            setVisible(false);
        } else if (cancelButton == e.getSource()) {
            // Close the frame
            setVisible(false);
        }
    }

}
