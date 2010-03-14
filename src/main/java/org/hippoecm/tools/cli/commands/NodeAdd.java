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

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Add a child node to the current node.
 */
public class NodeAdd implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "nodeadd";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "addnode" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "nodeadd <nodename> [<type>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "add a child node, default type is nt:unstructured";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        String nodeName = args[1];
        // for tab completion remove trailing slash
        if (nodeName.endsWith("/")) {
            nodeName = nodeName.substring(1, nodeName.length());
        }
        String nodeType;
        if (args.length == 3) {
            nodeType = args[2];
        } else {
            nodeType = "nt:unstructured";
        }

        if (JcrWrapper.addNode(node, nodeName, nodeType)) {
            System.out.println("Node '" + nodeName + "' added.");
        } else {
            System.out.println("Failed to add node: " + nodeName);
        }

        return true;

    }

}
