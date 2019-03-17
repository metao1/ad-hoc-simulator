package server.discovery;

public final class NodeAttributes {
    public final String id;
    public final int x;
    public final int y;
    public final int range;
    public final boolean isPromiscuous;

    public NodeAttributes(String id, int x, int y, int range, boolean isPromiscuous) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.range = range;
        this.isPromiscuous = isPromiscuous;
    }

    //Copy constructor
    public NodeAttributes(NodeAttributes ni) {
        this(ni.id, ni.x, ni.y, ni.range, ni.isPromiscuous);
    }

    // Hide the no arg constructor.
    @SuppressWarnings("unused")
    private NodeAttributes() {
        this.id = "";
        this.x = 0;
        this.y = 0;
        this.range = 0;
        this.isPromiscuous = false;
    }

}
