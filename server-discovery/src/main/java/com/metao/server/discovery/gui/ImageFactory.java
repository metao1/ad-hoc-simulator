package com.metao.server.discovery.gui;

import com.metao.server.discovery.Defaults;
import com.metao.server.discovery.Utilities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class ImageFactory {
    //Function that creates a selected image counterpart for a given image
    private static final Color SELECTED_OVERLAY_COLOR = new Color(0, 0, 255, 20); //blue + 30% transparency
    private static final Color SELECTED_BOX_COLOR = new Color(0, 0, 100, 255);
    static private BufferedImage nodeImg_;
    static private BufferedImage hoveredNodeImg_;
    static private BufferedImage selectedNodeImg_;
    static private BufferedImage ghostedNodeImg_;
    static private boolean isInit = false;
    static private ImageFactory instance = new ImageFactory();

    //set the static instance of the node image. If it's already set, return a ref to it.
    static public BufferedImage getNodeImg() {
        checkInit();
        return nodeImg_;
    }

    static public BufferedImage getHoveredNodeImg() {
        checkInit();
        return hoveredNodeImg_;
    }

    static public BufferedImage getSelectedNodeImg() {
        checkInit();
        return selectedNodeImg_;
    }

    static public BufferedImage getGhostedNodeImg() {
        checkInit();
        return ghostedNodeImg_;
    }

    static public void checkInit() {
        if (isInit) {
            return;
        }
        init();
        isInit = true;
    }

    static private void init() {
        //initalize images
        try {
            nodeImg_ = ImageIO.read(instance.getClass().getResource("/img/server.png"));
            hoveredNodeImg_ = getHoverImg(nodeImg_);
            selectedNodeImg_ = getSelectedImg(nodeImg_);
            ghostedNodeImg_ = getTransparentImg(nodeImg_, 0.5f);

        } catch (Exception e) {
            Utilities.showError("Failed to load images. Please file a bug report");
            System.exit(1);
        }

    }

    //Transformation functions
    //Function that creates a new transparent image from a given image
    static public BufferedImage getTransparentImg(BufferedImage src, float alpha) {
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        int rule = AlphaComposite.SRC_OVER;
        AlphaComposite ac = AlphaComposite.getInstance(rule, alpha);
        g2.setComposite(ac);
        g2.drawImage(src, null, 0, 0);
        g2.dispose();
        return dest;
    }

    //Function that creates a hover image counterpart for a given image
    static public BufferedImage getHoverImg(BufferedImage src) {
        BufferedImage dest = new BufferedImage(src.getWidth(null), src.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        g.drawImage(src, null, 0, 0);
        RescaleOp rescaleOp = new RescaleOp(1.5f, 0.0f, null);
        rescaleOp.filter(dest, dest);
        g.dispose();
        return dest;
    }

    static public BufferedImage getSelectedImg(BufferedImage src1) {
        BufferedImage src = getHoverImg(src1);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        g.setColor(SELECTED_OVERLAY_COLOR);
        g.drawImage(src, null, 0, 0);
        g.fillRect(0, 0, dest.getWidth(null), dest.getHeight(null));
        g.setColor(SELECTED_BOX_COLOR);
        for (int i = 0; i < 1; i++) {
            g.drawRoundRect(i, i, dest.getWidth(null) - 2 * i - 1, dest.getHeight(null) - 2 * i - 1, 10, 10);
        }

        g.dispose();
        return dest;
    }


    //Function that draws a letter directly onto a buffered image
    static public void drawNodeID(Graphics g1, String id, Rectangle r) {
        //get the center
        Graphics2D g = (Graphics2D) g1;

        g.setFont(Defaults.NODEID_FONT);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(id,
                r.x + 20 + r.width / 2 - fm.stringWidth(id) / 2,
                r.y + r.height / 2 + fm.getAscent() / 2 - 1);


    }
}
