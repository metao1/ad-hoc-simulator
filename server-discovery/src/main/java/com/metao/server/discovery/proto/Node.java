/**
 *
 */
package com.metao.server.discovery.proto;

import com.metao.server.discovery.Message;
import com.metao.server.discovery.NodeAttributes;

import javax.swing.*;

/**
 * Base Class for all protocol specific node types.
 *
 * @author Mehrdad Karami
 */
public abstract class Node {

    /**
     * Attributes member
     */
    protected NodeAttributes att;

    /**
     * Node Constructor.
     * <p>
     * All nodes must have node attributes set.
     *
     * @param atts Node Attributes
     * @author Mehrdad Karami
     */
    public Node(NodeAttributes atts) {
        this.setAttributes(atts);
    }

    /**
     * **************************************************************************
     * *** Abstract methods that must be implemented to define a new protocol.
     * **************************************************************************
     */

    /**
     * Pop a message of the node's transmit queue and return it.
     * <p>
     * This function is used to return a message off the transmit queue of a node
     * and return it for the simulation engine to consume. Effectively this is
     * used to simulate the transmittal of a message into the network.
     * <p>
     * When a protocol implements this function it should call the appropriate
     * ServerDiscoveryEvent for the message that is being sent. A message is either a Control
     * message or Narrative. Example follows:
     * <p>
     * OutputHandler.dispatch(ServerDiscoveryEvent.outControlMsgTransmitted(this.att.id,
     * message));
     * <p>
     * OutputHandler.dispatch(ServerDiscoveryEvent.outNarrMsgTransmitted(this.att.id,
     * message));
     *
     * @return Message Message that is being sent into the network.
     * @author Mehrdad Karami
     */
    public abstract Message messageToNetwork();

    /**
     * Push a message into the node's receive queue.
     * <p>
     * This function is used to deliver a message to a node. The message will be
     * placed into the nodes receive queue effectively the node is receiving the
     * message.
     * <p>
     * When a protocol implements this function it should call the appropriate
     * ServerDiscoveryEvent for the message that is being received. A message is either a
     * Control message or Narrative. Example follows:
     * <p>
     * OutputHandler.dispatch(ServerDiscoveryEvent.outControlMsgReceived(this.att.id,
     * message));
     * <p>
     * OutputHandler.dispatch(ServerDiscoveryEvent.outNarrMsgReceived(this.att.id, message));
     *
     * @param message Message to be delivered to the node.
     * @author Mehrdad Karami
     */
    public abstract void messageToNode(Message message);

    /**
     * Send a narrative message from one node to another.
     * <p>
     * Narrative messages are messages that the user initiates. This method will
     * be called by the simulation engine to notify the node that the user
     * requests it send a message.
     * <p>
     * This method should build a new narrative message and place it on the
     * transmit message queue.
     * <p>
     * Upon successful processing of a narrative message if the node receiving the
     * message is the destination for the message an addition ServerDiscoveryEvent should be
     * sent.
     * <p>
     * OutputHandler.dispatch(ServerDiscoveryEvent.outMsgRecieved(MsgOrigID, MsgDestID,
     * MsgText));
     *
     * @param sourceID
     * @param destinationID
     * @param messageText
     * @author Mehrdad Karami
     */
    public abstract void newNarrativeMessage(String sourceID,
                                             String destinationID, String messageText);

    /**
     * Process an iteration of this node.
     * <p>
     * This will do all the processing for a node's time interval.
     *
     * @author Mehrdad Karami
     */
    public abstract void clockTick();

    /**
     * Return a JDialog that will be displayed by the GUI.
     * <p>
     * Each protocol must define this function so that the GUI can inspect the
     * nodes information.
     *
     * @author Mehrdad Karami
     */
    public abstract JDialog getNodeDialog();

    /**
     * Update the previously returned JDialog with the latest information for a
     * node that will be showed to the GUI.
     * <p>
     * <p>
     * Each protocol must define this function so that the GUI can inspect the
     * nodes information.
     *
     * @author Mehrdad Karami
     */
    public abstract void updateNodeDialog(JDialog dialog);

    /**
     * **************************************************************************
     * *** Standard Node methods. Additional protocols should not modify these.
     * **************************************************************************
     */

    /**
     * This function will return the attributes that are defined in the Node
     * class.
     * <p>
     * Note, this returns a copy of the node attributes. Not a reference to the
     * attributes object itself.
     *
     * @return NodeAttributes
     */
    public NodeAttributes getAttributes() {
        return att;
    }

    /**
     * Sets the Node's attributes
     *
     * @param atts The new attributes for the node.
     * @author Mehrdad Karami
     */
    public void setAttributes(NodeAttributes atts) {
        this.att = atts;
    }

    /**
     * Sets the X and Y coordinates of the node.
     *
     * @param x The new x coordinate.
     * @param y The new y coordinate.
     */
    public void setXY(int x, int y) {
        this.att = new NodeAttributes(att.id, x, y, att.range, att.isPromiscuous);
    }

    /**
     * Sets the range of the node.
     *
     * @param newRange
     */
    public void setRange(int range) {
        this.att = new NodeAttributes(att.id, att.x, att.y, range,
                att.isPromiscuous);
    }

    /**
     * Return true if the nodes is listen only.
     *
     * @return True/False based on the nodes Promiscuity
     * @author Mehrdad Karami
     */
    public boolean isPromiscuous() {
        return this.att.isPromiscuous;
    }

    /**
     * Set whether or not a node is listen only.
     *
     * @param value
     * @author Mehrdad Karami
     */
    public void setPromiscuity(boolean value) {
        this.att = new NodeAttributes(this.att.id, this.att.x, this.att.y,
                this.att.range, value);
    }

}
