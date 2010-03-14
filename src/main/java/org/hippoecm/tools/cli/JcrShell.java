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

import java.io.IOException;

import org.hippoecm.tools.cli.Terminal.ShutdownHook;

/**
 * Main wrapper to start the shell.
 */
public final class JcrShell {

    /** the commands. */
    private static final String[] COMMAND_CLASSES = new String[] { "org.hippoecm.tools.cli.commands.Aliases",
            "org.hippoecm.tools.cli.commands.Cd", "org.hippoecm.tools.cli.commands.CdUuid",
            "org.hippoecm.tools.cli.commands.CdPrevious", "org.hippoecm.tools.cli.commands.Credentials",
            "org.hippoecm.tools.cli.commands.Exit", "org.hippoecm.tools.cli.commands.FindUuid",
            "org.hippoecm.tools.cli.commands.FindReferences", "org.hippoecm.tools.cli.commands.Help",
            "org.hippoecm.tools.cli.commands.Login", "org.hippoecm.tools.cli.commands.Logout",
            "org.hippoecm.tools.cli.commands.MixinAdd", "org.hippoecm.tools.cli.commands.MixinList",
            "org.hippoecm.tools.cli.commands.MixinRemove", "org.hippoecm.tools.cli.commands.NamespaceAdd",
            "org.hippoecm.tools.cli.commands.NamespaceList", "org.hippoecm.tools.cli.commands.NamespaceRemove",
            "org.hippoecm.tools.cli.commands.NodeAdd", "org.hippoecm.tools.cli.commands.NodeAllowed",
            "org.hippoecm.tools.cli.commands.NodeCopy", "org.hippoecm.tools.cli.commands.NodeExport",
            "org.hippoecm.tools.cli.commands.NodeImport", "org.hippoecm.tools.cli.commands.NodeList",
            "org.hippoecm.tools.cli.commands.NodeMove", "org.hippoecm.tools.cli.commands.NodeRemove",
            "org.hippoecm.tools.cli.commands.NodeTree", "org.hippoecm.tools.cli.commands.NodeTypeList",
            "org.hippoecm.tools.cli.commands.NodeTypeGet", "org.hippoecm.tools.cli.commands.PropAdd",
            "org.hippoecm.tools.cli.commands.PropAllowed", "org.hippoecm.tools.cli.commands.PropDelete",
            "org.hippoecm.tools.cli.commands.PropGet", "org.hippoecm.tools.cli.commands.PropList",
            "org.hippoecm.tools.cli.commands.PropSet", "org.hippoecm.tools.cli.commands.Query",
            "org.hippoecm.tools.cli.commands.Refresh", "org.hippoecm.tools.cli.commands.Repository",
            "org.hippoecm.tools.cli.commands.Reset", "org.hippoecm.tools.cli.commands.Save",
            "org.hippoecm.tools.cli.commands.Server", "org.hippoecm.tools.cli.commands.SessionSave",
            "org.hippoecm.tools.cli.commands.Status", "org.hippoecm.tools.cli.commands.ValueAdd",
            "org.hippoecm.tools.cli.commands.ValueRemove" };

    /**
     * Private constructor.
     */
    private JcrShell() {
        super();
    }

    /**
     * The main method to start the jcr shell.
     * @param args ignored
     * @throws IOException io failure when starting shell
     */
    public static void main(final String[] args) throws IOException {
        // initialize the command line object.
        Terminal term = new Terminal();
        term.setCommandLinePrompt(JcrWrapper.NOT_CONNECTED_PROMPT);
        term.setCommandLineVersion("JCR Command Shell v.1.01.00\nCreated by Bart van der Schans <schans@onehippo.com>");

        // add commands
        for (String clazz : COMMAND_CLASSES) {
            CommandHelper.registerCommandClass(clazz);
        }

        // register hook for proper shutdown
        ShutdownHook sh = term.getShutdownHook();
        Runtime.getRuntime().addShutdownHook(sh);

        // start terminal
        JcrWrapper.setTerminal(term);
        term.init();

    }
}
