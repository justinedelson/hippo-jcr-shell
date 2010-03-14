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
 * Add a value to or creat a multi value property to current node.
 */
public class ValueAdd implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "valueadd";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "addvalue" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "valueadd <propertyname> <value> [<type>]";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "add the value to a existing multi value property or create a new property, default type is String";
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
            StringBuffer propValue = new StringBuffer(args[2]);
            for (int i = 3; i < args.length; i++) {
                propValue.append(" ").append(args[i]);
            }
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

            Value value;
            try {
                value = node.getSession().getValueFactory().createValue(propValue.toString(), propType);
            } catch (ValueFormatException e) {
                System.out.println("Unable to create value: " + e.getMessage());
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
                if (!p.getDefinition().isMultiple()) {
                    System.out.println("Use propset to set single value properties.");
                    return false;
                }
                Value[] values = p.getValues();
                Value[] newValues = new Value[values.length + 1];
                System.arraycopy(values, 0, newValues, 0, values.length);
                newValues[values.length] = value;
                node.setProperty(propName, newValues);
            } else {
                node.setProperty(propName, new Value[] { value });
            }
            //TODO, shouldn't be handling cache inside command, move to JcrWrapper
            JcrWrapper.removeFromCache(node.getPath());

        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;

    }

}
