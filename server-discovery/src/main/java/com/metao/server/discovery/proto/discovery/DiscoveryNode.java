package com.metao.server.discovery.proto.discovery;

import com.metao.server.discovery.Message;
import com.metao.server.discovery.NodeAttributes;
import com.metao.server.discovery.OutputHandler;
import com.metao.server.discovery.event.ServerDiscoveryEvent;
import com.metao.server.discovery.proto.Node;

import javax.swing.*;
import java.util.*;

public class DiscoveryNode extends Node {

    /**
     * **************************************************************************
     * *** Constants Needed by Discovery Server
     * **************************************************************************
     */

    // TODO: Adjust constant values after experimentation.

    /**
     * Send Updates Interval
     * <p>
     * Each node periodically sends routing table updates. This is the interval
     * between transmissions. Measured in clock ticks.
     */
    public static final int UPDATE_INTERVAL = 10;

    /**
     * Maximum Size of Network Protocol Data Unit(NPDU)
     * <p>
     * This is basically the maximum number of route updates that can be sent in
     * one message.
     */
    public static final int MAX_NPDU = 10;

    /**
     * Route Timeout
     * <p>
     * If a route has not been updated in ROUTE_TIMEOUT then it will be considered
     * a broken link.
     */
    public static final int ROUTE_TIMEOUT = UPDATE_INTERVAL * 3;

    /**
     * Infinity Hop Count
     * <p>
     * Infinity Hop Count is a sentinel value that is used to mark a broken Link.
     */
    public static final int INFINITY_HOPS = -1;

    /**
     * **************************************************************************
     * *** Private Member Fields
     * **************************************************************************
     */

    /**
     * Current Tick
     * <p>
     * Time is loosely defined in the simulation. This is the current tick count
     * for the node. Basically this is the node's time.
     */
    private int CurrentTick = 0;

    /**
     * Node Sequence Number
     * <p>
     * The node sequence number is a identifier that is unique across all protocol
     * control messages(RREQ, RREP, RERR) for a node. It is incremented
     * immediately before a protocol control message is generated.
     */
    private int LastSeqNum = 1;

    /**
     * Last Tick that a Route Table Update Message was Sent
     */
    private int LastUpdate = 0;

    /**
     * Last Tick that a Route Table Full Dump Update Message was Sent
     */
    private int LastFullUpdate = -1;

    /**
     * Route Table
     */
    private HashMap<String, RouteEntry> RouteTable = new HashMap<String, RouteEntry>();

    /**
     * Transmit Queue
     * <p>
     * Queue of messages that are waiting to be transmitted into the network.
     */
    private Queue<Message> txQueue = new LinkedList<Message>();

    /**
     * Receive Queue
     * <p>
     * Queue of messages that have been received from the network.
     */
    private Queue<Message> rxQueue = new LinkedList<Message>();

    /**
     * Private Member Functions
     */

    /**
     * Constructor
     */
    public DiscoveryNode(NodeAttributes atts) {

        /**
         * Set Node Attributes
         */
        super(atts);

        /**
         * Add this node into it's own route table.
         *
         * This kind of creates a functionality similar to Discovery  's Hello message as
         * the node sends out its first Update Message.
         */
        this.RouteTable.put(this.att.id, new RouteEntry(this.att.id,
                this.LastSeqNum, 0, this.att.id, this.CurrentTick));

    }

