package com.metao.server.discovery.proto;

import com.metao.server.discovery.NodeAttributes;
import com.metao.server.discovery.proto.discovery.DiscoveryNode;

/*
 * Node Factory class.
 * Instantiates a new node based on a supplied node type. Also contains
 * the definitions for node types (NodeType) that are used throughout ServerDiscovery.
 *
 * This class must be changed if you want to add a new network protocol.
 *
 */
public class NodeFactory {
    public static Node makeNewNode(NodeType nt, NodeAttributes na) {
        if (nt == null || na == null) {
            return null;
        }

        switch (nt) {
            case DISCOVERY:
                return new DiscoveryNode(na);
            // Create nodes of the new NodeType.
            // case NewProtoName : return NewProto(na);
            default:
                return null;
        }
    }

    ;

    public enum NodeType {
        DISCOVERY
    }
}
