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

import org.apache.jackrabbit.uuid.UUID;
import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Command for traversing to a node with the given uuid.
 */
public class CdUuid implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "cduuid";
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
        return "cduuid <uuid>";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "change directory to a node by uuid";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 2) {
            return true;
        }

        try {
            UUID uuid = new UUID(args[1]);
            if (!JcrWrapper.cduuid(uuid)) {
                System.out.println("UUID not found: " + args[1]);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid uuid format: " + args[1]);
        }
        return true;

    }
}