    /**
     * Check Routes
     * <p>
     * Look at each entry in the route table. If there is an old route that has
     * not been updated mark it as a broken route.
     */
    void checkRoute() {

        /**
         * Iterator and RouteEntry for going through the values in the RouteTable.
         */
        Iterator<RouteEntry> RouteTableIter;
        RouteEntry TempRouteEntry;

        /**
         * List of Broken Route Entry Destinations used to find other routes that
         * are affected by the breakage.
         */
        HashSet<String> DestList = new HashSet<String>();

        /**
         * Get Iterator for RouteTable and then traverse it looking for entries that
         * are newer than tick.
         */
        RouteTableIter = RouteTable.values().iterator();

        while (RouteTableIter.hasNext()) {
            TempRouteEntry = RouteTableIter.next();

            /**
             * If this entry has not been updated in ROUTE_LIFETIME mark it as a
             * broken link.
             */
            if ((TempRouteEntry.getInstTime() + ROUTE_TIMEOUT) <= this.CurrentTick) {
                /**
                 * If this entry is already marked as broken drop it from the route
                 * table.
                 */
                if (TempRouteEntry.getHopCount() == INFINITY_HOPS) {
                    RouteTableIter.remove();
                }

                TempRouteEntry.setHopCount(INFINITY_HOPS);
                TempRouteEntry.setInstTime(CurrentTick);
                TempRouteEntry.setSeqNum(TempRouteEntry.getSeqNum() + 1);

                this.RouteTable.put(TempRouteEntry.getDestIP(), TempRouteEntry);

                /**
                 * Since the Route Table was modified, set the last update time to -1 to
                 * force an update message.
                 */
                this.LastUpdate = -1;

                /**
                 * Add this destination to the list of Destination that need to be check
                 * for as next hops.
                 */
                DestList.add(TempRouteEntry.getDestIP());
            }
        }

        /**
         * If the DestList has entries in it then check all routes to see if any of
         * the routes us an entry in the DestList as their next hop. If any are
         * found mark them as broken just like was done above.
         */
        RouteTableIter = RouteTable.values().iterator();

        while (RouteTableIter.hasNext()) {
            TempRouteEntry = RouteTableIter.next();

            /**
             * If this entry has a next hop that is in the list of links that were
             * just marked as broken then mark it broken.
             */
            if (DestList.contains(TempRouteEntry.getNextHopIP())) {

                TempRouteEntry.setHopCount(INFINITY_HOPS);
                TempRouteEntry.setInstTime(CurrentTick);
                TempRouteEntry.setSeqNum(TempRouteEntry.getSeqNum() + 1);

                this.RouteTable.put(TempRouteEntry.getDestIP(), TempRouteEntry);
            }
        }
    }

    /**
     * Increment this nodes Sequence Number
     * <p>
     * In addition to incrementing the sequence number this function also take
     * care of updating the Routing Table entry for this nodes entry for itself.
     */
    void incrSeqNum() {

        RouteEntry TempRouteEntry;

        this.LastSeqNum++;

        TempRouteEntry = this.RouteTable.get(this.att.id);
        TempRouteEntry.setSeqNum(this.LastSeqNum);
        TempRouteEntry.setInstTime(this.CurrentTick);
        this.RouteTable.put(this.att.id, TempRouteEntry);

    }

    /**
     * Place message into the transmit queue.
     * <p>
     * This function sends a message into the network by adding it to the transmit
     * queue.
     *
     * @param message Message to be transmitted.
     * @author Mehrdad Karami
     */
    private void sendMessage(Message message) {

        if (!this.att.isPromiscuous) {
            try {
                txQueue.add(message);
            } catch (IllegalStateException exception) {
                OutputHandler
                        .dispatch(ServerDiscoveryEvent
                                .outError(this.att.id
                                        + " Failed to successfully queue message to be sent due to a full transmit queue."));
            }
        }
    }

    /**
     * Send Route Updates
     *
     * @author Mehrdad Karami
     */
    void sendUpdates() {

        /**
         * Updates Message format.
         *
         * TYPE|FLAGS|DESTCOUNT|DESTID1|SEQ1|HOPCOUNT1|...|DESTIDX|SEQX|HOPCOUNTX
         *
         * TYPE = RTUP - Routing Table Update
         *
         * FLAGS =
         *
         * DESTCOUNT = Number of Destination Entries in the message.
         *
         * DESTID = Destination ID
         *
         * SEQ = Destination Sequence Number
         *
         * HOPCOUNT = Hop Count from the Destination to the Sender of this Message.
         * (ie. The receiver of this message would add 1 to the hop count.)
         */

        this.LastUpdate = this.CurrentTick;

        /**
         * Before sending out updates update our sequence number.
         */
        incrSeqNum();

        /**
         * If more routes have changed since the last full update than can be sent
         * out in one update message then send out a full update, otherwise send out
         * an incremental update.
         */
        if (countChangesSinceTick(this.LastFullUpdate) > MAX_NPDU) {
            /**
             * Send Full Update
             */
            this.LastFullUpdate = this.CurrentTick;
            sendFullUpdates();

        } else {
            /**
             * Send Incremental Update
             */
            sendIncrUpdates();

        }

    }

