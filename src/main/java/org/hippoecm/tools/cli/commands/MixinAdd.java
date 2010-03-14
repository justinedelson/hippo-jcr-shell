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

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Add a child node to the current node.
 */
public class MixinAdd implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "mixinadd";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "addmixin" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "mixinadd <mixin type>";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "add a mixin to the current node";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 2) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        String mixinName = args[1];

        try {
            node.addMixin(mixinName);
            //TODO, shouldn't be handling cache inside command, move to JcrWrapper
            JcrWrapper.removeFromCache(node.getPath());
        } catch (RepositoryException e) {
            System.out.println("Failed to add mixin: " + e.getMessage());
            return false;
        }
        return true;

    }

}
