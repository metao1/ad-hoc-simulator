package com.metao.server.discovery;

import javax.swing.plaf.FontUIResource;
import java.awt.*;

public class Defaults {
    public static final boolean DEBUG_ENABLED = false;
    public static final int X = 100;
    public static final int Y = 100;
    public static final boolean IS_PROMISCUOUS = false;
    public static final int RANGE = 350;
    public static final FontUIResource FONT = new FontUIResource(new Font("tahoma", Font.PLAIN, 12));
    public static final FontUIResource BOLDFACED_FONT = new FontUIResource(new Font("tahoma", Font.BOLD, 12));
    public static final Color NARRMSG_COLOR = new Color(0, 0, 0);
    public static final Color CNTRLMSG_COLOR = new Color(100, 100, 255);
    public static final Color BROADCAST_COLOR = new Color(100, 255, 100);
    public static final Color RANGE_COLOR = new Color(150, 150, 150);
    public static final Color SELECTED_RANGE_COLOR = new Color(255, 153, 51);
    public static final Font NODEID_FONT = new Font("tahoma", Font.BOLD, 12);
    public static final int SELECTED_NODE_RANGE_INDICATOR_THICKNESS = 3;
    public static final int CNTRLMSG_THICKNESS = 1;
    public static final int NARRMSG_THICKNESS = 4;
    public static final int BROADCAST_THICKNESS = 2;
    public static final String TITLE_STRING = "Auto Discovery Server - Simulator";
    public static final int LOG_AREA_BUF_SIZE = 131072;
    public static final int MAXFPS = 50;

    public static final int MINFPS = 3;

    //Animation speeds. Total time for one cycle of animation. Smaller is faster.
    public static final int BROADCAST_ANISPEED_MILLISECONDS = 700;
    public static final int MESSAGE_ANISPEED_MILLISECONDS = 1000;
}
