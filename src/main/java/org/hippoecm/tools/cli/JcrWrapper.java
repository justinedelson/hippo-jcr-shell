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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.commons.JcrUtils;

/**
 * Wrapper class for commonly used jcr calls.
 */
public final class JcrWrapper {

    public static final String NOT_CONNECTED_PROMPT = "jcr-shell:>";

    private static Map<String, SortedSet<String>> nodeNameCache = new HashMap<String, SortedSet<String>>();
    private static Map<String, SortedSet<String>> propertyNameCache = new HashMap<String, SortedSet<String>>();
    private static Object mutex = new Object();
    private static EventListener cacheListener;

    private static String server = "http://localhost:8888/server";

    private static String username = "admin";

    private static char[] password = "admin".toCharArray();

    private static Session session;

    private static Node currentNode;

    private static Node previousNode;

    private static boolean connected;

    private static Terminal term;

    private static boolean isHippoRepository = true;

    private JcrWrapper() {
        super();
    }

    public static void setTerminal(final Terminal term) {
        JcrWrapper.term = term;
    }

    static void setPrompt() {
        if (!isConnected() || currentNode == null) {
            term.setCommandLinePrompt(NOT_CONNECTED_PROMPT);
            return;
        }
        try {
            // term.setCommandLinePrompt(getUsername()+"@"+getServer() + ":" +
            // currentNode.getPath() + ">");
            term.setCommandLinePrompt(getUsername() + ":" + currentNode.getPath() + ">");
        } catch (RepositoryException e) {
            // ignore
            // term.setCommandLinePrompt("jcr (error) :>");
        }
    }

    public static void setHippoRepository(final boolean isHippo) {
        isHippoRepository = isHippo;
    }

    public static boolean isHippoRepository() {
        return isHippoRepository;
    }

    private static void setCurrentNode(final Node node) {
        previousNode = currentNode;
        currentNode = node;
        setPrompt();
    }

    public static boolean isConnected() {
        if (session != null && session.isLive()) {
            return true;
        }
        return connected;
    }

    public static void setConnected(final boolean connected) {
        JcrWrapper.connected = connected;
    }

    public static char[] getPassword() {
        return password.clone();
    }

    public static void setPassword(final String password) {
        JcrWrapper.password = password.toCharArray();
    }

    public static String getServer() {
        return server;
    }

