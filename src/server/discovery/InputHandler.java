/**
 *
 */
package server.discovery;

import server.discovery.event.ServerDiscoveryEvent;

import java.util.ArrayList;

/**
 * @author Mehrdad Karami
 */
public class InputHandler {
    private static final ArrayList<InputConsumer> inputConsumers = new ArrayList<InputConsumer>();

    public static void dispatch(ServerDiscoveryEvent e) {
        for (InputConsumer c : inputConsumers) {
            c.consumeInput(e);
        }
    }

    public static void addInputConsumer(InputConsumer c) {
        inputConsumers.add(c);
    }

    public static void removeInputConsumer(InputConsumer c) {
        inputConsumers.remove(c);
    }

}


