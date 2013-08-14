package org.ldg.mcpd;

import java.util.*;

public class InheritanceGraph {
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

    public List<String> getAncestors(final String child) {
        List<String> ancestors = new ArrayList<String>();

        Node root = nodes.get(child);

        if (root != null) {
            root.addAncestors(ancestors);
        }

        return ancestors;
    }

    public String getCommonAncestor(final String child1, final String child2) {
        Set<String> ancestors1 = new HashSet<String>(getAncestors(child1));

        if (ancestors1.contains(child2)) {
            return child2;
        }

        Set<Node> ancestors2 = new HashSet<Node>();
        ancestors2.add(nodes.get(child2));

        while (!ancestors2.isEmpty())
        {
            Set<Node> next_ancestors = new HashSet<Node>();
            for (Node ancestor : ancestors2) {
                if (ancestor == null) {
                    continue;
                }

                if (ancestors1.contains(ancestor.name)) {
                    return ancestor.name;
                }

                next_ancestors.addAll(ancestor.parents);
            }

            ancestors2 = next_ancestors;
        }

        return "java/lang/String";
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
