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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Helper class for commands to map command to it's class and map aliases.
 * This class also handles the reflecion stuff for executing commands.
 */
public final class CommandHelper {

    /**
     * Hide constructor.
     */
    private CommandHelper() {
        super();
    }

    /**
     * The map containing command->class sets.
     */
    private static Map<String, String> commandMap = new TreeMap<String, String>();

    /**
     * The map containing alias->command sets.
     */
    private static Map<String, String> aliasMap = new TreeMap<String, String>();

    /**
     * Check if the alias is a registered alias.
     * @param alias the alias
     * @return true if the alias if found
     */
    public static boolean isAlias(final String alias) {
        return aliasMap.containsKey(alias);
    }

    /**
     * Get the command for the alias.
     * @param alias the alias
     * @return the command or null if the alias is not found
     */
    public static String getCommandForAlias(final String alias) {
        if (isAlias(alias)) {
            return aliasMap.get(alias);
        }
        return null;
    }

    /**
     * Check if the command is a registered command.
     * @param command the command
     * @return true if the command is found
     */
    public static boolean isCommand(final String command) {
        return commandMap.containsKey(command);
    }

    /**
     * Get the class name for the command.
     * @param command the command
     * @return the class name or null if command is not found
     */
    public static String getClassForCommand(final String command) {
        if (isCommand(command)) {
            return commandMap.get(command);
        }
        return null;
    }

    /**
     * Register a new command class.
     * @param clazz the class name
     */
    public static void registerCommandClass(final String clazz) {
        String[] aliases = (String[]) runMethod("getAliases", clazz);
        String command = (String) runMethod("getCommand", clazz);
        commandMap.put(command, clazz);
        for (String alias : aliases) {
            aliasMap.put(alias, command);
        }
    }

    /**
     * Get the registerd commands as array.
     * @return a string array with the commands
     */
    public static String[] getCommandsAsArray() {
        Set<String> commandSet = commandMap.keySet();
        String[] commands = new String[commandSet.size()];
        commandSet.toArray(commands);
        return commands;
    }

    /**
     * Get the registered aliases as array.
     * @return a string array with the aliases
     */
    public static String[] getAliasesAsArray() {
        Set<String> aliasSet = aliasMap.keySet();
        String[] aliases = new String[aliasSet.size()];
        aliasSet.toArray(aliases);
        return aliases;
    }

    /**
     * @see runMethod(String,String,String[])
     * @param method The method to execute
     * @param clazz The name of the class to execute.
     * @return The return value of the method.
     */
    public static Object runMethod(final String method, final String clazz) {
        return runMethod(method, clazz, null);
    }

    /**
     * This method hides the ugly details of reflection. In a nutshell it
     * invokes the doIt() method of the java class associated with the command
     * being executed.
     *
     * @param method The method to execute
     * @param clazz The name of the class to execute.
     * @param args The command line parses into tokens.
     * @return The return value of the method.
     */
    public static Object runMethod(final String method, final String clazz, final String[] args) {
        // get the class object.
        Class theClass = null;
        try {
            theClass = Class.forName(clazz);
            if (theClass == null) {
                throw new RuntimeException("theClass is null.");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Class Not Found: " + clazz);
        }

        // get the constructor object.
        Constructor theConstructor = null;
        try {
            theConstructor = theClass.getConstructor(new Class[0]);
            if (theConstructor == null) {
                throw new RuntimeException("theConstructor is null.");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to find constructor for " + clazz);
        }

        // get the instance object.
        Command theInstance = null;
        try {
            theInstance = (Command) theConstructor.newInstance((Object[]) new Class[0]);
            if (null == theInstance) {
                throw new RuntimeException("theInstance is null.");
            }
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
            throw new RuntimeException("InvocationTargetException for " + clazz);
        } catch (java.lang.NoClassDefFoundError e) {
            throw new RuntimeException("NoClassDefFoundError for " + clazz);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load constructor for " + clazz);
        }

        Class[] parameterTypes = new Class[1];
        parameterTypes[0] = java.lang.String[].class;

        // gets the method object.
        Method theMethod = null;
        try {
            if (args == null) {
                theMethod = theClass.getMethod(method);
            } else {
                theMethod = theClass.getMethod(method, parameterTypes);
            }
            if (null == theMethod) {
                throw new RuntimeException("theMethod is null.");
            }
        } catch (Exception e) {
            Method[] methlist = theClass.getDeclaredMethods();
            for (int i = 0; i < methlist.length; i++) {
                Method m = methlist[i];
                System.out.println("name = " + m.getName());
                System.out.println("decl class = " + m.getDeclaringClass());
                Class[] pvec = m.getParameterTypes();
                for (int j = 0; j < pvec.length; j++) {
                    System.out.println("param #" + j + " " + pvec[j]);
                }
                Class[] evec = m.getExceptionTypes();
                for (int j = 0; j < evec.length; j++) {
                    System.out.println("exc #" + j + " " + evec[j]);
                }
                System.out.println("return type = " + m.getReturnType());
                System.out.println("-----");
            }

            e.printStackTrace();
            throw new RuntimeException("Unable to find test method for " + clazz);
        }

        Object[] params = new Object[1];
        params[0] = args;

        Object theReturnValue;

        try {
            if (args == null) {
                theReturnValue = theMethod.invoke(theInstance);
            } else {
                theReturnValue = theMethod.invoke(theInstance, params);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException for " + clazz);
        } catch (InvocationTargetException e) {
            // check for exit command
            if (e.getCause() instanceof JcrShellShutdownException) {
                throw new JcrShellShutdownException();
            }
            e.getCause().printStackTrace();
            throw new RuntimeException("InvocationTargetException for " + clazz);
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("NoClassDefFoundError for " + clazz);
        }

        return theReturnValue;
    }

}