    public static void setServer(final String server) {
        JcrWrapper.server = server;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(final String username) {
        JcrWrapper.username = username;
    }

    public static void clearCaches() {
        synchronized (mutex) {
            propertyNameCache.clear();
            nodeNameCache.clear();
        }
    }

    public static void removeFromCache(final String nodePath) {
        synchronized (mutex) {
            propertyNameCache.remove(nodePath);
            nodeNameCache.remove(nodePath);
        }
    }

    public static void updateCaches(EventIterator events) {
        Set<String> paths = new HashSet<String>();
        while (events.hasNext()) {
            Event event = events.nextEvent();
            try {
                String path = event.getPath();
                switch (event.getType()) {
                case Event.NODE_ADDED:
                case Event.NODE_REMOVED:
                    paths.add(path);
                    // fall through
                case Event.PROPERTY_ADDED:
                case Event.PROPERTY_CHANGED:
                case Event.PROPERTY_REMOVED:
                    path = path.substring(0, path.lastIndexOf('/'));
                    paths.add(path);
                    break;

                default:
                    System.out.println("Unknown event type: " + event.getType());
                    break;
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        for (String path : paths) {
            JcrWrapper.removeFromCache(path);
        }
    }

    public static String getStatus() {
        if (!isConnected()) {
            return username + "@" + server + " connected: " + connected;
        } else {
            return username + "@" + server + " session: " + session.getClass();
        }
    }

    public static boolean connect() {
        if (isConnected()) {
            return true;
        }
        // get the repository login and get session
        try {
            System.out.println();
            Repository repository = JcrUtils.getRepository(getServer());
            session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
            setConnected(true);
            setCurrentNode(session.getRootNode());
            System.out.println("done.");

            // start listener for caches
            ObservationManager obMgr = session.getWorkspace().getObservationManager();
            cacheListener = new EventListener() {
                public void onEvent(EventIterator events) {
                    JcrWrapper.updateCaches(events);
                }
            };
            obMgr.addEventListener(cacheListener, Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED
                    | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED, "/", true, null, null, true);
            clearCaches();
            return true;
        } catch (LoginException e) {
            System.out.println("failed: " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("failed: " + e.getMessage());
        } catch (ClassCastException e) {
            System.out.println("failed: " + e.getMessage());
        }
        return false;
    }

    public static void refresh(final boolean keepChanges) {
        if (connect()) {
            try {
                session.refresh(keepChanges);
                clearCaches();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean login() {
        return connect();
    }

    public static void logout() {
        if (isConnected()) {
            try {
                ObservationManager obMgr = session.getWorkspace().getObservationManager();
                obMgr.removeEventListener(cacheListener);
            } catch (RepositoryException e) {
                // ignore
            }
            session.logout();
            setConnected(false);
            clearCaches();
            previousNode = null;
            currentNode = null;
            setPrompt();
        }
    }

    public static boolean save() {
        if (connect()) {
            try {
                session.save();
                return true;
            } catch (AccessDeniedException e) {
                e.printStackTrace();
            } catch (ItemExistsException e) {
                e.printStackTrace();
            } catch (ConstraintViolationException e) {
                e.printStackTrace();
            } catch (InvalidItemStateException e) {
                e.printStackTrace();
            } catch (VersionException e) {
                e.printStackTrace();
            } catch (LockException e) {
                e.printStackTrace();
            } catch (NoSuchNodeTypeException e) {
                e.printStackTrace();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Node getCurrentNode() {
        if (connect()) {
            return currentNode;
        } else {
            return null;
        }
    }

    public static boolean removeNode(final Node node) {
        try {
            removeFromCache(node.getPath());
            if (node.getDepth() > 0) {
                removeFromCache(node.getParent().getPath());
            }
            node.remove();
            return true;
        } catch (RepositoryException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addNode(final Node parent, final String name, final String nodeType) {
        try {
            removeFromCache(parent.getPath());
            parent.addNode(name, nodeType);
            return true;
        } catch (RepositoryException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean moveNode(final Node srcNode, final String destAbsPath) throws PathNotFoundException,
            ItemExistsException {
        if (!connect()) {
            return false;
        }
        try {
            removeFromCache(srcNode.getPath());
            removeFromCache(srcNode.getParent().getPath());
            session.move(srcNode.getPath(), destAbsPath);
            int lastSlash = destAbsPath.lastIndexOf('/');
            if (lastSlash > 0) {
                removeFromCache(destAbsPath.substring(0, lastSlash - 1));
            } else {
                removeFromCache("/");
            }
        } catch (ItemExistsException e) {
            throw e;
        } catch (PathNotFoundException e) {
            throw e;
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return false;
    }

//    public static boolean copyNode(final Node srcNode, final String destAbsPath) throws PathNotFoundException,
//            ItemExistsException {
//        if (!connect()) {
//            return false;
//        }
//        try {
//            if (session instanceof HippoSession) {
//                // Hippo specific..
//                Node destNode = ((HippoSession) session).copy(srcNode, destAbsPath);
//                removeFromCache(destNode.getParent().getPath());
//            } else {
//                System.out.println("Copy only available in HippoSessions.");
//            }
//        } catch (ItemExistsException e) {
//            throw e;
//        } catch (PathNotFoundException e) {
//            throw e;
//        } catch (RepositoryException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

//    public static boolean isVirtual(Node jcrNode) {
//        if (jcrNode == null) {
//            return false;
//        }
//        if (!isHippoRepository()) {
//            return false;
//        }
//
//        HippoNode hippoNode = (HippoNode) jcrNode;
//        try {
//            Node canonical = hippoNode.getCanonicalNode();
//            if (canonical == null) {
//                return true;
//            }
//            return !hippoNode.getCanonicalNode().isSame(hippoNode);
//        } catch (RepositoryException e) {
//            System.out.println(e.getMessage());
//            return false;
//        }
//    }

    public static SortedSet<String> getNodeNameList(final Node node) {
        if (!connect()) {
            return new TreeSet<String>();
        }
        synchronized (nodeNameCache) {
            try {
                if (nodeNameCache.containsKey(node.getPath())) {
                    return nodeNameCache.get(node.getPath());
                }
                SortedSet<String> names = new TreeSet<String>();
                if (node.getDepth() != 0) {
                    names.add("..");
                }
                NodeIterator iter = node.getNodes();
                while (iter.hasNext()) {
                    names.add(fullName(iter.nextNode()));
                }
                nodeNameCache.put(node.getPath(), Collections.unmodifiableSortedSet(names));
                return nodeNameCache.get(node.getPath());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static SortedSet<String> getPropertyNameList(final Node node) {
        if (!connect()) {
            return new TreeSet<String>();
        }
        synchronized (propertyNameCache) {
            try {
                if (propertyNameCache.containsKey(node.getPath())) {
                    return propertyNameCache.get(node.getPath());
                }
                SortedSet<String> names = new TreeSet<String>();
                PropertyIterator iter = node.getProperties();
                while (iter.hasNext()) {
                    names.add(fullName(iter.nextProperty()));
                }
                propertyNameCache.put(node.getPath(), Collections.unmodifiableSortedSet(names));
                return propertyNameCache.get(node.getPath());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Node resolvePath(final String path) throws RepositoryException {
        if (path == null || path.length() == 0) {
            return currentNode;
        } else if (path.equals(".")) {
            return currentNode;
        } else if (path.equals("/")) {
            return session.getRootNode();
        } else if (path.equals("..")) {
            return currentNode.getParent();
        } else {
            String[] elements = path.split("\\/");
            StringBuffer pathEncoded = new StringBuffer(elements[0]);
            for (int i = 1; i < elements.length; i++) {
                pathEncoded.append("/");
                //if ("..".equals(elements[i])) {
                    pathEncoded.append(elements[i]);
                //} else {
                //    pathEncoded.append(NodeNameCodec.encode(elements[i]));
                //}
            }

            if (path.startsWith("/")) {
                return session.getRootNode().getNode(pathEncoded.toString().substring(1));
            }
            Node refNode = null;
            if (path.indexOf('/') == -1 && currentNode.hasProperty(pathEncoded.toString())) {
                // try reference
                Property p = currentNode.getProperty(pathEncoded.toString());
                if (p.getType() == PropertyType.REFERENCE) {
                    if (p.getDefinition().isMultiple()) {
                        Value[] vals = p.getValues();
                        if (vals.length > 0) {
                            refNode = session.getNodeByUUID(p.getValues()[0].getString());
                        }
                    } else {
                        refNode = session.getNodeByUUID(p.getString());
                    }
                }
            }
            if (refNode == null) {
                return currentNode.getNode(pathEncoded.toString());
            } else {
                return refNode;
            }
        }
    }

    public static boolean cdPrevious() {
        if (previousNode != null) {
            setCurrentNode(previousNode);
            return true;
        }
        return false;
    }

    public static boolean cd(final String path) {
        if (!connect()) {
            return false;
        }
        Node node = null;
        try {
            node = resolvePath(path);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        }
        if (node != null) {
            setCurrentNode(node);
            return true;
        } else {
            return false;
        }
    }

    public static NodeIterator getNodes(final String path) {
        if (!connect()) {
            return null;
        }
        Node node = null;
        try {
            node = resolvePath(path);
            return node.getNodes();
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static PropertyIterator getProperties(final String path) {
        if (!connect()) {
            return null;
        }
        Node node = null;
        try {
            node = resolvePath(path);
            return node.getProperties();
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

//    public static boolean cduuid(final UUID uuid) {
//        if (uuid == null) {
//            return false;
//        }
//        if (!connect()) {
//            return false;
//        }
//        Node node = null;
//
//        try {
//            node = session.getNodeByUUID(uuid.toString());
//        } catch (ItemNotFoundException e) {
//            return false;
//        } catch (RepositoryException e) {
//            e.printStackTrace();
//        }
//        if (node != null) {
//            setCurrentNode(node);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public static String finduuid(final UUID uuid) {
//        if (uuid == null) {
//            return null;
//        }
//        if (!connect()) {
//            return null;
//        }
//        Node node = null;
//
//        try {
//            node = session.getNodeByUUID(uuid.toString());
//        } catch (ItemNotFoundException e) {
//            return null;
//        } catch (RepositoryException e) {
//            e.printStackTrace();
//        }
//        if (node != null) {
//            try {
//                return node.getPath();
//            } catch (RepositoryException e) {
//                e.printStackTrace();
//                return null;
//            }
//        } else {
//            return null;
//        }
//    }

    public static Map<String, String> getNamespaces() {
        SortedMap<String, String> namespaces = new TreeMap<String, String>();
        if (!connect()) {
            return namespaces;
        }
        NamespaceRegistry nsReg;
        try {
            nsReg = session.getWorkspace().getNamespaceRegistry();
            String[] uris = nsReg.getURIs();
            for (String uri : uris) {
                try {
                    if (!"".equals(uri)) {
                        namespaces.put(uri, nsReg.getPrefix(uri));
                    }
                } catch (NamespaceException e) {
                    System.out.println("Unable to resolve uri: " + uri);
                }
            }
        } catch (RepositoryException e1) {
            e1.printStackTrace();
        }
        return namespaces;
    }

    public static boolean addNamespace(String prefix, String uri) {
        if (!connect()) {
            return false;
        }
        NamespaceRegistry nsReg;
        try {
            nsReg = session.getWorkspace().getNamespaceRegistry();
            try {
                nsReg.registerNamespace(prefix, uri);
                return true;
            } catch (UnsupportedRepositoryOperationException e) {
                System.out.println("Not supported: " + e.getMessage());
            } catch (AccessDeniedException e) {
                System.out.println("Not allowed: " + e.getMessage());
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeNamespace(String prefix) {
        if (!connect()) {
            return false;
        }
        NamespaceRegistry nsReg;
        try {
            nsReg = session.getWorkspace().getNamespaceRegistry();
            try {
                nsReg.unregisterNamespace(prefix);
                return true;
            } catch (NamespaceException e) {
                System.out.println("Failed: " + e.getMessage());
            } catch (UnsupportedRepositoryOperationException e) {
                System.out.println("Not supported: " + e.getMessage());
            } catch (AccessDeniedException e) {
                System.out.println("Not allowed: " + e.getMessage());
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static NodeType getNodeType(String name) {
        if (!connect()) {
            return null;
        }
        NodeType nt = null;

        try {
            nt = session.getWorkspace().getNodeTypeManager().getNodeType(name);
        } catch (NoSuchNodeTypeException e) {
            System.out.println("No such node type: " + name);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return nt;
    }

    public static SortedSet<String> getNodeTypes(String type) {
        if (!connect()) {
            return null;
        }
        try {
            NodeTypeIterator iter;
            SortedSet<String> types = new TreeSet<String>();

            if ("primary".equals(type)) {
                iter = session.getWorkspace().getNodeTypeManager().getPrimaryNodeTypes();
            } else if ("mixin".equals(type)) {
                iter = session.getWorkspace().getNodeTypeManager().getMixinNodeTypes();
            } else {
                iter = session.getWorkspace().getNodeTypeManager().getAllNodeTypes();
            }
            while (iter.hasNext()) {
                types.add(iter.nextNodeType().getName());
            }
            return types;
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NodeIterator query(final String statement, final String language) throws InvalidQueryException {
        if (statement == null) {
            return null;
        }
        if (!connect()) {
            return null;
        }

        QueryManager qm;
        try {
            qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(statement, language);
            QueryResult result = q.execute();
            return result.getNodes();
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static final void exportXml(final String absPath, final OutputStream out, final boolean skipBinary)
//            throws IOException, RepositoryException {
//        if (isHippoRepository()) {
//            ((HippoSession) session).exportDereferencedView(absPath, out, skipBinary, false);
//        } else {
//            session.exportSystemView(absPath, out, skipBinary, false);
//        }
//    }
//
//    public static final void importXml(String parentAbsPath, InputStream in, int uuidBehavior, int referenceBehavior,
//            int mergeBehavior) throws IOException, RepositoryException {
//        if (isHippoRepository()) {
//            ((HippoSession) session).importDereferencedXML(parentAbsPath, in, uuidBehavior, referenceBehavior,
//                    mergeBehavior);
//        } else {
//            session.importXML(parentAbsPath, in, uuidBehavior);
//        }
//    }

    public static final String fullName(final Item item) throws RepositoryException {
        StringBuffer buf = new StringBuffer();
        if (item.getDepth() == 0) {
            buf.append('/');
        }
        String path = item.getPath();
        buf.append(path.substring(path.lastIndexOf('/') + 1));
        return buf.toString();
    }
}
