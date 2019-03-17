package server.discovery.console;

import server.discovery.OutputConsumer;
import server.discovery.event.ServerDiscoveryEvent;

public class Console implements OutputConsumer {

    @Override
    public void consumeOutput(ServerDiscoveryEvent e) {
        System.out.println(e.getLogString());
    }

}