    /**
     * Send an Full Updates Message
     * <p>
     * Only send update entries for all route entries in the route table. This
     * will span multiple route update messages.
     *
     * @author Mehrdad Karami
     */
    void sendFullUpdates() {

        /**
         * Message object that will be passed to sendMessage.
         */
        Message Msg;
        /**
         * MsgStr will hold the message that is sent into the network.
         */
        String MsgStr = "";
        /**
         * Message Properties
         */
        String MsgType = "RTUP";
        String MsgFlags = "";
        int MsgDestCount = 0;
        String MsgDestEntries = ""; /* DESTID1|SEQ1|HOPCOUNT1|... */

        /**
         * Iterator and RouteEntry for going through the values in the RouteTable.
         */
        Iterator<RouteEntry> RouteTableIter;
        RouteEntry TempRouteEntry;

        OutputHandler.dispatch(ServerDiscoveryEvent.outDebug(this.att.id
                + "Starting a Full Update Message"));

        /**
         * Get Iterator for RouteTable and then traverse it looking for entries that
         * are newer than tick.
         */
        RouteTableIter = RouteTable.values().iterator();

        while (RouteTableIter.hasNext()) {
            TempRouteEntry = RouteTableIter.next();

            /**
             * Add this route entry to the Destination Entries List.
             */
            MsgDestCount++;
            MsgDestEntries = MsgDestEntries + '|' + TempRouteEntry.getDestIP() + '|'
                    + TempRouteEntry.getSeqNum() + '|' + TempRouteEntry.getHopCount();

            /**
             * If the update message is full then send it and start a new message.
             */
            if (MsgDestCount == MAX_NPDU) {

                /**
                 * Build the Message string. Note: MsgDestEntries starts with a '|' so
                 * one is not inserted here.
                 */
                MsgStr = MsgType + '|' + MsgFlags + '|' + MsgDestCount + MsgDestEntries;

                Msg = new Message(Message.BCAST_STRING, this.att.id, MsgStr);

                sendMessage(Msg);

                OutputHandler
                        .dispatch(ServerDiscoveryEvent
                                .outDebug(this.att.id
                                        + "Sending a Full Update message as part of a Full Routing Table Dump"));

                /**
                 * Reset the message properties before continuing the loop.
                 */
                MsgDestCount = 0;
                MsgDestEntries = "";
            }
        }

        /**
         * If we have a partial update message left, send it.
         */
        if (MsgDestCount > 0) {
            /**
             * Build the Message string. Note: MsgDestEntries starts with a '|' so one
             * is not inserted here.
             */
            MsgStr = MsgType + '|' + MsgFlags + '|' + MsgDestCount + MsgDestEntries;

            Msg = new Message(Message.BCAST_STRING, this.att.id, MsgStr);

            sendMessage(Msg);

            OutputHandler
                    .dispatch(ServerDiscoveryEvent
                            .outDebug(this.att.id
                                    + "Sending a Partial Update message as part of a Full Routing Table Dump"));

        }
    }

    /**
     * Send an Incremental Updates Message
     * <p>
     * Only send update entries for routes that have changed since the last full
     * update message.
     *
     * @author Mehrdad Karami
     */
    void sendIncrUpdates() {

        /**
         * Message object that will be passed to sendMessage.
         */
        Message Msg;
        /**
         * MsgStr will hold the message that is sent into the network.
         */
        String MsgStr = "";
        /**
         * Message Properties
         */
        String MsgType = "RTUP";
        String MsgFlags = "";
        int MsgDestCount = 0;
        String MsgDestEntries = ""; /* DESTID1|SEQ1|HOPCOUNT1|... */

        /**
         * Iterator and RouteEntry for going through the values in the RouteTable.
         */
        Iterator<RouteEntry> RouteTableIter;
        RouteEntry TempRouteEntry;

        /**
         * Get Iterator for RouteTable and then traverse it looking for entries that
         * are newer than tick.
         */
        RouteTableIter = RouteTable.values().iterator();

        while (RouteTableIter.hasNext()) {
            TempRouteEntry = RouteTableIter.next();

            /**
             * If this entry has been updated since LastFullUpdate then include it.
             */
            if (TempRouteEntry.getInstTime() > this.LastFullUpdate) {
                MsgDestCount++;
                MsgDestEntries = MsgDestEntries + '|' + TempRouteEntry.getDestIP()
                        + '|' + TempRouteEntry.getSeqNum() + '|'
                        + TempRouteEntry.getHopCount();
            }
        }

        /**
         * Build the Message string. Note: MsgDestEntries starts with a '|' so one
         * is not inserted here.
         */
        MsgStr = MsgType + '|' + MsgFlags + '|' + MsgDestCount + MsgDestEntries;

        Msg = new Message(Message.BCAST_STRING, this.att.id, MsgStr);

        sendMessage(Msg);

    }

