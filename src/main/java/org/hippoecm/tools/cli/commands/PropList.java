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
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * List properties of current node.
 */
public class PropList implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "proplist";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "listprops", "list" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "proplist";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "show a list of properties of the current node";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length > 2) {
            System.out.println(usage());
            System.out.println(help());
            return true;
        }
        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        String path = null;
        if (args.length == 2) {
            path = args[1];
        }

        PropertyIterator iter = JcrWrapper.getProperties(path);
        if (iter == null) {
            System.out.println("Path not found: " + path);
            return false;
        }

        try {
            System.out.printf("%-28s%-12s%s\n", "Name", "Type", "Value");
            System.out.printf("%-28s%-12s%s\n", "--------------------", "--------", "--------------------");
            while (iter.hasNext()) {
                Property p = iter.nextProperty();
                System.out.printf("%-28s%-12s%s\n", JcrWrapper.fullName(p), PropertyType.nameFromValue(p.getType()),
                        printValue(p));
            }
            System.out.printf("%-28s%-12s%s\n", "--------------------", "--------", "--------------------");
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Helper method for pretty printing property list.
     * @param p Property
     * @return String formatted String
     * @throws RepositoryException when unable to print property
     */
    private String printValue(final Property p) throws RepositoryException {
        if (p.getDefinition().isMultiple()) {
            return "[mulitvalue]";
        }
        int type = p.getType();
        switch (type) {
        case PropertyType.STRING:
        case PropertyType.LONG:
        case PropertyType.BOOLEAN:
        case PropertyType.DOUBLE:
        case PropertyType.PATH:
        case PropertyType.REFERENCE:
        case PropertyType.NAME:
            String val = p.getString();
            if (val.length() > 50) {
                return val.substring(0, 42) + " [more..]";
            } else {
                return val;
            }
        case PropertyType.BINARY:
            return "[binary data]";
        case PropertyType.UNDEFINED:
            return "[undefined]";
        case PropertyType.DATE:
            return p.getValue().getString();
        default:
            return "[unknown type]: " + type;
        }
    }
}
