package com.inteliense.canx;

import com.inteliense.canx.cans.CanObjectInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;

//you can open a jar... but you need a can opener to open a can lmfao

//yes, it works. I just messed it up..... so others could understand it better.

//they're never getting it.. ;)

public class StreamClassLoader extends ClassLoader {

    private final Map<String, byte[]> classData;

    public StreamClassLoader(CanObjectInputStream is) throws IOException {
        classData = new HashMap();
        JarEntry jarEntry = null;

        while ((jarEntry = is.getNextJarEntry()) != null) {
            String entryName = jarEntry.getName();
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            byte[] bite = new byte[1];

            while(is.read(bite, 0, 1) > 0) {
                ba.write(bite[0]);
                bite = new byte[1];
            }

            byte[] entryData = ba.toByteArray();

            if (entryName.endsWith(".class")) {
                String className = entryName.replace("/", ".").replace(".class", "");
                classData.put(className, entryData);
            }
        }
    }

    public String[] getAllClassNames() {
        Set<String> keyset = classData.keySet();
        return keyset.toArray(new String[keyset.size()]);
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        // note that it is required to first try loading the class using parent loader
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            return findClass(name);
        }
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        Class loadedClass = null;

        byte[] data = classData.getOrDefault(name, new byte[0]);
        if (data.length == 0) {
            throw new ClassNotFoundException();
        }

        loadedClass = defineClass(name, data, 0, data.length, null);

        return loadedClass;
    }
}