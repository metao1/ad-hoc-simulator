/**
 *
 */
package com.metao.server.discovery;

import com.metao.server.discovery.event.ServerDiscoveryEvent;

/**
 * @author Mehrdad Karami
 */
public interface InputConsumer {
    public void consumeInput(ServerDiscoveryEvent e);
}
