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
 * Command for traversing through the hierarchy.
 */
public class Cd implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "cd";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] {};
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "cd [<path>|<reference property>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "change directory";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length < 2) {
            System.out.println(help());
            System.out.println(usage());
            return true;
        }
        StringBuilder path = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                path.append(" ");
            }
            path.append(args[i]);
        }
        if (!JcrWrapper.cd(path.toString())) {
            System.out.println("Path not found: " + path.toString());
        }
        return true;
    }
}