    /**
     * Take a message off the receive queue.
     * <p>
     * This function receives a message from the network by removing it from the
     * receive queue for processing.
     *
     * @param message The message the is being received from the network.
     * @author Mehrdad Karami
     */
    void receiveMessage(Message message) {

        String MsgType;

        /**
         * Check to see if this node sent the message. If it did then ignore the
         * message.
         */
        if (message.originId.equals(this.att.id)) {
            return;
        }

        /**
         * Get the message type.
         *
         * Message type is always the first token in the message string. Split on
         * the '|' and get the first item in the resultant Array of Strings.
         */
        MsgType = message.message.split("\\|")[0];

        // TODO: Replace this terrible list of if statements with a switch statement
        // once Java 7 is released. Java 7 supposedly has the ability to switch on
        // strings.

        if (MsgType.equals("RTUP")) {
            receiveUpdates(message);
        } else if (MsgType.equals("NARR")) {
            receiveNarrative(message);
        }
    }

    /**
     * Receive Narrative Message.
     *
     * @param message Narrative message received from network.
     * @author Mehrdad Karami
     */
    void receiveNarrative(Message message) {

        /**
         * NARR Message Format
         *
         * TYPE|FLAGS|DESTID|ORIGID|TEXT
         *
         */

        /**
         * The message that will be sent if the message needs forwarded.
         */
        Message Msg;

        /**
         * MsgStr will hold the message that is sent into the network.
         */
        String MsgStr = "";

        /**
         * Message Properties
         */
        String MsgType;
        String MsgFlags;
        String MsgOrigID;
        String MsgDestID;
        String MsgText;

        /**
         * Route Table Entry used to get the destination ID info in our Route Table.
         */
        RouteEntry DestEntry;

        /**
         * Array to hold Message Fields
         */
        String MsgArray[];

        /**
         * Split Message into fields based on '|' delimiters and store in MsgArray.
         */
        MsgArray = message.message.split("\\|");

        /**
         * Store message fields into local variables. Yes this is not really needed
         * but I (SAK) think it makes the code more readable.
         *
         * Note: Skip MsgArray[0] - Message Type.
         */
        MsgType = MsgArray[0];
        MsgFlags = MsgArray[1];
        MsgDestID = MsgArray[2];
        MsgOrigID = MsgArray[3];
        MsgText = MsgArray[4];

        /**
         * Check to see if this node is the final destination of the message. If it
         * is great if not we need to forward it.
         */
        if (this.att.id.equals(MsgDestID)) {
            /**
             * The message has reached its final destination. Consider it delivered.
             */
            OutputHandler.dispatch(ServerDiscoveryEvent.outMsgRecieved(MsgOrigID, MsgDestID,
                    MsgText));
            return;
        }

        /**
         * Need to forward the message on.
         */

        /**
         * Check to see if the destination node ID is in our Route Table.
         */
        if (RouteTable.containsKey(MsgDestID)) {
            /**
             * Get the Route Entry.
             */
            DestEntry = RouteTable.get(MsgDestID);

            /**
             * Create the message string that will be sent.
             */
            MsgStr = MsgType + '|' + MsgFlags + '|' + MsgDestID + '|' + MsgOrigID
                    + '|' + MsgText;

            /**
             * The destination is in our RouteTable. Create the message to be sent.
             */

            Msg = new Message(DestEntry.getNextHopIP(), this.att.id, MsgStr);
            sendMessage(Msg);

            OutputHandler.dispatch(ServerDiscoveryEvent.outDebug(this.att.id
                    + " Forwarded Narrative Message: " + MsgStr));
        } else {
            /**
             * This node does not have a route to the desired destination and thus
             * should not have been used in the route. ERROR.
             */
            OutputHandler.dispatch(ServerDiscoveryEvent.outError(this.att.id
                    + "Received a narrative message for " + MsgDestID
                    + " but has no route to the destination.  Dropping message."));
        }

    }

