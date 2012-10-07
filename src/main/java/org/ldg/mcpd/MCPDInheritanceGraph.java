package org.ldg.mcpd;

import java.util.*;

public class MCPDInheritanceGraph {
    private Map<String, Node> nodes = new HashMap<String, Node>();

    public void addRelationship(String parent, String child) {
        Node parentNode = nodes.get(parent);
        Node childNode = nodes.get(child);

        if (parentNode == null) {
            parentNode = new Node(parent, null);
            nodes.put(parent, parentNode);
        }

        if (childNode == null) {
            childNode = new Node(child, parentNode);
            parentNode.children.add(childNode);
            nodes.put(child, childNode);
        } else {
            if (childNode.parent == null) {
                childNode.parent = parentNode;
                parentNode.children.add(childNode);
            } else if (childNode.parent != parentNode) {
                System.out.println("ERROR: Bad inheritance.");
                System.out.println(childNode.name + " has two superclasses:");
                System.out.println(childNode.parent.name);
                System.out.println(parent);
                throw new RuntimeException("Bad inheritance.");
            } else {
                // Nothing to do; inheritance already recorded.
            }
        }
    }

    public List<String> getAncestors(String child) {
        List<String> ancestors = new ArrayList<String>();
        Node current = nodes.get(child);

        if (current == null) {
            return ancestors;
        }

        while (current.parent != null) {
            ancestors.add(current.parent.name);
            current = current.parent;
        }

        return ancestors;
    }

    public void print() {
        Node object = nodes.get("java/lang/Object");
        if (object == null) {
            System.out.println("No root node.");
        } else {
            object.print("");
        }
    }

    private class Node {
        public String name;
        public Node parent;
        public Set<Node> children = new HashSet<Node>();

        public Node(String name, Node parent) {
            this.name = name;
            this.parent = parent;
        }

        public void print(String prefix) {
            System.out.println(prefix + name);
            prefix += "  ";

            for (Node child : children) {
                child.print(prefix);
            }
        }
    }
}
