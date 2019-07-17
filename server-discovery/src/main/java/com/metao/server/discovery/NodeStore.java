/**
 *
 */
package com.metao.server.discovery;

import com.metao.server.discovery.proto.Node;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Mehrdad Karami
 */
public class NodeStore {

    // Hash map of nodes.
    private HashMap<String, Node> store = new HashMap<String, Node>();


    public NodeAttributes getNodeAttributes(String nodeId) {
        Node node = store.get(nodeId);

        if (node == null) {
            return null;
        }
        return node.getAttributes();
    }

    public void clear() {
        store.clear();
    }


    public Node getNode(String nodeId) {
        return store.get(nodeId);

    }

    /**
     * Function will be add a Node to the NodeStore.
     * <p>
     * This function will add a node to the to the NodeStore. *
     *
     * @param node
     * @author Mehrdad Karami
     */

    public void addNode(Node node) {
        store.put(node.getAttributes().id, node);
    }

    /**
     * Function will be delete a Node from the NodeStore.
     * <p>
     * This function will delete a node from the to the NodeStore.
     *
     * @param id
     * @author Mehrdad Karami
     */
    public boolean deleteNode(String id) {
        return (store.remove(id) != null);
    }

    // Returns an iterator for all of the nodes in the store.
    public Iterator<Node> getNodes() {
        return store.values().iterator();
    }


}
