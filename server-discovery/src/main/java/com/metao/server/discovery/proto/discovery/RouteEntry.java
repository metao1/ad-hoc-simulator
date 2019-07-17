package com.metao.server.discovery.proto.discovery;

public class RouteEntry {

    /**
     * **************************************************************************
     * Constants needed by DISCOVERY Route Entry
     * **************************************************************************
     */


    /**
     * **************************************************************************
     * Private Member Fields
     * **************************************************************************
     */


    /**
     * Destination IP Address for the Destination Node
     */
    private String DestIP = "";
    /**
     * Destination Sequence Number
     */
    private int SeqNum;
    /**
     * Hop Count to the Destination Node
     */
    private int HopCount;
    /**
     * IP Address of the Next Hop Node on the path to the Destination Node.
     */
    private String NextHopIP = "";
    /**
     * Time that route was installed into the table.
     */
    private int InstTime;

    /**
     * **************************************************************************
     * *** Private Member Fields
     * **************************************************************************
     */

    /**
     * **************************************************************************
     * *** Public Member Functions
     * **************************************************************************
     */

    /**
     * Constructor with all fields defined.
     *
     * @param destIP
     * @param seqNum
     * @param hopCount
     * @param nextHopIP
     * @param instTime
     */
    RouteEntry(String destIP, int seqNum, int hopCount, String nextHopIP,
               int instTime) {
        super();
        DestIP = destIP;
        SeqNum = seqNum;
        HopCount = hopCount;
        NextHopIP = nextHopIP;
        InstTime = instTime;
    }

    /**
     * @return the destIP
     */
    public String getDestIP() {
        return DestIP;
    }

    /**
     * @param destIP the destIP to set
     */
    public void setDestIP(String destIP) {
        DestIP = destIP;
    }

    /**
     * @return the seqNum
     */
    public int getSeqNum() {
        return SeqNum;
    }

    /**
     * @param seqNum the seqNum to set
     */
    public void setSeqNum(int seqNum) {
        SeqNum = seqNum;
    }

    /**
     * @return the hopCount
     */
    public int getHopCount() {
        return HopCount;
    }

    /**
     * @param hopCount the hopCount to set
     */
    public void setHopCount(int hopCount) {
        HopCount = hopCount;
    }

    /**
     * @return the nextHopIP
     */
    public String getNextHopIP() {
        return NextHopIP;
    }

    /**
     * @param nextHopIP the nextHopIP to set
     */
    public void setNextHopIP(String nextHopIP) {
        NextHopIP = nextHopIP;
    }

    /**
     * @return the instTime
     */
    public int getInstTime() {
        return InstTime;
    }

    /**
     * @param instTime the instTime to set
     */
    public void setInstTime(int instTime) {
        InstTime = instTime;
    }


}
