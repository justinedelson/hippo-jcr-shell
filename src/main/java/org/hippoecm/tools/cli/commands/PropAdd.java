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
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Set a single value property.
 */
public class PropAdd implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "propadd";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "addprop" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "propadd <property> [<type>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "Create an empty multi valued property, default type is String";
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

        try {
            String propName = args[1];
            String propTypeName = null;
            int propType = -1;
            if (args.length == 3) {
                propTypeName = args[2];
            } else {
                propTypeName = "String";
            }

            try {
                propType = PropertyType.valueFromName(propTypeName);
            } catch (IllegalArgumentException e) {
                System.out.println("Uknown property type: " + propTypeName);
                return false;
            }

            if (node.hasProperty(propName)) {
                System.out.println("Property '" + propName + "' already exists.");
                return false;
            }
            try {
                node.setProperty(propName, new Value[] {}, propType);

                //TODO, shouldn't be handling cache inside command, move to JcrWrapper
                JcrWrapper.removeFromCache(node.getPath());

            } catch (ValueFormatException e) {
                System.out.println("Unable to create value: " + e.getMessage());
                return false;
            }

        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