    /**
     * Receive Route Updates
     * <p>
     * Process an update message and update the route table as needed.
     *
     * @author Mehrdad Karami
     */
    void receiveUpdates(Message message) {
        /**
         * Updates Message format.
         *
         * TYPE|FLAGS|DESTCOUNT|DESTID1|SEQ1|HOPCOUNT1|...|DESTIDX|SEQX|HOPCOUNTX
         *
         * TYPE = RTUP - Routing Table Update
         *
         * FLAGS =
         *
         * DESTCOUNT = Number of Destination Entries in the message.
         *
         * DESTID = Destination ID
         *
         * SEQ = Destination Sequence Number
         *
         * HOPCOUNT = Hop Count from the Destination to the Sender of this Message.
         * (ie. The receiver of this message would add 1 to the hop count.)
         */

        /**
         * Process each entry in the update message.
         */

        /**
         * When two routes to a destination received from two different neighbors
         * Choose the one with the greatest destination sequence number If equal,
         * choose the smallest hop-count
         */

        /**
         * Message Properties
         */
        @SuppressWarnings("unused")
        String MsgType;
        @SuppressWarnings("unused")
        String MsgFlags;
        int MsgDestCount;

        /**
         * Message destination entries will be processed one at a time using these
         * variables.
         */
        String MsgDestEntryID;
        int MsgDestEntrySeq;
        int MsgDestEntryHopCount;

        /**
         * RouteEntry for building new and update old route entries.
         */
        RouteEntry TempRouteEntry;

        /**
         * Array to hold Message Fields
         */
        String MsgArray[];

        /**
         * Split Message into fields based on '|' delimiters and store in MsgArray.
         */
        MsgArray = message.message.split("\\|");

        /**
         * Store message fields into local variables. Yes this is not really needed
         * but I (SAK) think it makes the code more readable.
         */
        MsgType = MsgArray[0];
        MsgFlags = MsgArray[1];
        MsgDestCount = Integer.parseInt(MsgArray[2]);

        for (int i = 0; i < MsgDestCount; i++) {

            /**
             * MsgArray Index is calculated by the following formula.
             *
             * i * n + b + j where
             *
             * i = Index of the Destination in DestEntryList
             *
             * n = Number of fields in a DestEntryList Entry(ID, Seq, HopCount)
             *
             * b = Number of fields before the DestEntryList
             *
             * j = Index of the desired field in the DestEntry(ID = 0, Seq = 1,
             * HopCount = 2)
             *
             */
            MsgDestEntryID = MsgArray[i * 3 + 3];
            MsgDestEntrySeq = Integer.parseInt(MsgArray[i * 3 + 4]);
            MsgDestEntryHopCount = Integer.parseInt(MsgArray[i * 3 + 5]) + 1;

            /**
             * If the destination is not already in the route table, add it.
             */
            if (!this.RouteTable.containsKey(MsgDestEntryID)) {
                TempRouteEntry = new RouteEntry(MsgDestEntryID, MsgDestEntrySeq,
                        MsgDestEntryHopCount, message.originId, this.CurrentTick);
                this.RouteTable.put(MsgDestEntryID, TempRouteEntry);

                /**
                 * Set the last update time to -1 to force an update message to be sent
                 * out advertising our new information.
                 */
                this.LastUpdate = -1;
            } else {
                /**
                 * Update the existing route entry if it needs it.
                 */
                TempRouteEntry = this.RouteTable.get(MsgDestEntryID);

                /**
                 * If the update's sequence number is newer than update ours.
                 */
                if (TempRouteEntry.getSeqNum() < MsgDestEntrySeq) {
                    /**
                     * SAK - THIS IS A DEVIATION FROM WHAT IS WRITTEN IN THE DISCOVERY
                     * PAPERS!!!!!!
                     *
                     * ONLY if we have a change the Hop Count and/or the Next Hop Id will
                     * this update to the route table result in a forced update out to the
                     * network.
                     *
                     */
                    if ((TempRouteEntry.getHopCount() < MsgDestEntryHopCount)
                            || (!TempRouteEntry.getNextHopIP().endsWith(message.originId))) {
                        /**
                         * Set the last update time to -1 to force an update message to be
                         * sent out advertising our new information.
                         */
                        this.LastUpdate = -1;
                    }

                    TempRouteEntry.setHopCount(MsgDestEntryHopCount);
                    TempRouteEntry.setNextHopIP(message.originId);
                    TempRouteEntry.setSeqNum(MsgDestEntrySeq);
                    TempRouteEntry.setInstTime(this.CurrentTick);

                    this.RouteTable.put(MsgDestEntryID, TempRouteEntry);

                } else {
                    /**
                     * If the sequence number in the update is the same as ours and the
                     * hop count is less then update.
                     */
                    if ((TempRouteEntry.getSeqNum() == MsgDestEntrySeq)
                            && (TempRouteEntry.getHopCount() > MsgDestEntryHopCount)) {
                        TempRouteEntry.setHopCount(MsgDestEntryHopCount);
                        TempRouteEntry.setNextHopIP(message.originId);
                        TempRouteEntry.setInstTime(this.CurrentTick);

                        this.RouteTable.put(MsgDestEntryID, TempRouteEntry);
                        /**
                         * Set the last update time to -1 to force an update message to be
                         * sent out advertising our new information.
                         */
                        this.LastUpdate = -1;
                    }
                }

            }

        }
    }

