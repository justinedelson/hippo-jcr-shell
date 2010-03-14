/*
 *  Copyright 2008 Hippo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.tools.cli.commands;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Print a node tree.
 */
public class NodeTree implements Command {

    private StringBuffer prefix = new StringBuffer();

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "nodetree";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "tree" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "nodetree [<levels>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "print a nodetree number of levels deep, default is 3";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }

        int maxLevel = 3;
        if (args.length == 2) {
            maxLevel = new Integer(args[1]).intValue();
        }
        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        try {
            printTree(node, 0, maxLevel, 0, 0);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    /**
     * Recursive print tree maxLevels deep
     * @param node start node
     * @param level current level
     * @param maxLevel max depth
     * @param childCount total number of childnodes of parent node
     * @param pos position of current childnode of parent node
     * @throws RepositoryException
     */
    private final void printTree(final Node node, final int level, final int maxLevel, final long childCount,
            final long pos) throws RepositoryException {
        if (level == (maxLevel + 1)) {
            // done..
            return;
        }
        StringBuffer buf = new StringBuffer();
        if (level > 0) {
            buf.append(prefix);
            if (pos == childCount) {
                buf.append("`--");
            } else {
                buf.append("|--");
            }
            buf.append(JcrWrapper.fullName(node));
            if (JcrWrapper.isVirtual(node)) {
                buf.append("*");
            }
            buf.append(" {").append(node.getPrimaryNodeType().getName()).append("}");
            System.out.println(buf.toString());

            if (pos == childCount) {
                prefix.append("   ");
            } else {
                prefix.append("|  ");
            }
        } else {
            // treat first node specifically
            buf.append(JcrWrapper.fullName(node));
            buf.append(" {").append(node.getPrimaryNodeType().getName()).append("}");
            System.out.println(buf.toString());
        }
        NodeIterator iter = node.getNodes();
        long size = iter.getSize();
        while (iter.hasNext()) {
            Node n = iter.nextNode();
            printTree(n, (level + 1), maxLevel, size, iter.getPosition());

        }
        if (level > 0) {
            prefix.delete(prefix.length() - 3, prefix.length());
        }
    }
}
