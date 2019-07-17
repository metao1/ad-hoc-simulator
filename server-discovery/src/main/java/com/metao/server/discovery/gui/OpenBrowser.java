package com.metao.server.discovery.gui;

import javax.swing.*;

public class OpenBrowser {
    public OpenBrowser(String URL) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(null, "Desktop is not supported (fatal)");
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            JOptionPane.showMessageDialog(null,
                    "Desktop doesn't support the browse action (fatal)");
            return;
        }

        try {
            java.net.URI uri = new java.net.URI(URL);
            desktop.browse(uri);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
