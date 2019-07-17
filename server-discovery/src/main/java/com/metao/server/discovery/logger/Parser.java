/**
 *
 */
package com.metao.server.discovery.logger;

import com.metao.server.discovery.event.ServerDiscoveryEvent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Mehrdad Karami
 */
public class Parser {

    static private boolean isReplayEvent(ServerDiscoveryEvent d) {
        switch (d.eventType) {
            case IN_ADD_NODE:
            case IN_DEL_NODE:
            case IN_MOVE_NODE:
            case IN_SET_NODE_RANGE:
            case IN_SET_NODE_PROMISCUITY:
            case IN_CLEAR_SIM:
            case IN_INSERT_MESSAGE:
            case IN_STOP_SIM:
                return true;
        }
        return false;
    }

    static private boolean isSetupEvent(ServerDiscoveryEvent d) {
        //Setup events are Q=0 and most IN_... types
        if (d.currentQuantum != 0) {
            return false;
        }

        if (isReplayEvent(d)) {
            return true;
        }

        return false;
    }

    private static BufferedReader getBufferedReader(String file) {
        //Open up the file
        FileReader LogFile = null;
        try {
            LogFile = new FileReader(file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return new BufferedReader(LogFile);
    }


    private static boolean isValidserverDiscoveryLogFile(String logFileLocation) {
        BufferedReader input = getBufferedReader(logFileLocation);
        String line = "";
        //Make sure this is a valid ServerDiscovery Log file by matching the first line
        //with the header of current serverDiscoveryEvents
        try {
            line = input.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            input.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!ServerDiscoveryEvent.getLogHeader().equals(line)) {

            return false;
        }
        return true;
    }

    public static Queue<ServerDiscoveryEvent> parseReplay(String logFileLocation) {
        BufferedReader input = getBufferedReader(logFileLocation);
        Queue<ServerDiscoveryEvent> Q = new LinkedList<ServerDiscoveryEvent>();
        String line = "";
        ServerDiscoveryEvent d;
        if (!isValidserverDiscoveryLogFile(logFileLocation)) {
            return null;
        }


        //Get every serverDiscoveryEvents in the file that is a Replay event.
        try {
            //Read past the first line
            line = input.readLine();

            while ((line = input.readLine()) != null) {
                d = ServerDiscoveryEvent.parseLogString(line);
                if (isReplayEvent(d)) {
                    Q.add(d);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Q;
    }

    public static Queue<ServerDiscoveryEvent> parseSetup(String logFileLocation) {
        BufferedReader input = getBufferedReader(logFileLocation);
        Queue<ServerDiscoveryEvent> Q = new LinkedList<ServerDiscoveryEvent>();
        ServerDiscoveryEvent d;
        String line;

        if (!isValidserverDiscoveryLogFile(logFileLocation)) {
            return null;
        }

        //Get every serverDiscoveryEvents in the file that is a setup event.
        try {
            //Read past the first line
            line = input.readLine();

            while ((line = input.readLine()) != null) {
                d = ServerDiscoveryEvent.parseLogString(line);
                if (isSetupEvent(d)) {
                    Q.add(d);
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Q;
    }

}
