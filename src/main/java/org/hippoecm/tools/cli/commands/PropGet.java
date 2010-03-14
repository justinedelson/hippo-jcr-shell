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
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Display property value(s).
 */
public class PropGet implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "propget";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "get", "getprop" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "propget <property> [<property> [..]]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "get the value(s) of properties from the current node";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length < 2) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        for (int i = 1; i < args.length; i++) {
            try {
                final String propName = args[i];
                if (!node.hasProperty(propName)) {
                    System.out.println("Node doesn't have a property with name: " + propName);
                    return true;
                }
                Property p = node.getProperty(propName);

                int type = p.getType();
                System.out.println(" " + p.getName() + "\t type:" + PropertyType.nameFromValue(type) + "\t multi:"
                        + p.getDefinition().isMultiple());
                System.out.println(" --- ");

                if (!p.getDefinition().isMultiple()) {
                    printValue(p.getValue());
                } else {
                    for (Value val : p.getValues()) {
                        printValue(val);
                    }
                }
                System.out.println(" --- ");

            } catch (RepositoryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;

    }

    /**
     * Helper method for pretty printing property values.
     * @param v Value
     * @throws RepositoryException when unable to print value
     */
    private void printValue(final Value v) throws RepositoryException {
        int type = v.getType();
        switch (type) {
        case PropertyType.STRING:
        case PropertyType.LONG:
        case PropertyType.BOOLEAN:
        case PropertyType.DOUBLE:
        case PropertyType.PATH:
        case PropertyType.REFERENCE:
        case PropertyType.NAME:
            System.out.println(v.getString());
            break;
        case PropertyType.BINARY:
            System.out.println("binary data");
            break;
        case PropertyType.UNDEFINED:
            System.out.println("undefined");
            break;
        case PropertyType.DATE:
            System.out.println(v.toString());
            break;
        default:
            throw new IllegalArgumentException("unknown type: " + type);
        }
    }
}
