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

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * List child nodes of current node.
 */
public class NamespaceAdd implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "namespaceadd";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "addnamespace" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "namespaceadd <prefix> <uri>";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "Register a new namespace.";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 3) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }
        if (JcrWrapper.addNamespace(args[1], args[2])) {
            System.out.println("Namespace registered.");
            return true;
        } else {
            System.out.println("Failed to register namespace.");
            return false;
        }
    }
}
