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

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import jline.Completor;

/**
 * Command line completer for node names.
 */
public class NodeNameCompletor implements Completor {

    /**
     * {@inheritDoc}
     */
    public int complete(final String buf, final int cursor, final List clist) {
        Node node = JcrWrapper.getCurrentNode();
        // sanity check
        if (node == null) {
            return -1;
        }

        // get path and node part
        String start = (buf == null) ? "" : buf;
        String path = null;
        boolean absolute = start.startsWith("/");
        int lastSlash = start.lastIndexOf('/');

        try {
            if (absolute) {
                if (lastSlash > 0) {
                    path = start.substring(0, lastSlash);

                    // strip path from start of matcher
                    start = start.substring(lastSlash + 1);

                    node = node.getSession().getRootNode().getNode(path.substring(1));
                } else {
                    path = "";
                    start = start.substring(1);
                    node = node.getSession().getRootNode();
                }
            } else {
                if (lastSlash > -1) {
                    path = start.substring(0, lastSlash);

                    // strip path from start of matcher
                    start = start.substring(lastSlash + 1);

                    if (path.equals("../")) {
                        node = node.getParent();
                    } else {
                        node = node.getNode(path);
                    }
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        // fetch node list
        SortedSet<String> candidates = JcrWrapper.getNodeNameList(node);

        // strip first part of list that do not match
        SortedSet<String> matches = candidates.tailSet(start);

        // find and add matches
        for (Iterator<String> i = matches.iterator(); i.hasNext();) {
            String can = (String) i.next();
            // list is sorted, bail out
            if (!(can.startsWith(start))) {
                break;
            }
            // add path if needed
            if (path != null) {
                clist.add(path + "/" + can + "/");
            } else {
                clist.add(can + "/");
            }
        }
        // add slash if there's only one hit
        if (clist.size() == 1) {
            clist.set(0, ((String) clist.get(0)));
        }
        return (clist.size() == 0) ? (-1) : 0;
    }
}
