package com.cookbook.processing;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class ClassManager {

    static List<Class> findAllClasses(String packageName) {

        List<Class> classes = new ArrayList<>();
        File directory = null;
        try {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader == null) {
                    throw new ClassNotFoundException("Can't get class loader.");
                }
                String path = packageName.replace('.', '/');
                URL resource = classLoader.getResource(path);
                if (resource == null) {
                    throw new ClassNotFoundException("No resource for " + path);
                }
                directory = new File(resource.getFile());
            } catch (NullPointerException x) {
                throw new ClassNotFoundException(
                        packageName + " (" + directory + ") does not appear to be a valid package");
            }
            classes = findClasses(directory, packageName);

        } catch (Exception ex) {
            classes.clear();
            System.out.println("Exception " + ex.getMessage());
        }
        return classes;
    }

    static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }






}
