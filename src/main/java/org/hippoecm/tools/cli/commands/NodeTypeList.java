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

import java.util.Set;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * List child nodes of current node.
 */
public class NodeTypeList implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "nodetypelist";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "listnodetypes", "ntlist" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "nodetypelist [<primary|mixin>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "list registered nodetypes";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length > 2) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }
        Set<String> types = null;

        if (args.length == 2) {
            if ("mixin".equals(args[1])) {
                types = JcrWrapper.getNodeTypes("mixin");
            } else if ("primary".equals(args[1])) {
                types = JcrWrapper.getNodeTypes("primary");
            } else {
                System.out.println("Unknown option: " + args[1]);
                System.out.println(usage());
                System.out.println(help());
            }
        } else {
            types = JcrWrapper.getNodeTypes("all");
        }

        if (types == null) {
            System.out.println("Unable to get node types");
            return false;
        }

        for (String type : types) {
            System.out.println(type);
        }

        return true;

    }
}
