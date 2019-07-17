package com.metao.server.discovery.gui;

import com.metao.server.discovery.Defaults;
import com.metao.server.discovery.InputHandler;
import com.metao.server.discovery.NodeInspector;
import com.metao.server.discovery.event.ServerDiscoveryEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class SimArea extends JLayeredPane {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static Color LOCKBOX_COLOR = new Color(0, 0, 0, 20);
    private boolean locked = true;
    private NodeAttributesArea nodeAttributesArea;
    private NodeInspector nodeInspector;
    private ArrayList<GNodeListener> nodeListeners = new ArrayList<GNodeListener>();
    private boolean graphicsEnabled = true;
    private Animations animations = new Animations(this);
    private TreeMap<String, GNode> gnodemap = new TreeMap<String, GNode>();
    private LockBox lockBox;
    private boolean lockedReplayMode = false;

    // /////////////////////////////Constructor
    public SimArea() {
        setLayout(null);
        XYTickPanel xyticks = new XYTickPanel(this);
        add(xyticks, JLayeredPane.DEFAULT_LAYER);
        moveToBack(xyticks);
        setLocked(true);
        addMouseListener(new PopClickListener());
        setVisible(true);

        animations.start();
    }

    public NodeInspector getNodeInspector() {
        return nodeInspector;
    }

    public void setNodeInspector(NodeInspector nodeInspector) {
        this.nodeInspector = nodeInspector;
    }

    public Point getBoundedNodePoint(Point unboundPoint) {
        // enforce an X Y boundrary
        int x, y;

        x = unboundPoint.x;
        y = unboundPoint.y;

        Point maxPoint = maxNodePoint();
        Point minPoint = minNodePoint();
        x = Math.min(x, maxPoint.x);
        x = Math.max(x, minPoint.x);
        y = Math.min(y, maxPoint.y);
        y = Math.max(y, minPoint.y);

        return new Point(x, y);
    }

    public Point minNodePoint() {
        return new Point(0, 0);
    }

    public Point maxNodePoint() {
        return new Point(getWidth() - ImageFactory.getNodeImg().getWidth(),
                getHeight() - ImageFactory.getNodeImg().getHeight());
    }

    public String getSelectedNodeID() {
        if (GNode.SelectedNode != null) {
            return GNode.SelectedNode.getId();
        } else
            return null;
    }

    public boolean setSelectedNode(String id) {
        // Get the node by id
        GNode n = getGNode(id);

        // If n is null, node not found. return false.
        if (n == null) {
            return false;
        }

        // Otherwise, select the node, return true.
        n.select();
        return true;
    }

    public void addNodeListener(GNodeListener gl) {
        nodeListeners.add(gl);
    }

    public void removeNodeSelectedListener(GNodeListener gl) {
        nodeListeners.remove(gl);
    }

    private GNode getGNode(String id) {
        // return a null reference if we don't find it
        if (!gnodemap.containsKey(id)) {
            return null;
        }

        return gnodemap.get(id);
    }

    private void addNewNodeReq(int x, int y) {
        InputHandler.dispatch(ServerDiscoveryEvent.inAddNode(x, y, Defaults.RANGE, Defaults.IS_PROMISCUOUS));
    }

    private void deleteNodeReq(String id) {
        // Dispatch
        InputHandler.dispatch(ServerDiscoveryEvent.inDeleteNode(id));
    }

    // This function will send a request to move a node to the input handler
    // eventually.
    private void moveNodeReq(String id, int x, int y) {
        InputHandler.dispatch(ServerDiscoveryEvent.inMoveNode(id, x, y));
    }

    public void moveNode(String id, int x, int y) {
        // Get the gnode from the map
        GNode gnode = getGNode(id);

        // If it doesn't exist, theres a problem
        assert (gnode != null);

        // move the x y coords
        gnode.setXY(x, y);

        // drop any connections this node might have
        animations.dropConns(gnode);

    }

    public void deleteNode(String id) {
        // Get the gnode
        GNode gnode = getGNode(id);

        // If it doesn't exist, just return
        if (gnode == null) {
            return;
        }

        // Unselect this node
        gnode.unselect();

        // Remove it from the layeredPanel
        this.remove(this.getIndexOf(gnode));

        // remove it from the map
        gnodemap.remove(id);

        // cleanup the gnode itself
        gnode.cleanup();

        // drop any connections it might have
        animations.removeRangeIndicator(gnode);
        animations.dropConns(gnode);

        gnode = null;

        reassessFPS();
        this.invalidate();
        this.repaint();
    }

    public void setNodeRange(String id, int range) {
        GNode g = getGNode(id);
        if (g == null)
            return;

        g.setRange(range);

    }

    // This function adds a node to the GUI. It's assumed that the node now exists
    // in the simulator.
    public void addNewNode(int x, int y, int range, String id) {
        // instantiate a new GNode
        GNode node = new GNode(id, x, y, range, this);

        // add it to the gnode map
        gnodemap.put(id, node);

        // add it to the canvas
        this.add(node, JLayeredPane.PALETTE_LAYER);

        // add our node listener
        node.addListener(new NodeActionHandler());

        // set the locked replay status
        node.setLockedReplayMode(lockedReplayMode);

        // Add a range indicator
        animations.addRangeIndicator(node);

        reassessFPS();

    }

    public void setGraphicsEnabled(boolean isEnabled) {
        this.graphicsEnabled = isEnabled;
        if (isEnabled) {
            animations.start();
        } else {
            animations.stop();
        }
    }

    // ////////////////////////Data

    private void reassessFPS() {
        // reassess the FPS
        int newFPS = 0;
        if (gnodemap.size() != 0) {
            newFPS = (1000 / gnodemap.size());
        }
        int usedFPS;
        if (newFPS < Defaults.MAXFPS && newFPS > Defaults.MINFPS)
            usedFPS = newFPS;
        else if (newFPS <= Defaults.MINFPS) {
            usedFPS = Defaults.MINFPS;
        } else
            usedFPS = Defaults.MAXFPS;

        animations.setFPS(usedFPS);
    }

    // Inner classes

    public void nodeBroadcast(String nodeId) {
        if (!graphicsEnabled) {
            return;
        }
        GNode n = getGNode(nodeId);
        if (n == null)
            return;
        animations.nodeBroadcast(n);
    }

    public void traceMessage(String fromId, String toId, Color color,
                             int longevityFactor, int fatness, int priority) {

        if (!graphicsEnabled) {
            return;
        }
        GNode a = getGNode(fromId);
        GNode b = getGNode(toId);

        if (a == null || b == null)
            return;

        animations.traceMessage(a, b, color, longevityFactor, fatness, priority);

    }

    public void selectNode(String nodeId) {
        GNode g = getGNode(nodeId);
        if (g == null) {
            return;
        } else {
            g.select();
        }

    }

    private void lockCanvas(boolean locked) {
        if (locked) {
            lockBox = new LockBox(this);
            addComponentListener(lockBox);
            lockBox.updateSize();
        } else {
            if (lockBox != null) {
                this.removeComponentListener(lockBox);
                this.remove(lockBox);
            }
        }
    }

    /**
     * You can use this function to determine the state of the sim area.
     *
     * @return the locked
     * @author Mehrdad Karami
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * This function will allow the GUI to sort of "deactivate" the the signal
     * handlers.
     *
     * @param locked the state of the simulation
     * @author Mehrdad Karami
     */
    public void setLocked(boolean locked) {
        this.locked = locked;

        // propagate signal to nodes
        for (GNode gnode : gnodemap.values()) {
            gnode.setLocked(locked);
        }

        lockCanvas(locked);

    }

    public void clear() {
        ArrayList<String> nodeIds = new ArrayList<String>();
        for (GNode n : gnodemap.values()) {
            // Accumulate every node id
            nodeIds.add(n.getId());
        }
        for (String id : nodeIds) {
            deleteNode(id);
        }
    }

    public void setSimSpeed(int speed) {
        // propagate the speed setting down to animation sub systems
        Animations.setSimSpeed(speed);

    }

    public void simPaused() {
        // Drop all message animations
        animations.dropAll();
    }

    public void simStopped() {
        // Drop all message animations
        animations.dropAll();

        // lock the user interface
        setLocked(true);
    }

    public void setNodeAttributesArea(NodeAttributesArea nodeAttributesArea) {
        this.nodeAttributesArea = nodeAttributesArea;
    }

    public void setLockedReplayMode(boolean b) {
        lockedReplayMode = b;
        for (GNode n : gnodemap.values()) {
            n.setLockedReplayMode(b);
        }
    }

    class NodeActionHandler implements GNodeListener {

        public void nodeMoved(GNode n, int x, int y) {
            // enforce simArea's bound restrictions
            Point boundedPoint = getBoundedNodePoint(new Point(x, y));

            // drop all connection animations associated with this node
            animations.dropConns(n);

            // issue a move node request
            moveNodeReq(n.getId(), boundedPoint.x, boundedPoint.y);

            // propagate the signal to other listeners
            for (GNodeListener g : nodeListeners) {
                g.nodeMoved(n, x, y);
            }
        }

        public void nodeEntered(GNode n) {

            // propagate the signal to other listeners
            for (GNodeListener g : nodeListeners) {
                g.nodeEntered(n);
            }
        }

        public void nodeExited(GNode n) {

            // propagate the signal to other listeners
            for (GNodeListener g : nodeListeners) {
                g.nodeExited(n);
            }
        }

        public void nodeSelected(GNode n) {

            // propagate the signal to other listeners
            for (GNodeListener g : nodeListeners) {
                g.nodeSelected(n);
            }
        }

        public void nodePopupEvent(GNode n, int x, int y) {
            EditNodePopup edit_menu = new EditNodePopup();
            edit_menu.gnode = n;
            edit_menu.gnodemap = gnodemap;
            edit_menu.show(n, x, y);

        }

    }

    // Pop up menu for adding nodes
    class AddNodePopup extends JPopupMenu implements ActionListener {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        JMenuItem anItem1;
        JMenuItem anItem2;
        int x, y;

        public AddNodePopup() {
            anItem1 = new JMenuItem("Add a new server");
            anItem1.addActionListener(this);
            add(anItem1);
        }

        public void actionPerformed(ActionEvent e) {
            //Enforce boundaries
            Point p = getBoundedNodePoint(new Point(this.x, this.y));
            addNewNodeReq(p.x, p.y);
        }
    }

    // Pop up menu for editing/deleting nodes
    class EditNodePopup extends JPopupMenu {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        JMenuItem delete_item;
        JMenuItem msg_item;
        JMenuItem view_item;
        GNode gnode;
        TreeMap<String, GNode> gnodemap;

        public EditNodePopup() {
            delete_item = new JMenuItem("Delete node");
            delete_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteNodeReq(gnode.getId());
                }
            });

            msg_item = new JMenuItem("Send Message");
            msg_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new SendNodeMessageDialog(null, gnode.getId(), nodeAttributesArea
                            .getNodeList());
                }
            });

            view_item = new JMenuItem("View attributes");
            view_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (nodeAttributesArea != null) {
                        nodeAttributesArea.openNodeDialog(gnode.getId());
                    }
                }
            });

            if (!lockedReplayMode) {
                add(delete_item);
                add(msg_item);
            }
            add(view_item);

        }

    }

    // Listener for spawning new pop menus
    class PopClickListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                // If the Sim Area is locked just return.
                if (locked == true)
                    return;

                doPop(e);
            }
        }

        // override
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                // If the Sim Area is locked just return.
                if (locked == true)
                    return;

                doPop(e);
            }
        }

        private void doPop(MouseEvent e) {
            // If the Sim Area is locked just return.
            if (locked || lockedReplayMode)
                return;

            // Show the "Add Node" menu.
            AddNodePopup menu = new AddNodePopup();
            menu.x = e.getX();
            menu.y = e.getY();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class LockBox extends JPanel implements ComponentListener {

        private static final long serialVersionUID = 1L;
        private JLayeredPane parent;

        public LockBox(JLayeredPane parent) {
            this.parent = parent;
            setVisible(true);
            parent.add(this, JLayeredPane.POPUP_LAYER);
            setOpaque(false);
            setLocation(0, 0);

        }

        public void updateSize() {
            this.setSize(parent.getSize());
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(LOCKBOX_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public void componentHidden(ComponentEvent arg0) {
            this.updateSize();
        }

        @Override
        public void componentMoved(ComponentEvent arg0) {
            this.updateSize();
        }

        @Override
        public void componentResized(ComponentEvent arg0) {
            this.updateSize();
        }

        @Override
        public void componentShown(ComponentEvent arg0) {
            this.updateSize();
        }
    }


}
