package com.metao.server.discovery.console;

import com.metao.server.discovery.OutputConsumer;
import com.metao.server.discovery.event.ServerDiscoveryEvent;

public class Console implements OutputConsumer {

    @Override
    public void consumeOutput(ServerDiscoveryEvent e) {
        System.out.println(e.getLogString());
    }

}
