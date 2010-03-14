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
 * List child nodes of current node.
 */
public class NodeList implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "ls";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "dir", "nodelist" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "ls [<path>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "list child nodes of the current node";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length < 1) {
            System.out.println(usage());
            System.out.println(help());
            return true;
        }
        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        StringBuilder path = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                path.append(" ");
            }
            path.append(args[i]);
        }
        
        NodeIterator iter = JcrWrapper.getNodes(path.toString());
        if (iter == null) {
            System.out.println("Path not found: " + path);
            return false;
        }
        try {
            System.out.printf("%-40s%s\n", "Name", "Type");
            System.out.printf("%-40s%s\n", "--------------------", "--------------------");
            while (iter.hasNext()) {
                Node n = iter.nextNode();
                if (JcrWrapper.isVirtual(n)) {
                    System.out.printf("%-40s%s\n", JcrWrapper.fullName(n) + "*", n.getPrimaryNodeType().getName());
                } else {
                    System.out.printf("%-40s%s\n", JcrWrapper.fullName(n), n.getPrimaryNodeType().getName());
                }
            }
            System.out.printf("%-40s%s\n", "--------------------", "--------------------");
            System.out.printf("Total: %s\n", iter.getSize());
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return true;

    }
}
