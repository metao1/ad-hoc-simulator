package server.discovery;

import server.discovery.event.ServerDiscoveryEvent;

/**
 * @author Mehrdad Karami
 */
public interface OutputConsumer {
    public void consumeOutput(ServerDiscoveryEvent e);
}
