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
 * Save the session.
 */
public class SessionSave implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "sessionsave";
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
        return "sessionsave";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "save the current session";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (JcrWrapper.save()) {
            System.out.println("Session saved.");
            return true;
        } else {
            System.out.println("Failed to save session.");
            return false;
        }
    }
}
