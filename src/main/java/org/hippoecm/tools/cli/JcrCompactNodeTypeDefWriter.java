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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

public class JcrCompactNodeTypeDefWriter {

    private static final String INDENT = "  ";
    private Writer out;

    public JcrCompactNodeTypeDefWriter(OutputStream outputStream) {
        this.out = new OutputStreamWriter(outputStream);
    }

    public JcrCompactNodeTypeDefWriter(Writer out) {
        this.out = out;
    }

    public void printNodeTypeDef(NodeType nt) throws IOException {
        writeName(nt);
        writeSupertypes(nt);
        writeOptions(nt);
        writePropDefs(nt);
        writeNodeDefs(nt);
        out.write("\n\n");
        out.flush();
    }

    private void writeName(NodeType nt) throws IOException {
        out.write("[");
        out.write(resolve(nt.getName()));
        out.write("]");
    }

    private void writeSupertypes(NodeType nt) throws IOException {
        NodeType[] superTypes = nt.getDeclaredSupertypes();
        String delim = " > ";
        for (NodeType sn : superTypes) {
            out.write(delim);
            out.write(resolve(sn.getName()));
            delim = ", ";
        }
    }

    private void writeOptions(NodeType nt) throws IOException {
        if (nt.hasOrderableChildNodes()) {
            out.write("\n" + INDENT);
            out.write("orderable");
            if (nt.isMixin()) {
                out.write(" mixin");
            }
        } else if (nt.isMixin()) {
            out.write("\n" + INDENT);
            out.write("mixin");
        }
    }

    private void writePropDefs(NodeType nt) throws IOException {
        PropertyDefinition[] propdefs = nt.getDeclaredPropertyDefinitions();
        for (PropertyDefinition propdef : propdefs) {
            writePropDef(nt, propdef);
        }
    }

    private void writePropDef(NodeType nt, PropertyDefinition pd) throws IOException {
        out.write("\n" + INDENT + "- ");
        writeItemDefName(pd.getName());
        out.write(" (");
        out.write(PropertyType.nameFromValue(pd.getRequiredType()).toLowerCase());
        out.write(")");

        writeDefaultValues(pd.getDefaultValues());
        out.write(nt.getPrimaryItemName() != null && nt.getPrimaryItemName().equals(pd.getName()) ? " primary" : "");
        if (pd.isMandatory()) {
            out.write(" mandatory");
        }
        if (pd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (pd.isProtected()) {
            out.write(" protected");
        }
        if (pd.isMultiple()) {
            out.write(" multiple");
        }
        if (pd.getOnParentVersion() != OnParentVersionAction.COPY) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(pd.getOnParentVersion()).toLowerCase());
        }
        writeValueConstraints(pd.getValueConstraints());
    }

    private void writeNodeDefs(NodeType nt) throws IOException {
        NodeDefinition[] childnodeDefs = nt.getDeclaredChildNodeDefinitions();
        for (NodeDefinition childnodeDef : childnodeDefs) {
            writeNodeDef(nt, childnodeDef);
        }
    }

    private void writeNodeDef(NodeType nt, NodeDefinition nd) throws IOException {
        out.write("\n" + INDENT + "+ ");

        String name = nd.getName();
        if (name.equals("*")) {
            out.write('*');
        } else {
            writeItemDefName(name);
        }
        writeRequiredTypes(nd.getRequiredPrimaryTypes());
        writeDefaultType(nd.getDefaultPrimaryType());
        out.write(nt.getPrimaryItemName() != null && nt.getPrimaryItemName().equals(nd.getName()) ? " primary" : "");
        if (nd.isMandatory()) {
            out.write(" mandatory");
        }
        if (nd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (nd.isProtected()) {
            out.write(" protected");
        }
        if (nd.allowsSameNameSiblings()) {
            out.write(" multiple");
        }
        if (nd.getOnParentVersion() != OnParentVersionAction.COPY) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(nd.getOnParentVersion()).toLowerCase());
        }
    }

    private void writeRequiredTypes(NodeType[] reqTypes) throws IOException {
        if (reqTypes != null && reqTypes.length > 0) {
            String delim = " (";
            for (int i = 0; i < reqTypes.length; i++) {
                out.write(delim);
                out.write(resolve(reqTypes[i].getName()));
                delim = ", ";
            }
            out.write(")");
        }
    }

    /**
     * write default types
     * @param defType
     */
    private void writeDefaultType(NodeType defType) throws IOException {
        if (defType != null && !defType.getName().equals("*")) {
            out.write(" = ");
            out.write(resolve(defType.getName()));
        }
    }

    private void writeValueConstraints(String[] vca) throws IOException {
        if (vca != null && vca.length > 0) {
            String vc = vca[0];
            out.write(" < '");
            out.write(escape(vc));
            out.write("'");
            for (int i = 1; i < vca.length; i++) {
                vc = vca[i];
                out.write(", '");
                out.write(escape(vc));
                out.write("'");
            }
        }
    }

    private void writeItemDefName(String name) throws IOException {
        out.write(resolve(name));
    }

    public static String resolve(String name) {
        if (name == null) {
            return "";
        }

        if (name.indexOf(":") > -1) {

            String prefix = name.substring(0, name.indexOf(":"));
            if (!"".equals(prefix)) {
                prefix += ":";
            }

            String encLocalName = /*ISO9075Helper.encodeLocalName(*/name.substring(name.indexOf(":") + 1)/*)*/;
            String resolvedName = prefix + encLocalName;

            // check for '-' and '+'
            if (resolvedName.indexOf('-') >= 0 || resolvedName.indexOf('+') >= 0) {
                return "'" + resolvedName + "'";
            } else {
                return resolvedName;
            }
        } else {
            return name;
        }

    }

    public static String escape(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\\') {
                sb.insert(i, '\\');
                i++;
            } else if (sb.charAt(i) == '\'') {
                sb.insert(i, '\'');
                i++;
            }
        }
        return sb.toString();
    }

    private void writeDefaultValues(Value[] dva) throws IOException {
        if (dva != null && dva.length > 0) {
            String delim = " = '";
            for (int i = 0; i < dva.length; i++) {
                out.write(delim);
                try {
                    out.write(escape(dva[i].getString()));
                } catch (RepositoryException e) {
                    out.write(escape(dva[i].toString()));
                }
                out.write("'");
                delim = ", '";
            }
        }
    }
}