/**
 *
 */
package server.discovery;

import server.discovery.event.ServerDiscoveryEvent;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Mehrdad Karami
 */

public class OutputHandler {

    private static final CopyOnWriteArraySet<ServerDiscoveryEvent.EventType> filteredEvents = new CopyOnWriteArraySet<ServerDiscoveryEvent.EventType>();
    // List of consumers
    private static CopyOnWriteArrayList<OutputConsumer> consumers = new CopyOnWriteArrayList<OutputConsumer>();

    public static void removeOutputConsumer(OutputConsumer c) {
        consumers.remove(c);
    }

    public static void addOutputConsumer(OutputConsumer c) {
        consumers.add(c);
    }

    public static void dispatch(ServerDiscoveryEvent e) {
        //Apply event filter
        if (filteredEvents.contains(e.eventType)) {
            return;
        }
        for (OutputConsumer c : consumers) {
            c.consumeOutput(e);
        }
    }

    public static void addFilteredEvent(ServerDiscoveryEvent.EventType eType) {
        filteredEvents.add(eType);
    }

    public static void removeFilteredEvent(ServerDiscoveryEvent.EventType eType) {
        filteredEvents.remove(eType);
    }

}