    /**
     * **************************************************************************
     * *** Public Member Functions - Not in Node Interface
     * **************************************************************************
     */

    /**
     * Get the number of route table changes since the given tick..
     *
     * @param tick Clock Tick that we will compare the route entry install times
     *             against.
     * @return The number of Route Table changes since the given tick.
     * @author Mehrdad Karami
     */
    private int countChangesSinceTick(int tick) {

        /**
         * Counting Variable.
         */
        int count = 0;

        /**
         * Iterator and RouteEntry for going through the values in the RouteTable.
         */
        Iterator<RouteEntry> RouteTableIter;
        RouteEntry TempRouteEntry;

        /**
         * Get Iterator for RouteTable and then traverse it looking for entries that
         * are newer than tick.
         */
        RouteTableIter = RouteTable.values().iterator();

        while (RouteTableIter.hasNext()) {
            TempRouteEntry = RouteTableIter.next();

            /**
             * If this entry has been updated since tick then count it.
             */
            if (TempRouteEntry.getInstTime() > tick) {
                count++;
            }
        }

        return (count);
    }

    /**
     * **************************************************************************
     * *** Public Member Functions - Implement Node Interface
     * **************************************************************************
     */

    /**
     * Pop a message of the node's transmit queue and return it.
     * <p>
     * This function is used to return a message off the transmit queue of a node
     * and return it for the simulation engine to consume. Effectively this is
     * used to simulate the transmittal of a message into the network.
     *
     * @return Message Message that is being sent into the network. If there are
     * no more messages Null is returned.
     * @author Mehrdad Karami
     * @see serverDiscovery.node.proto.node.messageToNetwork
     */
    @Override
    public Message messageToNetwork() {

        Message Msg;
        String MsgType;

        try {
            Msg = txQueue.remove();
        } catch (NoSuchElementException exception) {
            return (null);
        }

        /**
         * Get the message type.
         *
         * Message type is always the first token in the message string. Split on
         * the '|' and get the first item in the resultant Array of Strings.
         */
        MsgType = Msg.message.split("\\|")[0];

        // TODO: Replace this terrible list of if statements with a switch
        // statement once Java 7 is released. Java 7 supposedly has the ability
        // to switch on strings.

        /**
         * Switch on the Message type to send the ServerDiscovery in events.
         */
        if (MsgType.equals("RTUP")) {
            OutputHandler.dispatch(ServerDiscoveryEvent.outControlMsgTransmitted(this.att.id,
                    Msg));
        } else if (MsgType.equals("NARR")) {
            OutputHandler.dispatch(ServerDiscoveryEvent.outNarrMsgTransmitted(this.att.id, Msg));
        }

        return (Msg);
    }

    /**
     * Push a message into the node's receive queue.
     * <p>
     * This function is used to deliver a message to a node. The message will be
     * placed into the nodes receive queue effectively the node is receiving the
     * message.
     *
     * @param message Message to be delivered to the node.
     * @author Mehrdad Karami
     * @see serverDiscovery.node.proto.node.messageToNode
     */
    @Override
    public void messageToNode(Message message) {

        String MsgType;

        try {
            rxQueue.add(message);
        } catch (IllegalStateException exception) {
            OutputHandler
                    .dispatch(ServerDiscoveryEvent
                            .outError(this.att.id
                                    + " Failed to successfully receive message due to a full receive queue."));
            return;
        }

        /**
         * Get the message type.
         *
         * Message type is always the first token in the message string. Split on
         * the '|' and get the first item in the resultant Array of Strings.
         */
        MsgType = message.message.split("\\|")[0];

        if (MsgType.equals("RTUP")) {
            OutputHandler.dispatch(ServerDiscoveryEvent.outControlMsgReceived(this.att.id,
                    message));
        } else if (MsgType.equals("NARR")) {
            OutputHandler
                    .dispatch(ServerDiscoveryEvent.outNarrMsgReceived(this.att.id, message));
        }

    }

