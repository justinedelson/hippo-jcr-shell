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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.History;
import jline.MultiCompletor;
import jline.SimpleCompletor;

/**
 * Manage the terminal (with jline).
 */
public class Terminal {

    /**
     * Displayed when the program first starts. Provides for customization
     * option.
     */
    private String commandLineHeader = null;

    /**
     * Provides an unchanging prompt for each line of input. Provides for
     * customization option.
     */
    private String commandLinePrompt = "> ";

    /**
     * Displayed when the program first starts. Provides for customization
     * option.
     */
    private String commandLineVersion = null;

    /**
     * Jline history file name.
     */
    private static final String HISTORYFILE = ".javaterm"; // history file in user's

    /**
     * Jline history file.
     */
    private String historyFile;

    /**
     * The console reader from jline.
     */
    private static ConsoleReader consoleReader;

    /**
     * Main entry point. The first argument can be a filename with an
     * application initialization file.
     * @throws IOException when the interaction with the shell fails
     */
    public final void init() throws IOException {
        String line;

        historyFile = System.getProperty("user.home") + File.separator + HISTORYFILE;

        consoleReader = new ConsoleReader();
        consoleReader.setHistory(new History(new File(historyFile)));

        // set completer with list of words
        MultiCompletor commandComp = new MultiCompletor(new Completor[] {
                new SimpleCompletor(CommandHelper.getCommandsAsArray()),
                new SimpleCompletor(CommandHelper.getAliasesAsArray()) });
        MultiCompletor paramComp = new MultiCompletor(new Completor[] { new NodeNameCompletor(),
                new PropertyNameCompletor() });
        Completor[] comp = new Completor[] { commandComp, paramComp };
        consoleReader.addCompletor(new ArgumentCompletor(comp));

        if (getCommandLineHeader() != null) {
            Terminal.println(getCommandLineHeader());
        }
        if (getCommandLineVersion() != null) {
            Terminal.println(getCommandLineVersion());
        }
        Terminal.println("exit or quit leaves program.");
        Terminal.println("help lists commands.");

        boolean keepRunning = true;
        // main input loop
        while (keepRunning) {
            try {
                line = consoleReader.readLine(getCommandLinePrompt());
                if (line != null) {
                    handleCommand(line, consoleReader);
                } else {
                    // Ctrl-D, do proper exit
                    handleCommand("exit", consoleReader);
                }
            } catch (JcrShellShutdownException e) {
                // thrown by exit command
                keepRunning = false;
            } catch (java.io.EOFException eof) {
                keepRunning = false;
            } catch (UnsupportedEncodingException enc) {
                enc.printStackTrace(System.err);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }
        // cleanup
        JcrWrapper.logout();
        // clear line
        consoleReader.printString("Bye bye!");
    }

    /**
     * Parse and handle command line.
     * @param line the command line
     * @param cr the console reader (jline)
     * @return true if the command line was succesful handled and executed
     * @throws IOException when the interaction with the shell fails
     */
    private boolean handleCommand(final String line, final ConsoleReader cr) throws IOException {
        long tickStart = System.currentTimeMillis();
        boolean retValue = true;

        String cmdLine = line.trim();
        // # = comment
        if (cmdLine.length() == 0) {
            return true;
        }
        if (cmdLine.startsWith("#")) {
            return true;
        }
        String[] args = tokenizeCommand(cmdLine);

        // white space
        if (args.length == 0) {
            return true;
        }

        String cmd = args[0].trim().toLowerCase();
        if (CommandHelper.isAlias(cmd)) {
            cmd = CommandHelper.getCommandForAlias(cmd);
        }

        if (!CommandHelper.isCommand(cmd)) {
            Terminal.println("Unknown command: " + cmd);
            return false;
        }

        String classToInvoke = CommandHelper.getClassForCommand(cmd);

        try {
            retValue = ((Boolean) CommandHelper.runMethod("execute", classToInvoke, args)).booleanValue();
        } catch (Exception e) {
            // shutdown 'exception'
            if (e instanceof JcrShellShutdownException) {
                throw new JcrShellShutdownException();
            }
            e.printStackTrace();
            Terminal.println("Error Running: [" + classToInvoke + "] with [" + Arrays.toString(args) + "]");
        }
        Terminal.println("  completed: " + (System.currentTimeMillis() - tickStart) + " msecs");
        return retValue;
    }

    /**
     * Tokenize the command line on whitespace.
     * @param line commandline
     * @return String array with tokens
     */
    private String[] tokenizeCommand(final String line) {
        String[] tokens = line.split("\\s+");
        return tokens;
    }

    /**
     * Getter for commandLineHeader variable.
     * @return the command line header.
     */
    public final String getCommandLineHeader() {
        return commandLineHeader;
    }

    /**
     * Getter for commandLinePrompt variable.
     * @return the command line prompt.
     */
    public final String getCommandLinePrompt() {
        return commandLinePrompt;
    }

    /**
     * Getter for commandLineVersion variable.
     * @return the command line version.
     */
    public final String getCommandLineVersion() {
        return commandLineVersion;
    }

    /**
     * Setter for the commandLineHeader variable.
     * @param string The command line header.
     */
    public final void setCommandLineHeader(final String string) {
        commandLineHeader = string;
    }

    /**
     * Setter for the commandLinePrompt variable.
     * @param string The prompt.
     */
    public final void setCommandLinePrompt(final String string) {
        commandLinePrompt = string;
    }

    /**
     * Setter for the commandLineVersion variable.
     * @param string The version.
     */
    public final void setCommandLineVersion(final String string) {
        commandLineVersion = string;
    }

    /**
     * Get the custom shutdown hook.
     * @return the shutdown hook
     */
    public final ShutdownHook getShutdownHook() {
        return new ShutdownHook();
    }

    /**
     * Print string to console, fall back to System.out.
     * @param str string to print
     */
    public static final void print(final String str) {
        try {
            consoleReader.printString(str);
        } catch (IOException e) {
            // ugh..
            System.out.print(str);
        }
    }

    /**
     * Read password from terminal with masking.
     * @return the entered password
     */
    public static final String getPassword() {
        try {
            return consoleReader.readLine("password: ", '*');
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Print string to console, fall back to System.out.
     * @param str string to print
     */
    public static final void println(final String str) {
        try {
            consoleReader.printString(str);
            consoleReader.printNewline();
        } catch (IOException e) {
            // ugh..
            System.out.println(str);
        }
    }

    /**
     * Print new line to console, fall back to System.out.
     */
    public static final void newLine() {
        println("");
    }

    /**
     * Trivial shutdown hook class.
     */
    static class ShutdownHook extends Thread {
        /**
         * Exit properly on shutdown.
         */
        public void run() {
            try {
                Terminal.newLine();
                Terminal.print("Shuting down..");
                JcrWrapper.logout();
                Terminal.println("done.");
            } catch (JcrShellShutdownException e) {
                // ignore, expected, thrown by exit command
            }
        }
    }
}
