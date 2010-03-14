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
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrCompactNodeTypeDefWriter;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * List allowed child nodes of current node.
 */
public class NodeAllowed implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "nodeallowed";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "allowednodes" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "nodeallowed";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "show a list of (child) nodes allowed for current node";
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
        NodeDefinition[] nodeDefs = nt.getDeclaredChildNodeDefinitions();
        for (NodeDefinition nodeDef : nodeDefs) {
            System.out.println(getNodeDefString(nodeDef));
        }

        System.out.println("");
        System.out.println("inherited: ");
        NodeType[] superTypes = nt.getDeclaredSupertypes();
        for (NodeType superType : superTypes) {
            NodeDefinition[] superDefs = superType.getDeclaredChildNodeDefinitions();
            for (NodeDefinition nodeDef : superDefs) {
                System.out.println(getNodeDefString(nodeDef));
            }

        }
        System.out.println("");

        return true;
    }

    private String getNodeDefString(NodeDefinition nodeDef) {
        StringBuffer def = new StringBuffer("+ ");

        String name = nodeDef.getName();
        if (name.equals("*")) {
            def.append('*');
        } else {
            def.append(JcrCompactNodeTypeDefWriter.resolve(name));
        }
        NodeType[] reqTypes = nodeDef.getRequiredPrimaryTypes();
        if (reqTypes != null && reqTypes.length > 0) {
            String delim = " (";
            for (int i = 0; i < reqTypes.length; i++) {
                def.append(delim);
                def.append(JcrCompactNodeTypeDefWriter.resolve(reqTypes[i].getName()));
                delim = ", ";
            }
            def.append(")");
        }
        NodeType defaultType = nodeDef.getDefaultPrimaryType();
        if (defaultType != null && !defaultType.getName().equals("*")) {
            def.append(" = ");
            def.append(JcrCompactNodeTypeDefWriter.resolve(defaultType.getName()));
        }

        if (nodeDef.isMandatory()) {
            def.append(" mandatory");
        }
        if (nodeDef.isAutoCreated()) {
            def.append(" autocreated");
        }
        if (nodeDef.isProtected()) {
            def.append(" protected");
        }
        if (nodeDef.allowsSameNameSiblings()) {
            def.append(" multiple");
        }
        return def.toString();
    }

}