    /**
     * Send a narrative message from one node to another.
     * <p>
     * Narrative messages are messages that the user inits.
     *
     * @param sourceID
     * @param destinationID
     * @param messageText
     * @author Mehrdad Karami
     */
    @Override
    public void newNarrativeMessage(String sourceID, String destinationID,
                                    String messageText) {

        /**
         * NARR Message Format
         *
         * TYPE|FLAGS|DESTID|ORIGID|TEXT
         *
         */

        /**
         * The message that will be sent.
         */
        Message Msg;

        /**
         * MsgStr will hold the message that is sent into the network.
         */
        String MsgStr = "";

        /**
         * Message Properties
         */
        String MsgType = "NARR";
        String MsgFlags = "";
        String MsgOrigID = sourceID;
        String MsgDestID = destinationID;

        /**
         * Route Table Entry used to get the destination ID info in our Route Table.
         */
        RouteEntry DestEntry;

        /**
         * Check to make sure that the sourceID is this node.
         */
        if (!this.att.id.equals(sourceID)) {
            OutputHandler.dispatch(ServerDiscoveryEvent.outError(this.att.id
                    + " Tried to create a new Narrative Message but the source ID was "
                    + sourceID));
        }

        /**
         * Build the Message String.
         *
         * This is independent of which logic path is chosen below.
         */
        MsgStr = MsgType + '|' + MsgFlags + '|' + MsgDestID + '|' + MsgOrigID + '|'
                + messageText;

        /**
         * Check to see if the destination node ID is in our Route Table.
         */
        if (RouteTable.containsKey(destinationID)) {
            /**
             * Get the Route Entry.
             */
            DestEntry = RouteTable.get(destinationID);
            /**
             * The destination is in our RouteTable.
             */
            Msg = new Message(DestEntry.getNextHopIP(), this.att.id, MsgStr);
            sendMessage(Msg);

            OutputHandler.dispatch(ServerDiscoveryEvent.outDebug(MsgStr));
            /**
             * Done processing this request.
             */
            return;
        } else {
            /**
             * Do not have a route to the destination node.
             */
            OutputHandler.dispatch(ServerDiscoveryEvent.outNodeInfo(this.att.id
                    + " Wants to send to " + MsgDestID
                    + " but has no Route.  The message will be dropped."));
        }
    }

    @Override
    public void clockTick() {

        /**
         * Increment the CurrentTick for this time quantum.
         */
        this.CurrentTick++;

        /**
         * Receive and process each message on the Receive Queue.
         */
        while (!rxQueue.isEmpty()) {
            receiveMessage(rxQueue.remove());
        }

        /**
         * Check for link breakage.
         */
        // check routes.

        if (this.CurrentTick >= (this.LastUpdate + UPDATE_INTERVAL)) {
            sendUpdates();
        }

    }

    /**
     * getNodeDialog
     * <p>
     * This method will construct a JDialog from select node information to return
     * back to the GUI to be displayed.
     *
     * @return JDialog
     */
    @Override
    public JDialog getNodeDialog() {
        DiscoveryDialog dialog = new DiscoveryDialog(null, this.att.id, this.CurrentTick,
                this.RouteTable);
        return (JDialog) dialog;
    }

    /**
     * updateNodeDialog
     * <p>
     * This method will update an already constructed JDialog box that is already
     * being displayed by the GUI.
     *
     * @param JDialog
     */
    @Override
    public void updateNodeDialog(JDialog dialog) {
        // Cast the JDialog into our type
        DiscoveryDialog dsdvDlg = (DiscoveryDialog) dialog;
        dsdvDlg.updateInformation(this.CurrentTick, this.RouteTable);
    }

    /**
     * **************************************************************************
     * *** Public Member Functions - Implement Node Interface
     * **************************************************************************
     */

}
