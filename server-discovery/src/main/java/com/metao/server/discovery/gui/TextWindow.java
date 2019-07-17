/**
 * @author Mehrdad Karami
 */
package com.metao.server.discovery.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class TextWindow extends JDialog {
    private static final long serialVersionUID = 1L;

    // Creating the border layout
    private BorderLayout Layout = new BorderLayout();
    private JPanel Panel = new JPanel(Layout);
    private JScrollPane scroller;
    private JTextArea text = new JTextArea();

    public TextWindow(String filename) {

        InputStream stream = TextWindow.class.getResourceAsStream(filename);

        try {
            text.append(convertStreamToString(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reset the view to the beginning of the file
        text.setCaretPosition(0);
        text.setEditable(false);
        this.setPreferredSize(new Dimension(800, 600));
        getContentPane().add(Panel);

        // Add the text area to the scroller
        scroller = new JScrollPane(text);
        Panel.add(scroller, BorderLayout.CENTER);

        // Set the default dimension of the node attributes window
        this.pack();
        this.setModal(true);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }

    private String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means there's
         * no more data to read. We use the StringWriter class to produce the
         * string.
         */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}
