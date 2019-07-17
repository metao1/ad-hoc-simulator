package com.metao.server.discovery.gui;

import javax.swing.*;

public interface NodeControls {
    public JSpinner getXSpinner();

    public JSpinner getYSpinner();

    public JSpinner getRangeSpinner();

    public JButton getNodeAttributesButton();

    public JCheckBox getPromiscuityCheckBox();

    public JComboBox getNodeComboBox();
}
