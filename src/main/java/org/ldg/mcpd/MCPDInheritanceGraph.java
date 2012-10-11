package org.ldg.mcpd;

import java.util.*;

public class MCPDInheritanceGraph {
    private Map<String, Node> nodes = new HashMap<String, Node>();

    public void addRelationship(String parent, String child) {
        Node parentNode = nodes.get(parent);
        Node childNode = nodes.get(child);

        if (parentNode == null) {
            parentNode = new Node(parent);
            nodes.put(parent, parentNode);
        }

        if (childNode == null) {
            childNode = new Node(child);
            nodes.put(child, childNode);
        }

        parentNode.children.add(childNode);
        childNode.parents.add(parentNode);
    }

    public List<String> getAncestors(String child) {
        List<String> ancestors = new ArrayList<String>();

        Node root = nodes.get(child);

        if (root != null) {
            root.addAncestors(ancestors);
        }

        return ancestors;
    }

    private class Node {
        public String name;
        public Set<Node> parents = new HashSet<Node>();
        public Set<Node> children = new HashSet<Node>();

        public Node(String name) {
            this.name = name;
        }

        public void addAncestors(List<String> ancestors) {
            for (Node parent : parents) {
                if (!ancestors.contains(parent)) {
                    ancestors.add(parent.name);
                    parent.addAncestors(ancestors);
                }
            }
        }
    }
}
