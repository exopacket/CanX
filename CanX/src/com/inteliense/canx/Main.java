package com.inteliense.canx;

import com.inteliense.canx.utils.AES;
import com.inteliense.canx.utils.SHA;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

    //CanX Daemon C Program and C CLI

    //OVERALL TODO
    //CanMake java jar that encrypts jars with the available options & support for encrypted java daemons.
    //CanX ## java jar that is executed by java replacement c exec;
    //              recognizes encrypted jar (java -jar [filename]) -> loads classes and invokes entrypoint
    //              !encrypted -> prints java command and returns expected exit code -> c exec ends fork;
    //                      --> starts new fork with correct java location and command
    //Keys Exec ## c executable that returns key with [successful verification ?]
    //Java Replacement /usr/bin/java w/ c executable that forks CanX with .jar file (auto recognized in CanX)
    //installer

    //TODO ecb key storing & caching w/[?] c executable & KleverKeys lib

    public static void main(String[] args) throws Exception {

        System.out.println(encryptJar("/Users/int/Desktop/", "/Users/int/Desktop/", "test", "MyPass", 0, "Main"));

    }

    //TODO fix in/outPath & entrypoint (.class .java replace) plus package check
    private static String encryptJar(String inPath, String outPath, String filename, String password, int user, String entrypoint) throws Exception {

        FileInputStream in = new FileInputStream(inPath + filename + ".jar");
        BufferedInputStream bin = new BufferedInputStream(in);

        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        byte[] bite = new byte[1];

        while(bin.read(bite, 0, 1) > 0) {
            ba.write(bite[0]);
            bite = new byte[1];
        }

        byte[] binData = ba.toByteArray();

        in.close();
        bin.close();
        ba.close();

        byte[] ecbKey = getEcbKey();
        byte[] cbcKey = getCbcKey();
        byte[] cbcIv = AES.generateIv();

        String encIv = AES.encrypt(SHA.getHex(cbcIv), ecbKey);

        String passHash512 = SHA.getHash(SHA.getHmac256(password, SHA.getHex(ecbKey)));
        String encPassHash512 = AES.encrypt(passHash512, ecbKey);

        String userHash512 = SHA.getHash(SHA.getHmac256("" + user, SHA.getHex(ecbKey)));
        String encUserHash512 = AES.encrypt(userHash512, ecbKey);

        String sigHash512 = SHA.getHash(SHA.getHmac256(SHA.getHex(binData), SHA.getHex(ecbKey)));
        String encSigHash512 = SHA.getHex(AES.encrypt(SHA.getBytes(sigHash512), cbcKey, cbcIv));

        String encEntrypoint = SHA.getHex(AES.encrypt(entrypoint.getBytes(StandardCharsets.UTF_8), cbcKey, cbcIv));

        byte[] encrypted = AES.encrypt(binData, cbcKey, cbcIv);
        String encryptedHex = SHA.getHex(encrypted);

        File file = new File(outPath + filename + ".jar");
        if(!file.exists()) file.createNewFile();
        PrintWriter pw = new PrintWriter(file);

        pw.println("._can_start_");
        pw.println(encUserHash512);
        pw.println(encPassHash512);
        pw.println(encEntrypoint);
        pw.println(encIv);
        pw.println(encSigHash512);
        pw.println(encryptedHex);
        pw.println("._can_end_");

        pw.close();
        pw.flush();

        return outPath + filename + ".jar";

    }

    //TODO next 2 functions getKey
    private static byte[] getEcbKey() {
        return SHA.getBytes("737BA2E430BC862AFCE04F44043AC709");
    }

    private static byte[] getCbcKey() {
        return SHA.getBytes("82F486EA43D7A0D5ED4FE6726C1AAF6524616CE856FA2FFFB67DBB49B1CAB90A");
    }

}
