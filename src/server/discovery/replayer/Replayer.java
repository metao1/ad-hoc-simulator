package server.discovery.replayer;

import server.discovery.InputHandler;
import server.discovery.OutputConsumer;
import server.discovery.OutputHandler;
import server.discovery.Utilities;
import server.discovery.event.ServerDiscoveryEvent;

import javax.swing.*;
import java.util.Iterator;
import java.util.Queue;

public class Replayer implements OutputConsumer {

    private final ReplayMode mode;
    private Queue<ServerDiscoveryEvent> replayEvents;
    private ReplayerListener replayerListener;
    private boolean isRunning = true;

    public Replayer(Queue<ServerDiscoveryEvent> replayEvents, ReplayerListener rl, ReplayMode mode) {
        this.mode = mode;

        //Add this as an output consumer
        OutputHandler.addOutputConsumer(this);
        this.replayEvents = replayEvents;
        this.replayerListener = rl;

        //Fire off events at the zero quantum.
        dispatchEventsAtQuantum(replayEvents, 0);
        rl.replayerStarted(replayEvents, this);

    }

    public static ReplayMode askReplayMode() {
        //Ask the user what type of replay they would like to run; locked or interactive
        Object answers[] = {"Locked (Default mode)", "Interactive"};
        int answer = JOptionPane.showOptionDialog(null,
                "Would you like to run the replay in interactive or locked mode?", "Select replay mode.", 0,
                JOptionPane.QUESTION_MESSAGE, null, answers, answers[0]);

        // Return null if the user closed the dialog box
        if (answer == JOptionPane.CLOSED_OPTION) {
            return null;
        }

        // Return their selection
        return ReplayMode.values()[answer];

    }

    public ReplayMode getMode() {
        return mode;
    }

    private void dispatchEventsAtQuantum(Queue<ServerDiscoveryEvent> Q, long quantum) {
        Iterator<ServerDiscoveryEvent> iter = Q.iterator();
        while (iter.hasNext()) {
            ServerDiscoveryEvent d = iter.next();

            //If this quantum is greater than specified quantum, break out
            //This assumes the list is ordered. If it isn't, change this routine!
            if (d.currentQuantum > quantum) {
                return;
            }

            //If this quantum is = to the specified quantum, pull it off the Q and dispatch it
            if (d.currentQuantum == quantum) {
                //If this is the STOP SIM event, finish and break out
                if (d.eventType == ServerDiscoveryEvent.EventType.IN_STOP_SIM) {
                    iter.remove();
                    finish();
                    break;
                }

                iter.remove();
                InputHandler.dispatch(d);
                continue;
            }

            //If we get here, theres a bug
            Utilities.showError("Impossible sequence in replayer dispatch events. Please file a bug report.");
            System.exit(1);
        }
    }

    private void finish() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        //Signal that the replayer is finished.
        replayerListener.replayerFinished(false);
        OutputHandler.removeOutputConsumer(this);
    }

    public void abort() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        //Signal that the replayer is finished with aborted flag.
        replayerListener.replayerFinished(true);
        OutputHandler.removeOutputConsumer(this);

    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void consumeOutput(ServerDiscoveryEvent e) {
        switch (e.eventType) {
            case OUT_QUANTUM_ELAPSED:
                dispatchEventsAtQuantum(this.replayEvents, e.currentQuantum - 1);
                break;
        }
    }

    public enum ReplayMode {LOCKED, INTERACTIVE}

    ;

    public interface ReplayerListener {
        void replayerStarted(Queue<ServerDiscoveryEvent> Q, Replayer instance);

        void replayerFinished(boolean aborted);
    }

}
