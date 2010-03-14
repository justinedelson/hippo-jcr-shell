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
import javax.jcr.ValueFormatException;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Set a single value property.
 */
public class PropSet implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "propset";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "setprop" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "propset <property> <value> [<type>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "Set the value of a existing property or create a new property, default type is String";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length < 3) {
            System.out.println(help());
            return false;
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        try {
            String propName = args[1];
            String propValue = args[2];
            String propTypeName = null;
            int propType = -1;
            if (args.length == 4) {
                propTypeName = args[3];
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
                Property p = node.getProperty(propName);
                int type = p.getType();
                if (type != propType) {
                    System.out.println("Property type doesn't match type of current property: "
                            + PropertyType.nameFromValue(type));
                    return false;
                }
                if (p.getDefinition().isMultiple()) {
                    System.out.println("Use propadd to add values to a multivalue.");
                    return false;
                }
            }
            try {
                Value value = node.getSession().getValueFactory().createValue(propValue, propType);
                node.setProperty(propName, value);

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
