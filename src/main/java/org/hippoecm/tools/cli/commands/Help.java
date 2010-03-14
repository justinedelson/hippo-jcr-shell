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

import java.util.Arrays;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.CommandHelper;

/**
 * Help command.
 */
public class Help implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "help";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "?", "commands" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "help [<command>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "print general help message or the help of the command";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length == 1) {
            String[] commands = CommandHelper.getCommandsAsArray();
            Arrays.sort(commands);
            System.out.printf("%-20s%-60s\n", "Command", "Usage");
            System.out.printf("%-20s%-60s\n", "--------------", "--------------------");
            for (String command : commands) {
                System.out.printf("%-20s%-60s\n", command, CommandHelper.runMethod("usage", CommandHelper
                        .getClassForCommand(command)));
            }
            System.out.printf("%-20s%-60s\n", "--------------", "--------------------");
        } else if (args.length == 2) {
            String command = args[1];
            System.out.println("Usage: " + CommandHelper.runMethod("usage", CommandHelper.getClassForCommand(command)));
            System.out.println("   " + CommandHelper.runMethod("help", CommandHelper.getClassForCommand(command)));
        } else {
            System.out.println("Usage: " + usage());
            System.out.println("   " + help());
        }
        return true;
    }
}
