package com.inteliense.canx;

import com.inteliense.canx.cans.CanInputStream;
import com.inteliense.canx.cans.CanObjectInputStream;
import com.inteliense.canx.utils.AES;
import com.inteliense.canx.utils.SHA;

import java.io.*;
import java.lang.reflect.Method;

public class Startup {

    public static byte[] key;
    public static byte[] iv;

    public static void main(String[] args) throws Exception {

        String path = "/Users/int/Desktop/test.jar";

        System.out.println("###########################");

        CanInputStream canIn = new CanInputStream(encryptJar(path));
        CanObjectInputStream canObj = new CanObjectInputStream(canIn);

        System.out.println("###########################");

        StreamClassLoader scl = new StreamClassLoader(canObj);
        canObj.close();
        canIn.close();

        System.out.println("###########################");

        String[] classes = scl.getAllClassNames();

        System.out.println("############flaksjdflkaj###############");

        for (String cl : classes) {
            Class c = scl.loadClass(cl);
            Method method = c.getMethod("main", String[].class);
            String[] params = new String[]{};
            method .invoke(null, (Object) params);
            if (c != null) {
                System.out.println("Successfully loaded " + cl);
            }
        }

        System.out.println("################************************###########");

    }

    private static String encryptJar(String path) throws Exception {

        FileInputStream in = new FileInputStream(path);
        BufferedInputStream bin = new BufferedInputStream(in);

        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        byte[] bite = new byte[1];

        while(bin.read(bite, 0, 1) > 0) {
            ba.write(bite[0]);
            bite = new byte[1];
        }

        byte[] entryData = ba.toByteArray();

        in.close();
        bin.close();
        ba.close();

        key = AES.generateKey();
        iv = AES.generateIv();

        System.out.println(SHA.getHex(key));
        System.out.println(SHA.getHex(iv));

        System.out.println("entryDataLen=" + entryData.length);

        byte[] encrypted = AES.encrypt(entryData, key, iv);

        File file = new File(path.replace(".jar", ".can"));
        if(!file.exists()) file.createNewFile();
        PrintWriter pw = new PrintWriter(file);

        pw.print(SHA.getHex(encrypted));
        System.out.println(SHA.getHex(encrypted));

        pw.close();
        pw.flush();

        return path.replace(".jar", ".can");

    }

}
