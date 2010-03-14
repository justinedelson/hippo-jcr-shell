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
 * Delete property from current node.
 */
public class PropDelete implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "propdelete";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "deleteprop" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "propdelete <property> [<property> [..]]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "delete properties from the current node";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length < 2) {
            System.out.println(help());
            return false;
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        for (int i = 1; i < args.length; i++) {
            try {
                final String propName = args[i];
                if (!node.hasProperty(propName)) {
                    System.out.println("Node doesn't have a property with name: " + propName);
                    continue;
                }
                node.getProperty(propName).remove();
                System.out.println("Property '" + propName + "' removed.");

                //TODO, shouldn't be handling cache inside command, move to JcrWrapper
                JcrWrapper.removeFromCache(node.getPath());
            } catch (RepositoryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;

    }

}
