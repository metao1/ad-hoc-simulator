package server.discovery.gui;

import server.discovery.Defaults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Animations extends JPanel implements ComponentListener,
        ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static long anicount = System.currentTimeMillis();
    static int connLifeTime = 0;
    private static Stroke BASIC_STROKE = new BasicStroke(1);
    private static Stroke SELECTED_RANGE_STROKE = new BasicStroke(Defaults.SELECTED_NODE_RANGE_INDICATOR_THICKNESS);
    private static Stroke BROADCAST_STROKE = new BasicStroke(Defaults.BROADCAST_THICKNESS);
    private static int xPoints[] = new int[4];
    private static int yPoints[] = new int[4];
    Timer repaintTimer = new Timer(100, this);
    LinkedList<Connection> connStore = new LinkedList<Connection>();
    HashMap<GNode, RangeIndicator> riStore = new HashMap<GNode, RangeIndicator>();
    LinkedList<Connection> topList = new LinkedList<Connection>();
    public Animations(JLayeredPane parent) {
        parent.add(this, JLayeredPane.DEFAULT_LAYER);
        parent.addComponentListener(this);
        setOpaque(false);
        setLocation(0, 0);
        repaintTimer.start();
    }

    public static void setSimSpeed(int speed) {
        // There's no science here, I've just been guesstimating to arrive at this
        // multiplier.
        connLifeTime = 1500 + speed * 300;
    }

    static public void drawThickLine(
            Graphics g, int x1, int y1, int x2, int y2, int thickness, Color c) {
        // The thick line is in fact a filled polygon
        g.setColor(c);
        int dX = x2 - x1;
        int dY = y2 - y1;
        // line length
        double lineLength = Math.sqrt(dX * dX + dY * dY);

        double scale = (double) (thickness) / (2 * lineLength);

        // The x,y increments from an endpoint needed to create a rectangle...
        double ddx = -scale * (double) dY;
        double ddy = scale * (double) dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;

        // Now we can compute the corner points...
        xPoints[0] = x1 + dx;
        yPoints[0] = y1 + dy;
        xPoints[1] = x1 - dx;
        yPoints[1] = y1 - dy;
        xPoints[2] = x2 - dx;
        yPoints[2] = y2 - dy;
        xPoints[3] = x2 + dx;
        yPoints[3] = y2 + dy;

        g.fillPolygon(xPoints, yPoints, 4);
    }

    static void drawConn(Graphics g, Color c, int x1, int y1, int x2, int y2, int fatness) {

        g.setColor(c);
        if (fatness != 1) {
            drawThickLine(g, x1, y1, x2, y2, fatness, c);
        } else {
            g.drawLine(x1, y1, x2, y2);
        }


        double stepX = (double) (x1 - x2) / Defaults.MESSAGE_ANISPEED_MILLISECONDS;
        double stepY = (double) (y1 - y2) / Defaults.MESSAGE_ANISPEED_MILLISECONDS;


        g.fillRect(x1 - (int) (stepX * (anicount % (Defaults.MESSAGE_ANISPEED_MILLISECONDS))), y1
                - (int) (stepY * (anicount % (Defaults.MESSAGE_ANISPEED_MILLISECONDS))), 3 + fatness * 2, 3 + fatness * 2);
    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public void traceMessage(GNode a, GNode b, Color color, int longevityFactor, int fatness, int priority) {

        Connection c = new Connection(a, b, color, longevityFactor, fatness, priority);
        connStore.add(c);
    }

    public void nodeBroadcast(GNode gnode) {
        RangeIndicator ri = riStore.get(gnode);

        if (ri == null) return;

        ri.fireBroadcast();
    }

    public void stop() {
        repaintTimer.stop();
        dropAll();
        repaint();
    }

    public void start() {
        repaintTimer.start();
    }

    public void setFPS(int fps) {
        repaintTimer.setDelay(1000 / fps);
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        anicount = System.currentTimeMillis();

        //clear out the top list
        topList.clear();

        // Draw the message tracing animation
        Iterator<Connection> i = connStore.iterator();
        while (i.hasNext()) {

            Connection c = i.next();
            if (c.shouldDie()) {
                // remove it from the store
                i.remove();
                continue;
            }

            // Draw the lowest priority connections
            if (c.priority == 0) {
                int x1, y1, x2, y2;
                x1 = c.fromNode.getCenter().x;
                y1 = c.fromNode.getCenter().y;
                x2 = c.toNode.getCenter().x;
                y2 = c.toNode.getCenter().y;
                drawConn(g, c.color, x1, y1, x2, y2, c.fatness);
            } else {
                topList.add(c);
            }
        }
        //Draw the topList connections
        for (Connection c : topList) {
            int x1, y1, x2, y2;
            x1 = c.fromNode.getCenter().x;
            y1 = c.fromNode.getCenter().y;
            x2 = c.toNode.getCenter().x;
            y2 = c.toNode.getCenter().y;
            drawConn(g, c.color, x1, y1, x2, y2, c.fatness);
        }

        // Draw the range indicators and broadcasts
        Iterator<RangeIndicator> j = riStore.values().iterator();
        while (j.hasNext()) {
            RangeIndicator ri = j.next();

            // Draw the range ring
            ri.drawRange(g);

            if (ri.isActive()) {
                ri.drawBroadcast(g);
            }

        }


    }

    @Override
    public void componentHidden(ComponentEvent arg0) {
        // TODO Auto-generated method stub
        updateSize();

    }

    @Override
    public void componentMoved(ComponentEvent arg0) {
        updateSize();
        // TODO Auto-generated method stub

    }

    @Override
    public void componentResized(ComponentEvent arg0) {
        updateSize();
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
        updateSize();
        // TODO Auto-generated method stub

    }

    public void dropConns(GNode n) {
        for (Connection conn : connStore) {
            if (conn.fromNode == n || conn.toNode == n) {
                conn.drop();
            }
        }
    }

    public void removeConn(Connection c) {
        for (Connection conn : connStore) {
            if (conn.equals(c)) {
                conn.drop();
                return;
            }
        }

    }

    public void dropAll() {
        for (Connection conn : connStore) {
            conn.drop();
        }

        for (RangeIndicator ri : riStore.values()) {
            ri.drop();
        }

    }

    public void updateSize() {
        this.setSize(getParent().getSize());
    }

    public void addRangeIndicator(GNode g) {
        riStore.put(g, new RangeIndicator(g));
    }

    public void removeRangeIndicator(GNode g) {
        riStore.remove(g);
    }

    class Connection {
        GNode fromNode;
        GNode toNode;
        long dieCount;
        long startCount;
        int priority; //0 is lowest layer
        int fatness;
        Color color;

        Connection(GNode fromNode, GNode toNode, Color color, int longevityFactor, int fatness, int priority) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.color = color;
            this.priority = priority;
            this.fatness = fatness;
            startCount = anicount;
            dieCount = anicount + Animations.connLifeTime * longevityFactor;
        }

        public boolean shouldDie() {
            if (anicount >= dieCount)
                return true;
            else
                return false;
        }

        public boolean equals(Object b) {
            Connection B = (Connection) b;
            if (fromNode != B.fromNode)
                return false;
            if (toNode != B.toNode)
                return false;
            return true;
        }

        public void drop() {
            this.dieCount = 0;
        }
    }

    class RangeIndicator {
        GNode parent_;
        long startStep;
        int totalSteps = Defaults.BROADCAST_ANISPEED_MILLISECONDS;

        RangeIndicator(GNode parent) {
            // Copy in attributes
            parent_ = parent;
        }

        public void fireBroadcast() {
            startStep = anicount;
        }

        public void drop() {
            startStep = 0;
        }

        public void drawRange(Graphics g) {

            Graphics2D g2 = (Graphics2D) g;

            // Draw the graphic
            int x = parent_.getCenter().x - parent_.getRange();
            int y = parent_.getCenter().y - parent_.getRange();
            int width = parent_.getRange() * 2;
            int height = parent_.getRange() * 2;
            if (parent_.isSelected()) {
                g2.setColor(Defaults.SELECTED_RANGE_COLOR);
                g2.setStroke(SELECTED_RANGE_STROKE);
            } else {
                g2.setColor(Defaults.RANGE_COLOR);
            }
            g2.drawOval(x, y, width, height);

            //restore the original stroking context
            g2.setStroke(BASIC_STROKE);
        }

        public void drawBroadcast(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            // Draw the graphic

            int bCastRadius = (int) (parent_.getRange() * ((double) curStep() / (double) totalSteps));
            int bCastX = parent_.getCenter().x - bCastRadius;
            int bCastY = parent_.getCenter().y - bCastRadius;

            g2.setColor(Defaults.BROADCAST_COLOR);
            g2.setStroke(BROADCAST_STROKE);
            g2.drawOval(bCastX, bCastY, bCastRadius * 2, bCastRadius * 2);
            g2.setStroke(BASIC_STROKE);
        }

        public boolean isActive() {
            return (Animations.anicount - startStep < totalSteps && Animations.anicount > startStep);
        }

        public long curStep() {
            return Animations.anicount - startStep;
        }

    }

}
