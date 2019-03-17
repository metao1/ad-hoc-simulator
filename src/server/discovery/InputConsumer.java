/**
 *
 */
package server.discovery;

import server.discovery.event.ServerDiscoveryEvent;

/**
 * @author Mehrdad Karami
 */
public interface InputConsumer {
    public void consumeInput(ServerDiscoveryEvent e);
}
