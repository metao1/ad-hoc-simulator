package server.discovery.gui;

import server.discovery.Defaults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class XYTickPanel extends JPanel implements ComponentListener {
    private static final long serialVersionUID = 1L;
    private static Font f = Defaults.FONT;

    public XYTickPanel(Component parent) {
        setLocation(0, 0);
        parent.addComponentListener(this);
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension size = getSize();
        int maxX, maxY;
        maxX = size.width;
        maxY = size.height;

        g.setColor(Color.GRAY);

        //Draw the X ticks
        for (int i = 5; i < maxX; i += 5) {
            if (i % 100 == 0) {
                g.drawLine(i, 0, i, 10);
                g.setFont(f);
                FontMetrics fm = g.getFontMetrics();

                g.drawString(Integer.toString(i),
                        i - (fm.stringWidth(Integer.toString(i)) / 2),
                        10 + fm.getAscent());
            } else if (i % 50 == 0) {
                g.drawLine(i, 0, i, 6);
            } else {
                g.drawLine(i, 0, i, 3);
            }
        }

        //Draw the Y ticks
        for (int i = 5; i < maxY; i += 5) {
            if (i % 100 == 0) {
                g.drawLine(0, i, 10, i);
                g.setFont(f);
                FontMetrics fm = g.getFontMetrics();

                g.drawString(Integer.toString(i),
                        10 + 2,
                        i + fm.getAscent() / 2);
            } else if (i % 50 == 0) {
                g.drawLine(0, i, 6, i);
            } else {
                g.drawLine(0, i, 3, i);
            }

        }

    }

    @Override
    public void componentHidden(ComponentEvent arg0) {
        this.setSize(getParent().getSize());
    }

    @Override
    public void componentMoved(ComponentEvent arg0) {
        this.setSize(getParent().getSize());
    }

    @Override
    public void componentResized(ComponentEvent arg0) {
        this.setSize(getParent().getSize());
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
        this.setSize(getParent().getSize());
    }

}
