/**
 *
 */
package server.discovery.event;

import server.discovery.Message;
import server.discovery.NodeAttributes;
import server.discovery.SimulationTimeKeeper;
import server.discovery.Utilities;
import server.discovery.proto.NodeFactory;
import server.discovery.proto.NodeFactory.NodeType;

import java.lang.reflect.Field;

/**
 * @author Mehrdad Karami
 */
public class ServerDiscoveryEvent {
    static final Class<ServerDiscoveryEvent> c = ServerDiscoveryEvent.class;
    static final Field[] fields = c.getFields();

    ;
    private static String newline = System.getProperty("line.separator");
    private static SimulationTimeKeeper simTimeKeeper;
    public EventType eventType;
    public String nodeId;
    public String sourceId;
    public String destinationId;
    public String informationalMessage;
    public String transmittedMessage;
    public int newSimSpeed;
    public int nodeX;
    public int nodeY;
    public int nodeRange;
    public NodeFactory.NodeType nodeType;
    public long currentQuantum;
    public boolean isPromiscuous;


    // Hide the default constructor. serverDiscoveryEvents can only be made through the
    // supplied functions that follow.
    private ServerDiscoveryEvent() {
        //If the time keeper is set, view the current time from it.
        if (simTimeKeeper != null) {
            currentQuantum = simTimeKeeper.getTime();
        }
    }

    public static void setSimTimeKeeper(SimulationTimeKeeper s) {
        simTimeKeeper = s;
    }

