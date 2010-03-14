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
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * List child nodes of current node.
 */
public class MixinList implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "mixinlist";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "listmixins" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "mixinlist";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "list mixins of current node";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 1) {
            System.out.println(usage());
            System.out.println(help());
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }
        try {
            if (!node.hasProperty("jcr:mixinTypes")) {
                System.out.println("Node has no mixins.");
                return true;
            }

            for (Value val : node.getProperty("jcr:mixinTypes").getValues()) {
                System.out.println(val.getString());
            }
        } catch (RepositoryException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }
}
