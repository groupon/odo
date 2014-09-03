/*
 Copyright 2014 Groupon, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package com.groupon.odo.proxylib;

import com.groupon.odo.plugin.PluginArguments;
import com.groupon.odo.plugin.ResponseOverride;
import com.groupon.odo.proxylib.models.Configuration;
import com.groupon.odo.proxylib.models.Plugin;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PluginManager {
    private static final Logger logger = LoggerFactory
            .getLogger(PluginManager.class);

    private static PluginManager _instance = null;
    private EditService editService = EditService.getInstance();
    private String proxyLibPath = null;

    private ClassLoader classLoader = null;

    // list of loaded jars
    private ArrayList<String> jarInformation;

    // hashmap to hold method information for a class
    private HashMap<String, com.groupon.odo.proxylib.models.Method> methodInformation;

    // hashmap to hold class information for lazy loading
    private HashMap<String, ClassInformation> classInformation;

    public static void destroy() {
        _instance = null;
    }

    /**
     * Gets the current instance of plugin manager
     *
     * @return
     */
    public static PluginManager getInstance() {
        if (_instance == null) {
            _instance = new PluginManager();
            _instance.classInformation = new HashMap<String, ClassInformation>();
            _instance.methodInformation = new HashMap<String, com.groupon.odo.proxylib.models.Method>();
            _instance.jarInformation = new ArrayList<String>();

            if (_instance.proxyLibPath == null) {
                //Get the System Classloader
                ClassLoader sysClassLoader = Thread.currentThread().getContextClassLoader();

                //Get the URLs
                URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

                for (int i = 0; i < urls.length; i++) {
                    if (urls[i].getFile().contains("proxylib")) {
                        // store the path to the proxylib
                        _instance.proxyLibPath = urls[i].getFile();
                        break;
                    }
                }
            }
            _instance.initializePlugins();
        }
        return _instance;
    }

    public void initializePlugins() {
        Plugin[] plugins = this.getPlugins(true);

        for (Plugin plugin : plugins) {
            try {
                this.identifyClasses(plugin.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This loads plugin file information into a hash for lazy loading later on
     *
     * @param pluginDirectory
     */
    public void identifyClasses(final String pluginDirectory) throws Exception {
        methodInformation.clear();
        jarInformation.clear();
        try {
            new FileTraversal() {
                public void onDirectory(final File d) {
                }

                public void onFile(final File f) {
                    try {
                        // loads class files
                        if (f.getName().endsWith(".class")) {
                            // get the class name for this path
                            String className = f.getAbsolutePath();
                            className = className.replace(pluginDirectory, "");
                            className = getClassNameFromPath(className);

                            logger.info("Storing plugin information: {}, {}", className,
                                    f.getName());

                            ClassInformation classInfo = new ClassInformation();
                            classInfo.pluginPath = pluginDirectory;
                            classInformation.put(className, classInfo);
                        } else if (f.getName().endsWith(".jar")) {
                            // loads JAR packages
                            // open up jar and discover files
                            // look for anything with /proxy/ in it
                            // this may discover things we don't need but that is OK
                            try {
                                jarInformation.add(f.getAbsolutePath());
                                JarFile jarFile = new JarFile(f);
                                Enumeration<?> enumer = jarFile.entries();

                                // Use the Plugin-Name manifest entry to match with the provided pluginName
                                String pluginPackageName = jarFile.getManifest().getMainAttributes().getValue("plugin-package");
                                if(pluginPackageName == null)
                                    return;

                                while (enumer.hasMoreElements()) {
                                    Object element = enumer.nextElement();
                                    String elementName = element.toString();

                                    if (!elementName.endsWith(".class"))
                                        continue;


                                    String className = getClassNameFromPath(elementName);
                                    if (className.contains(pluginPackageName)) {
                                        logger.info("Storing plugin information: {}, {}", className,
                                                f.getAbsolutePath());

                                        ClassInformation classInfo = new ClassInformation();
                                        classInfo.pluginPath = f.getAbsolutePath();
                                        classInformation.put(className, classInfo);
                                    }
                                }
                            } catch (Exception e) {

                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Exception caught: {}, {}", e.getMessage(), e.getCause());
                    }
                }
            }.traverse(new File(pluginDirectory));
        } catch (IOException e) {
            throw new Exception("Could not identify all plugins: " + e.getMessage());
        }
    }

    /**
     * Create a classname from a given path
     *
     * @param path
     * @return
     */
    private String getClassNameFromPath(String path) {
        String className = path.replace(".class", "");

        // for *nix
        if (className.startsWith("/")) {
            className = className.substring(1, className.length());
        }
        className = className.replace("/", ".");

        // for windows
        if (className.startsWith("\\")) {
            className = className.substring(1, className.length());
        }
        className = className.replace("\\", ".");

        return className;
    }

    /**
     * Loads the specified class name and stores it in the hash
     *
     * @param className
     * @throws Exception
     */
    public void loadClass(String className) throws Exception {
        ClassInformation classInfo = classInformation.get(className);

        logger.info("Loading plugin.: {}, {}", className, classInfo.pluginPath);

        // get URL for proxylib
        // need to load this also otherwise the annotations cannot be found later on
        File libFile = new File(proxyLibPath);
        URL libUrl = libFile.toURI().toURL();

        // store the last modified time of the plugin
        File pluginDirectoryFile = new File(classInfo.pluginPath);
        classInfo.lastModified = pluginDirectoryFile.lastModified();

        // load the plugin directory
        URL classURL = new File(classInfo.pluginPath).toURI().toURL();
        ClassLoader sysClassLoader = Thread.currentThread().getContextClassLoader();

        //Get the URLs
        URL[] oldUrls = ((URLClassLoader) sysClassLoader).getURLs();
        ArrayList<URL> urlList = new ArrayList<URL>();
        Collections.addAll(urlList, oldUrls);
        urlList.add(classURL);

        URL[] urls = urlList.toArray(new URL[0]);
        classLoader = new URLClassLoader(urls);

        // load the class
        Class<?> cls = classLoader.loadClass(className);

        // put loaded class into classInfo
        classInfo.loadedClass = cls;
        classInfo.loaded = true;

        classInformation.put(className, classInfo);

        logger.info("Loaded plugin: {}, {} method(s)", cls.toString(), cls.getDeclaredMethods().length);
    }

    /**
     * Calls the specified function with the specified arguments. This is used for v2 response overrides
     *
     * @param className
     * @param methodName
     * @param args
     * @return
     * @throws Exception
     */
    public void callFunction(String className, String methodName, PluginArguments pluginArgs, Object... args) throws Exception {
        Class<?> cls = getClass(className);

        ArrayList<Object> newArgs = new ArrayList<Object>();
        newArgs.add(pluginArgs);
        com.groupon.odo.proxylib.models.Method m = preparePluginMethod(newArgs, className, methodName, args);

        m.getMethod().invoke(cls, newArgs.toArray(new Object[0]));
    }

    /**
    * Calls the specified function with the specified arguments. This is used for v1 response overrides
    *
    * @param className
    * @param methodName
    * @param args
    * @return
    * @throws Exception
    */
    public Object callFunction(String className, String methodName, String responseContent, Object... args) throws Exception {
        Object retval;
        Class<?> cls = getClass(className);

        ArrayList<Object> newArgs = new ArrayList<Object>();
        newArgs.add(responseContent);
        com.groupon.odo.proxylib.models.Method m = preparePluginMethod(newArgs, className, methodName, args);

        retval = m.getMethod().invoke(cls, newArgs.toArray(new Object[0]));
        return retval;
    }

    private com.groupon.odo.proxylib.models.Method preparePluginMethod(List<Object> newArgs, String className,
                                                                       String methodName, Object... args) throws Exception {

        com.groupon.odo.proxylib.models.Method method = getMethod(className, methodName);

        // now convert the remaining args as necessary so the function is invoked with the correct types
        if (method.getMethodArguments().length > 0) {
            int x = 0;
            for (Object type : method.getMethodArguments()) {
                if (((String) type).endsWith("Integer")) {
                    newArgs.add(Integer.parseInt((String) args[x]));
                } else if (((String) type).endsWith("String")) {
                    newArgs.add((String) args[x]);
                } else if (((String) type).endsWith("Boolean")) {
                    newArgs.add(Boolean.valueOf((String) args[x]));
                }
                x++;
            }
        }

        return method;
    }

    /**
     * Get method object for a class/method name
     *
     * @param className
     * @param methodName
     * @return
     * @throws Exception
     */
    public com.groupon.odo.proxylib.models.Method getMethod(String className, String methodName) throws Exception {
        // TODO: fix this so it returns the right override ID
        com.groupon.odo.proxylib.models.Method m = null;

        // calls getClass first in case the loaded class needs to be invalidated
        Class<?> gottenClass = getClass(className);
        ClassInformation classInfo = classInformation.get(className);

        String fullName = className + "." + methodName;
        if (methodInformation.containsKey(fullName)) {
            m = methodInformation.get(fullName);
        } else {
            logger.info("Getting method info: {}", fullName);

            // Make a new classpool with the system classpath URLS
            // We create a new classpool each time since we want to reload plugin information in case it has changed.
            // Once a method is loaded this should not get called so the extra expense is not always taken as a hit
            ClassPool classPool = new ClassPool();
            ClassLoader sysClassLoader = Thread.currentThread().getContextClassLoader();

            //Get the URLs
            URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();
            for (int i = 0; i < urls.length; i++) {
                try {
                    // insert all classpaths into the javassist classpool
                    classPool.insertClassPath(urls[i].getFile());
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
            classPool.insertClassPath(classInfo.pluginPath);

            // load method information
            Method[] methods = gottenClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().toString().compareTo(methodName) != 0)
                    continue;

                try {
                    // get annotation information
                    // each item should only have 1 annotation.. if it doesn't have any then skip it..
                    // if it has more than one then use the first one
                    Annotation[] annotations = method.getAnnotations();
                    for (Annotation annotation : annotations) {
                        com.groupon.odo.proxylib.models.Method newMethod = new com.groupon.odo.proxylib.models.Method();
                        newMethod.setClassName(className);
                        newMethod.setMethodName(methodName);
                        newMethod.setMethod(method);
                        newMethod.setMethodType(annotation.annotationType().toString());

                        String[] argNames = null;
                        String description = null;

                        CtClass cc = classPool.get(className);
                        CtMethod cm = cc.getDeclaredMethod(methodName);
                        Object[] all = cm.getAnnotations();

                        // Convert to the right type and get annotation information
                        if (annotation.annotationType().toString().endsWith(Constants.PLUGIN_RESPONSE_OVERRIDE_CLASS)) {
                            ResponseOverride roAnnotation = (ResponseOverride) all[0];
                            newMethod.setHttpCode(roAnnotation.httpCode());
                            description = roAnnotation.description();
                            argNames = roAnnotation.parameters();
                            newMethod.setOverrideVersion(1);
                        }
                        else if(annotation.annotationType().toString().endsWith(Constants.PLUGIN_RESPONSE_OVERRIDE_V2_CLASS)) {
                            com.groupon.odo.plugin.v2.ResponseOverride roAnnotation = (com.groupon.odo.plugin.v2.ResponseOverride) all[0];
                            description = roAnnotation.description();
                            argNames = roAnnotation.parameters();
                            newMethod.setBlockRequest(roAnnotation.blockRequest());
                            newMethod.setOverrideVersion(2);
                        }

                        // identify arguments
                        // first arg is always a reserved that we skip
                        ArrayList<String> params = new ArrayList<String>();
                        if (method.getParameterTypes().length > 1) {
                            for (int x = 1; x < method.getParameterTypes().length; x++) {
                                params.add(method.getParameterTypes()[x].getName());
                            }
                        }
                        newMethod.setMethodArguments(params.toArray(new Object[0]));
                        newMethod.setMethodArgumentNames(argNames);
                        newMethod.setDescription(description);
                        newMethod.setIdString(className + "." + methodName);

                        methodInformation.put(fullName, newMethod);
                        m = newMethod;
                        // want to break since we only care about the first annotation
                        break;
                    }

                    break;

                } catch (javassist.NotFoundException nfe) {
                    // this can happen if libraries mismatch
                    // in this case we just return null since the method would be unuseable
                    return null;
                }
            }
        }

        return m;
    }

    /**
     * Obtain the class of a given className
     *
     * @param className
     * @return
     * @throws Exception
     */
    private synchronized Class<?> getClass(String className) throws Exception {
        // see if we need to invalidate the class
        ClassInformation classInfo = classInformation.get(className);
        File classFile = new File(classInfo.pluginPath);
        if (classFile.lastModified() > classInfo.lastModified) {
            logger.info("Class {} has been modified, reloading", className);
            logger.info("Thread ID: {}", Thread.currentThread().getId());
            classInfo.loaded = false;
            classInformation.put(className, classInfo);

            // also cleanup anything in methodInformation with this className so it gets reloaded
            Iterator<Map.Entry<String, com.groupon.odo.proxylib.models.Method>> iter = methodInformation.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, com.groupon.odo.proxylib.models.Method> entry = iter.next();
                if (entry.getKey().startsWith(className)) {
                    iter.remove();
                }
            }
        }

        if (!classInfo.loaded) {
            loadClass(className);
        }

        return classInfo.loadedClass;
    }

    /**
     * Returns a string array of the available classes
     *
     * @return
     */
    public String[] getPluginClasses() {
        return classInformation.keySet().toArray(new String[0]);
    }

    /**
     * Returns a string array of the methods loaded for a class
     *
     * @param pluginClass
     * @return
     */
    public String[] getMethods(String pluginClass) throws Exception {
        ArrayList<String> methodNames = new ArrayList<String>();

        Method[] methods = getClass(pluginClass).getDeclaredMethods();
        for (Method method : methods) {
            logger.info("Checking {}", method.getName());

            com.groupon.odo.proxylib.models.Method methodInfo = this.getMethod(pluginClass, method.getName());
            if (methodInfo == null)
                continue;

            // check annotations
            Boolean matchesAnnotation = false;
            if (methodInfo.getMethodType().endsWith(Constants.PLUGIN_RESPONSE_OVERRIDE_CLASS) ||
                    methodInfo.getMethodType().endsWith(Constants.PLUGIN_RESPONSE_OVERRIDE_V2_CLASS)) {
                matchesAnnotation = true;
            }

            if (!methodNames.contains(method.getName()) && matchesAnnotation)
                methodNames.add(method.getName());
        }

        return methodNames.toArray(new String[0]);
    }

    /**
     * Class to handle some directory/file traversal
     */
    private class FileTraversal {
        public final void traverse(final File f) throws IOException {
            if (f.isDirectory()) {
                onDirectory(f);
                final File[] childs = f.listFiles();
                for (File child : childs) {
                    traverse(child);
                }
                return;
            }
            onFile(f);
        }

        public void onDirectory(final File d) {
        }

        public void onFile(final File f) {
        }
    }

    /**
     * This is used to pass all the methods into the model for editGroup
     * (mostly just for testing and seeing how things work for now)
     * gets all the methods so that i can pass them in as an attribute to our model
     *
     * @return
     * @throws Exception
     */
    public List<com.groupon.odo.proxylib.models.Method> getAllMethods() throws Exception {
        ArrayList<com.groupon.odo.proxylib.models.Method> methods = new ArrayList<com.groupon.odo.proxylib.models.Method>();
        String[] classes = getPluginClasses();
        for (int i = 0; i < classes.length; i++) {
            try {
                String[] methodNames = getMethods(classes[i]);
                for (int j = 0; j < methodNames.length; j++) {
                    com.groupon.odo.proxylib.models.Method method = getMethod(classes[i], methodNames[j]);
                    methods.add(method);
                }
            } catch (java.lang.NoClassDefFoundError e) {
                // this is ok.. might mean an old plugin
            } catch (java.lang.ClassNotFoundException e) {
                // this is also ok..
            }
        }
        return methods;
    }

    /**
     * returns all the methods not in the group, using the same ArrayList<HashMap>> format
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    public List<com.groupon.odo.proxylib.models.Method> getMethodsNotInGroup(int groupId) throws Exception {
        List<com.groupon.odo.proxylib.models.Method> allMethods = getAllMethods();
        List<com.groupon.odo.proxylib.models.Method> methodsNotInGroup = new ArrayList<com.groupon.odo.proxylib.models.Method>();
        List<com.groupon.odo.proxylib.models.Method> methodsInGroup = editService.getMethodsFromGroupId(groupId, null);

        for (int i = 0; i < allMethods.size(); i++) {
            boolean add = true;
            String methodName = allMethods.get(i).getMethodName();
            String className = allMethods.get(i).getClassName();

            for (int j = 0; j < methodsInGroup.size(); j++) {
                if ((methodName.equals(methodsInGroup.get(j).getMethodName())) &&
                        (className.equals(methodsInGroup.get(j).getClassName())))
                    add = false;
            }
            if (add)
                methodsNotInGroup.add(allMethods.get(i));
        }
        return methodsNotInGroup;
    }

    /**
     * Returns the data about all of the plugins that are set
     *
     * @return
     */
    public Plugin[] getPlugins(Boolean onlyValid) {
        Configuration[] configurations = ConfigurationService.getInstance().getConfigurations(Constants.DB_TABLE_CONFIGURATION_PLUGIN_PATH);

        ArrayList<Plugin> plugins = new ArrayList<Plugin>();

        if (configurations == null)
            return new Plugin[0];

        for (Configuration config : configurations) {
            Plugin plugin = new Plugin();
            plugin.setId(config.getId());
            plugin.setPath(config.getValue());

            File path = new File(plugin.getPath());
            if (path.isDirectory()) {
                plugin.setStatus(Constants.PLUGIN_STATUS_VALID);
                plugin.setStatusMessage("Valid");
            } else {
                plugin.setStatus(Constants.PLUGIN_STATUS_NOT_DIRECTORY);
                plugin.setStatusMessage("Path is not a directory");
            }

            if (!onlyValid || plugin.getStatus() == Constants.PLUGIN_STATUS_VALID)
                plugins.add(plugin);
        }

        return plugins.toArray(new Plugin[0]);
    }

    public void addPluginPath(String path) throws Exception {
        ConfigurationService.getInstance().addValue(Constants.DB_TABLE_CONFIGURATION_PLUGIN_PATH, path);
        this.identifyClasses(path);
    }

    public void deletePluginPath(int id) throws Exception {
        ConfigurationService.getInstance().deleteValue(id);
        // TODO: clear these out of memory
    }

    /**
     * Gets a static resource from a plugin
     *
     * @param pluginName - Name of the plugin(defined in the plugin manifest)
     * @param fileName   - Filename to fetch
     * @return
     * @throws Exception
     */
    public byte[] getResource(String pluginName, String fileName) throws Exception {
        // TODO: This is going to be slow.. future improvement is to cache the data instead of searching all jars
        for (String jarFilename : jarInformation) {
            JarFile jarFile = new JarFile(new File(jarFilename));
            Enumeration<?> enumer = jarFile.entries();

            // Use the Plugin-Name manifest entry to match with the provided pluginName
            String jarPluginName = jarFile.getManifest().getMainAttributes().getValue("Plugin-Name");

            if (!jarPluginName.equals(pluginName))
                continue;

            while (enumer.hasMoreElements()) {
                Object element = enumer.nextElement();
                String elementName = element.toString();

                // Skip items in the jar that don't start with "resources/"
                if (!elementName.startsWith("resources/"))
                    continue;

                elementName = elementName.replace("resources/", "");
                if (elementName.equals(fileName)) {
                    // get the file from the jar
                    ZipEntry ze = jarFile.getEntry(element.toString());

                    InputStream fileStream = jarFile.getInputStream(ze);
                    byte[] data = new byte[(int) ze.getSize()];
                    DataInputStream dataIs = new DataInputStream(fileStream);
                    dataIs.readFully(data);
                    dataIs.close();
                    return data;
                }
            }
        }
        throw new FileNotFoundException("Could not find resource");
    }

    /**
     * Simple class to hold information about loaded/unloaded classes
     */
    private class ClassInformation {
        public boolean loaded = false;
        public String pluginPath = null;
        public long lastModified = 0;
        public Class<?> loadedClass = null;
    }
}