    public static ServerDiscoveryEvent inInsertMessage(Message message) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.transmittedMessage = message.message;
        e.sourceId = message.originId;
        e.destinationId = message.destinationId;
        e.eventType = EventType.IN_INSERT_MESSAGE;
        return e;
    }

    public static ServerDiscoveryEvent outInsertMessage(String sourceID, String destID, String message) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_INSERT_MESSAGE;
        e.informationalMessage = "User message inserted into the network. Source ID: " + sourceID + " Dest ID: " + destID + " Message: " + message;
        return e;
    }

    public static ServerDiscoveryEvent inClearSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_CLEAR_SIM;
        return e;
    }

    public static ServerDiscoveryEvent inNewSim(NodeType nt) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_NEW_SIM;
        e.nodeType = nt;
        return e;
    }

    public static ServerDiscoveryEvent outClearSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_CLEAR_SIM;
        e.informationalMessage = "Nodes cleared.";
        return e;
    }

    public static ServerDiscoveryEvent inStartSim(int simSpeed) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_START_SIM;
        e.newSimSpeed = simSpeed;
        return e;
    }

    public static ServerDiscoveryEvent outStartSim(int simSpeed) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_START_SIM;
        e.newSimSpeed = simSpeed;
        e.informationalMessage = "Simulation Started.";
        return e;
    }

    public static ServerDiscoveryEvent inStopSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_STOP_SIM;
        return e;
    }

    public static ServerDiscoveryEvent outStopSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_STOP_SIM;
        e.informationalMessage = "Simulation Stopped.";
        return e;
    }

    public static ServerDiscoveryEvent outNewSim(NodeType nt) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_NEW_SIM;
        e.nodeType = nt;
        e.informationalMessage = "New " + nt + " Simulation Created.";
        return e;
    }

    public static ServerDiscoveryEvent inPauseSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_PAUSE_SIM;
        return e;
    }

    public static ServerDiscoveryEvent outPauseSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_PAUSE_SIM;
        e.informationalMessage = "Simulation Paused.";
        return e;
    }

    public static ServerDiscoveryEvent inResumeSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_RESUME_SIM;
        return e;
    }

    public static ServerDiscoveryEvent outResumeSim() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_RESUME_SIM;
        e.informationalMessage = "Simulation Resumed.";
        return e;
    }

    public static ServerDiscoveryEvent inAddNode(int x, int y, int range, boolean isPromiscuous) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_ADD_NODE;
        e.nodeX = x;
        e.nodeY = y;
        e.nodeRange = range;
        e.isPromiscuous = isPromiscuous;
        return e;
    }

    public static ServerDiscoveryEvent inDeleteNode(String id) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_DEL_NODE;
        e.nodeId = id;
        return e;
    }

    public static ServerDiscoveryEvent inSetNodeRange(String id, int newRange) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_SET_NODE_RANGE;
        e.nodeId = id;
        e.nodeRange = newRange;
        return e;
    }

    public static ServerDiscoveryEvent inSimSpeed(int newSpeed) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_SIM_SPEED;
        e.newSimSpeed = newSpeed;
        return e;
    }

    public static ServerDiscoveryEvent outSimSpeed(int newSpeed) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_SIM_SPEED;
        e.newSimSpeed = newSpeed;
        e.informationalMessage = "Simulation Speed Set: " + newSpeed + ".";
        return e;
    }

    public static ServerDiscoveryEvent inSetNodePromiscuity(String id, boolean isPromiscuous) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_SET_NODE_PROMISCUITY;
        e.isPromiscuous = isPromiscuous;
        e.nodeId = id;
        return e;
    }

    public static ServerDiscoveryEvent inMoveNode(String id, int x, int y) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.IN_MOVE_NODE;
        e.nodeId = id;
        e.nodeX = x;
        e.nodeY = y;
        return e;
    }

    public static ServerDiscoveryEvent outAddNode(NodeAttributes n) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_ADD_NODE;
        d.setNodeAttributes(n);
        d.informationalMessage = "Server Added: " + n.id + ".";
        return d;

    }

    public static ServerDiscoveryEvent outMoveNode(String id, int x, int y) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_MOVE_NODE;
        d.nodeId = id;
        d.nodeX = x;
        d.nodeY = y;
        d.informationalMessage = "server " + id + " moved to X:" + x + " Y:" + y + ".";
        return d;
    }

    public static ServerDiscoveryEvent outDeleteNode(String id) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_DEL_NODE;
        d.nodeId = id;
        d.informationalMessage = "Node Deleted: " + id;
        return d;
    }

    public static ServerDiscoveryEvent outSetNodeRange(String id, int newRange) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_SET_NODE_RANGE;
        e.nodeId = id;
        e.nodeRange = newRange;
        e.informationalMessage = "server " + id + "'s range changed to " + newRange + ".";
        return e;
    }

    public static ServerDiscoveryEvent outSetNodePromiscuity(String id, boolean isPromiscuous) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_SET_NODE_PROMISCUITY;
        e.isPromiscuous = isPromiscuous;
        String status;
        if (isPromiscuous) status = "enabled";
        else status = "disabled";
        e.informationalMessage = "server " + id + " " + status + " promiscuous mode.";
        return e;
    }

    public static ServerDiscoveryEvent outMsgRecieved(String sourceId, String destId, String message) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_MSG_RECEIVED;
        e.sourceId = sourceId;
        e.destinationId = destId;
        e.transmittedMessage = message;
        e.informationalMessage = "Node " + sourceId + " successfully sent a message to Node " + destId;
        return e;
    }

    public static ServerDiscoveryEvent outMsgTransmitted(String sourceId, String destId, String message) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_MSG_TRANSMITTED;
        e.sourceId = sourceId;
        e.destinationId = destId;
        e.transmittedMessage = message;
        e.informationalMessage = "Node " + sourceId + " transmitted a message to Node " + destId;
        return e;
    }

    public static ServerDiscoveryEvent outError(String informationalMessage) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_ERROR;
        d.informationalMessage = informationalMessage;
        return d;
    }

    public static ServerDiscoveryEvent outDebug(String informationalMessage) {

        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_DEBUG;
        d.informationalMessage = informationalMessage;
        return d;
    }

    static ServerDiscoveryEvent outInformation(String informationalMessage) {
        // stub
        return new ServerDiscoveryEvent();
    }

    public static ServerDiscoveryEvent outControlMsgTransmitted(String sourceId, Message msg) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_CONTROLMSG_TRANSMITTED;
        d.sourceId = msg.originId;
        d.destinationId = msg.destinationId;
        d.transmittedMessage = msg.message;
        d.informationalMessage = d.sourceId + " transmitted control message to " + d.destinationId + " : " + msg.message;
        return d;
    }

    public static ServerDiscoveryEvent outNarrMsgTransmitted(String sourceId, Message msg) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_NARRMSG_TRANSMITTED;
        d.sourceId = msg.originId;
        d.destinationId = msg.destinationId;
        d.transmittedMessage = msg.message;
        d.informationalMessage = d.sourceId + " transmitted narrative message to " + d.destinationId + " : " + msg.message;
        return d;
    }

    public static ServerDiscoveryEvent outControlMsgReceived(String sourceId, Message msg) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_CONTROLMSG_RECEIVED;
        d.sourceId = msg.originId;
        d.destinationId = sourceId;
        d.transmittedMessage = msg.message;
        d.informationalMessage = d.sourceId + " received control message from " + d.destinationId + " : " + msg.message;
        return d;
    }

    public static ServerDiscoveryEvent outNarrMsgReceived(String sourceId, Message msg) {
        ServerDiscoveryEvent d = new ServerDiscoveryEvent();
        d.eventType = EventType.OUT_NARRMSG_RECEIVED;
        d.sourceId = msg.originId;
        d.destinationId = sourceId;
        d.transmittedMessage = msg.message;
        d.informationalMessage = d.sourceId + " received narrative message from " + d.destinationId + " : " + msg.message;
        return d;
    }

    public static ServerDiscoveryEvent outNodeInfo(String infoMsg) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_NODE_INFO;
        e.informationalMessage = infoMsg;
        return e;
    }

    public static ServerDiscoveryEvent outQuantumElapsed() {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        e.eventType = EventType.OUT_QUANTUM_ELAPSED;
        return e;
    }

    public static String getLogHeader() {
        // use reflection to get each field name
        Class<ServerDiscoveryEvent> c = ServerDiscoveryEvent.class;
        Field[] fields = c.getFields();
        String ret = "";
        for (Field f : fields) {
            ret += f.getName() + ",";
        }
        // remove trailing comma
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    private static EventType getEventTypeFromString(String str) {
        // use reflection to get each field
        for (EventType e : EventType.values()) {
            if (e.toString().equals(str)) return e;
        }
        return null;
    }

    public static NodeType parseNodeType(String str) {
        //Get each possible node type
        NodeType[] nTypes = Utilities.getNodeTypes();

        //For each nType..
        for (NodeType nt : nTypes) {
            if (str.equals(nt.toString())) {
                return nt;
            }
        }

        //No match
        return null;
    }

    public static ServerDiscoveryEvent parseLogString(String lineEvent) {
        ServerDiscoveryEvent e = new ServerDiscoveryEvent();
        try {
            String[] details = lineEvent.split(",");

            e.eventType = getEventTypeFromString(details[0]);
            if (e.eventType == null) {
                //Must have event type field.
                return null;
            }

            e.nodeId = details[1];
            e.sourceId = details[2];
            e.destinationId = details[3];
            e.informationalMessage = details[4];
            e.transmittedMessage = details[5];
            e.newSimSpeed = Integer.parseInt(details[6]);
            e.nodeX = Integer.parseInt(details[7]);
            e.nodeY = Integer.parseInt(details[8]);
            e.nodeRange = Integer.parseInt(details[9]);
            e.nodeType = parseNodeType(details[10]);
            e.currentQuantum = Long.parseLong(details[11]);
            e.isPromiscuous = Boolean.parseBoolean(details[12]);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return e;
    }

    //Provided for convenience.
    public NodeAttributes getNodeAttributes() {
        return new NodeAttributes(nodeId, nodeX, nodeY, nodeRange, isPromiscuous);
    }

    //Provided for convenience.
    public void setNodeAttributes(NodeAttributes n) {
        nodeX = n.x;
        nodeY = n.y;
        nodeRange = n.range;
        nodeId = n.id;
        isPromiscuous = n.isPromiscuous;
    }

    public String getLogString() {
        StringBuilder sb = new StringBuilder();
        // proposed format of log string is:
        // comma separated values, with public fields of ServerDiscoveryEvent printed out in
        // order

        //Trunc the string builder
        sb.setLength(0);

        for (Field f : fields) {
            Object obj;
            try {
                obj = f.get(this);
                if (obj != null) {
                    sb.append(obj.toString() + ",");
                } else {
                    sb.append(",");
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Utilities.showError("An error occurred while trying to serialize an event. Please file a bug report");
                System.exit(1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Utilities.showError("An error occurred while trying to serialize an event. Please file a bug report");
                System.exit(1);
            }

        }
        // remove trailing comma
        sb.deleteCharAt(sb.length() - 1);

        // add a newline char
        sb.append(newline);
        return sb.toString();
    }


    public enum EventType {
        // Input event types
        IN_ADD_NODE, IN_MOVE_NODE, IN_DEL_NODE, IN_SET_NODE_RANGE, IN_SET_NODE_PROMISCUITY, IN_SIM_SPEED,
        IN_START_SIM, IN_PAUSE_SIM, IN_RESUME_SIM, IN_STOP_SIM, IN_CLEAR_SIM, IN_NEW_SIM, IN_INSERT_MESSAGE,

        // Output event types
        OUT_ADD_NODE, OUT_MOVE_NODE, OUT_DEL_NODE, OUT_SET_NODE_RANGE, OUT_SET_NODE_PROMISCUITY,
        OUT_MSG_TRANSMITTED, OUT_DEBUG, OUT_ERROR, OUT_START_SIM, OUT_PAUSE_SIM, OUT_RESUME_SIM,
        OUT_STOP_SIM, OUT_SIM_SPEED, OUT_NEW_SIM, OUT_INSERT_MESSAGE, OUT_NARRMSG_RECEIVED,
        OUT_CONTROLMSG_RECEIVED, OUT_NARRMSG_TRANSMITTED, OUT_CONTROLMSG_TRANSMITTED,
        OUT_QUANTUM_ELAPSED, OUT_CLEAR_SIM, OUT_MSG_RECEIVED, OUT_NODE_INFO
    }

}
