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

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Copy a child node.
 */
public class NodeCopy implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "nodecopy";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "nodecp", "copynode" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "nodecopy <nodename> <target path>";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "nodecp node target: copy node to target";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 3) {
            System.out.println(help());
            return false;
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        String src = args[1];
        String dest = args[2];

        // tab completion slash
        if (src.endsWith("/")) {
            src = src.substring(0, src.length() - 1);
        }

        Node srcNode = null;
        try {
            if (src.startsWith("/")) {
                if (!node.getSession().getRootNode().hasNode(src.substring(1))) {
                    System.out.println("Src node not found: " + src);
                    return false;
                }
                srcNode = node.getSession().getRootNode().getNode(src.substring(1));
            } else {
                if (!node.hasNode(src)) {
                    System.out.println("Src node not found: " + src);
                    return false;
                }
                srcNode = node.getNode(src);
            }
            if (!dest.startsWith("/")) {
                if (node.getPath().equals("/")) {
                    dest = "/" + dest;
                } else {
                    dest = node.getPath() + "/" + dest;
                }
            }
            try {
                JcrWrapper.copyNode(srcNode, dest);
            } catch (ItemExistsException e) {
                System.out.println("Target already exists: " + dest);
            } catch (PathNotFoundException e) {
                System.out.println("Target not found: " + dest);
            }
        } catch (RepositoryException e1) {
            e1.printStackTrace();
        }

        return true;

    }

}
