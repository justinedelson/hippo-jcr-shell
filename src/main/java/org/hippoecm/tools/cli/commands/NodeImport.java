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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.ImportMergeBehavior;
import org.hippoecm.repository.api.ImportReferenceBehavior;
import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Copy a child node.
 */
public class NodeImport implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "nodeimport";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "import" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "nodeimport <xml file> [<uuidBehavior> [<referenceBehavior> [<mergeBehavior>]]]";
    }

    public class LookupHashMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 9065806784464553409L;

        public K getFirstKey(Object value) {
            if (value == null) {
                return null;
            }
            for (Map.Entry<K, V> e : entrySet()) {
                if (value.equals(e.getValue())) {
                    return e.getKey();
                }
            }
            return null;
        }
    }

    private final LookupHashMap<Integer, String> uuidOpts = new LookupHashMap<Integer, String>();
    private final LookupHashMap<Integer, String> mergeOpts = new LookupHashMap<Integer, String>();
    private final LookupHashMap<Integer, String> derefOpts = new LookupHashMap<Integer, String>();

    private final void InitMaps() {
        uuidOpts.put(new Integer(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING),
                "Remove existing node with same uuid");
        uuidOpts.put(new Integer(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING),
                "Replace existing node with same uuid");
        uuidOpts.put(new Integer(ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW), "Throw error on uuid collision");
        uuidOpts.put(new Integer(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW), "Create new uuids on import");

        mergeOpts.put(new Integer(ImportMergeBehavior.IMPORT_MERGE_ADD_OR_OVERWRITE),
                "Try to add, else overwrite same name nodes");
        mergeOpts.put(new Integer(ImportMergeBehavior.IMPORT_MERGE_ADD_OR_SKIP),
                "Try to add, else skip same name nodes");
        mergeOpts.put(new Integer(ImportMergeBehavior.IMPORT_MERGE_OVERWRITE), "Overwrite same name nodes");
        mergeOpts.put(new Integer(ImportMergeBehavior.IMPORT_MERGE_SKIP), "Skip same name nodes");
        mergeOpts.put(new Integer(ImportMergeBehavior.IMPORT_MERGE_THROW), "Throw error on naming conflict");

        derefOpts.put(new Integer(ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_REMOVE),
                "Remove reference when not found");
        derefOpts.put(new Integer(ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_THROW),
                "Throw error when not found");
        derefOpts.put(new Integer(ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_TO_ROOT),
                "Add reference to root node when not found");

    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        InitMaps();
        StringBuffer buf = new StringBuffer();
        buf.append("import xml export file to the current node").append("\n");

        buf.append("uuidBehavior: ").append("\n");
        for (Map.Entry<Integer, String> map : uuidOpts.entrySet()) {
            buf.append("  ").append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
        }

        buf.append("referenceBehavior: ").append("\n");
        for (Map.Entry<Integer, String> map : derefOpts.entrySet()) {
            buf.append("  ").append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
        }

        buf.append("mergeBehavior: ").append("\n");
        for (Map.Entry<Integer, String> map : mergeOpts.entrySet()) {
            buf.append("  ").append(map.getKey()).append(" : ").append(map.getValue()).append("\n");
        }
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length < 2 || args.length > 5) {
            System.out.println(usage());
            System.out.println(help());
            return false;
        }

        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        int uuidBehavior = ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;
        int referenceBehavior = ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_TO_ROOT;
        int mergeBehavior = ImportMergeBehavior.IMPORT_MERGE_ADD_OR_OVERWRITE;

        String fileName = args[1];
        if (args.length > 2) {
            try {
                uuidBehavior = new Integer(args[2]).intValue();
            } catch (NumberFormatException e) {
                System.out.println("Invalid uuidBehavior: " + args[2]);
                System.out.println(usage());
                return false;
            }
        }
        if (args.length > 3) {
            try {
                referenceBehavior = new Integer(args[3]).intValue();
            } catch (NumberFormatException e) {
                System.out.println("Invalid referenceBehavior: " + args[3]);
                System.out.println(usage());
                return false;
            }
        }
        if (args.length > 4) {
            try {
                mergeBehavior = new Integer(args[4]).intValue();
            } catch (NumberFormatException e) {
                System.out.println("Invalid mergeBehavior: " + args[4]);
                System.out.println(usage());
                return false;
            }
        }

        try {
            File file = new File(fileName);
            try {
                FileInputStream fis = new FileInputStream(file);
                try {
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    try {
                        JcrWrapper.importXml(node.getPath(), bis, uuidBehavior, referenceBehavior, mergeBehavior);
                    } finally {
                        bis.close();
                    }
                } finally {
                    fis.close();
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
