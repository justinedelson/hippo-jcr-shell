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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Copy a child node.
 */
public class NodeExport implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "nodeexport";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "export" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "nodeexport <nodename> <xml file> [<skipBinaries>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "export the target node in xml format and store it into the file, default skipBinaries is false";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length != 3 && args.length != 4) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }

        boolean skipBinaries = false;

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        String src = args[1];
        String fileName = args[2];

        String srcAbsPath = null;

        // tab completion slash
        if (src.endsWith("/")) {
            src = src.substring(0, src.length() - 1);
        }

        try {
            if (src.startsWith("/")) {
                if (!node.getSession().getRootNode().hasNode(src.substring(1))) {
                    System.out.println("Src node not found: " + src);
                    return false;
                }
                srcAbsPath = node.getSession().getRootNode().getNode(src.substring(1)).getPath();
            } else {
                if (!node.hasNode(src)) {
                    System.out.println("Src node not found: " + src);
                    return false;
                }
                srcAbsPath = node.getNode(src).getPath();
            }

            File file = new File(fileName);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    try {
                        JcrWrapper.exportXml(srcAbsPath, bos, skipBinaries);
                    } finally {
                        bos.close();
                    }
                } finally {
                    fos.close();
                }
            } catch (IOException e) {
                System.out.println("Unablte to write to file '" + fileName + "': " + e.getMessage());
            }
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;

    }

}
