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

import jline.Completor;

/**
 * Command line completer for property names.
 */
public class PropertyNameCompletor implements Completor {

    /**
     * {@inheritDoc}
     */
    public int complete(final String buf, final int cursor, final List clist) {
        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return -1;
        }

        // fetch node list
        String start = (buf == null) ? "" : buf;

        SortedSet<String> candidates = JcrWrapper.getPropertyNameList(node);
        SortedSet<String> matches = candidates.tailSet(start);

        for (Iterator<String> i = matches.iterator(); i.hasNext();) {
            String can = (String) i.next();
            if (!(can.startsWith(start))) {
                break;
            }
            clist.add(can);
        }

        if (clist.size() == 1) {
            clist.set(0, ((String) clist.get(0)) + " ");
        }
        return (clist.size() == 0) ? (-1) : 0;
    }
}
