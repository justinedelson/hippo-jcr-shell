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
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrCompactNodeTypeDefWriter;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * List allowed properties of current node.
 */
public class PropAllowed implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "propallowed";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "allowedprops" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "propallowed";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "show a list of properties of allowed for current node";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length > 1) {
            System.out.println(usage());
            System.out.println(help());
            return true;
        }
        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        NodeType nt;
        try {
            nt = node.getPrimaryNodeType();
        } catch (RepositoryException e) {
            System.out.println("error: " + e.getMessage());
            return false;
        }

        System.out.println("Allowed child nodes: ");
        System.out.println("");

        System.out.println("main: ");
        PropertyDefinition[] propDefs = nt.getDeclaredPropertyDefinitions();
        for (PropertyDefinition propDef : propDefs) {
            System.out.println(getPropDefString(propDef));
        }

        System.out.println("");
        System.out.println("inherited: ");
        NodeType[] superTypes = nt.getDeclaredSupertypes();
        for (NodeType superType : superTypes) {
            PropertyDefinition[] superDefs = superType.getDeclaredPropertyDefinitions();
            for (PropertyDefinition propDef : superDefs) {
                System.out.println(getPropDefString(propDef));
            }

        }
        System.out.println("");

        return true;
    }

    private String getPropDefString(PropertyDefinition propDef) {
        StringBuffer def = new StringBuffer("- ");
        def.append(JcrCompactNodeTypeDefWriter.resolve(propDef.getName()));
        def.append(" (").append(PropertyType.nameFromValue(propDef.getRequiredType())).append(')');

        Value[] dv = propDef.getDefaultValues();
        if (dv != null && dv.length > 0) {
            String delim = " = '";
            for (int i = 0; i < dv.length; i++) {
                def.append(delim);
                try {
                    def.append(JcrCompactNodeTypeDefWriter.escape(dv[i].getString()));
                } catch (RepositoryException e) {
                    def.append(JcrCompactNodeTypeDefWriter.escape(dv[i].toString()));
                }
                def.append("'");
                delim = ", '";
            }
        }
        if (propDef.isMandatory()) {
            def.append(" mandatory");
        }
        if (propDef.isAutoCreated()) {
            def.append(" autocreated");
        }
        if (propDef.isProtected()) {
            def.append(" protected");
        }
        if (propDef.isMultiple()) {
            def.append(" multiple");
        }
        String[] vca = propDef.getValueConstraints();
        if (vca != null && vca.length > 0) {
            String vc = vca[0];
            def.append(" < '");
            def.append(JcrCompactNodeTypeDefWriter.escape(vc));
            def.append("'");
            for (int i = 1; i < vca.length; i++) {
                vc = vca[i];
                def.append(", '");
                def.append(JcrCompactNodeTypeDefWriter.escape(vc));
                def.append("'");
            }
        }
        return def.toString();
    }

}
