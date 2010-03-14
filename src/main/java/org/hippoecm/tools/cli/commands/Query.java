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
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.hippoecm.tools.cli.Command;
import org.hippoecm.tools.cli.JcrWrapper;

/**
 * Run a query.
 */
public class Query implements Command {

    /**
     * {@inheritDoc}
     */
    public final String getCommand() {
        return "query";
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getAliases() {
        return new String[] { "select" };
    }

    /**
     * {@inheritDoc}
     */
    public final String usage() {
        return "query <sql|xpath> <statement>";
    }

    /**
     * {@inheritDoc}
     */
    public final String help() {
        return "run a query statement. Language can be xpath or sql.";
    }

    /**
     * {@inheritDoc}
     */
    public final boolean execute(final String[] args) {
        if (args.length < 4) {
            System.out.println(help());
            return false;
        }
        String language;
        StringBuffer query = new StringBuffer();

        if ("select".equals(args[0])) {
            language = "sql";
            for (int i = 0; i < args.length; i++) {
                query.append(args[i]).append(" ");
            }
        } else {
            language = args[1].toLowerCase();
            if (!"xpath".equals(language) && !"sql".equals(language)) {
                System.out.println("Unknown query language: " + language);
                return false;
            }
            for (int i = 2; i < args.length; i++) {
                query.append(args[i]).append(" ");
            }
        }

        NodeIterator iter = null;
        try {
            iter = JcrWrapper.query(query.toString(), language);
        } catch (InvalidQueryException e1) {
            System.out.println("Invalid query: " + query.toString());
            return false;
        }

        if (iter == null) {
            System.out.println("Failed to run query: " + query.toString());
            return false;
        }

        try {
            System.out.printf("%-30s%-30s\n", "Name", "Path");
            System.out.printf("%-30s%-30s\n", "--------------------", "--------------------");
            while (iter.hasNext()) {
                Node n = iter.nextNode();
                System.out.printf("%-30s%-30s\n", JcrWrapper.fullName(n), n.getPath());
            }
            System.out.printf("%-30s%-30s\n", "--------------------", "--------------------");
            System.out.printf("Total: %s\n", iter.getSize());
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
