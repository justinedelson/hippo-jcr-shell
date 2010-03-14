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
package org.hippoecm.tools.cli;

/**
 * Command interface. When invoked, execute is called.
 * The help text will be shown next to the command when
 * the 'help' command is issued.
 */
public interface Command {

    /**
     * Get the command string.
     * @return the command
     */
    String getCommand();

    /**
     * Get the command aliases.
     * @return a string array with the aliases
     */
    String[] getAliases();

    /**
     * Get the help message.
     * @return the help about the command
     */
    String help();

    /**
     * Get usage info.
     * @return the usage of the command
     */
    String usage();

    /**
     * Execute the command with the given command.
     * @param args the arguments for the command
     * @return true on successful execution
     */
    boolean execute(String[] args);
}
