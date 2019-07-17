package com.metao.server.discovery;

import com.metao.server.discovery.gui.GUI;

class GUIStarter implements Runnable {
    private GUI g;

    public GUI getGui() {
        return g;
    }

    public void run() {
        g = new GUI();
    }
}
