package com.wyattk.appframe.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Util class for reading data from a file
 */
public class FileRead {

    /**
     * Reads data from a specified file path, starts looking in resources & packages
     * @param path is the path to the file
     * @return the contents of the file
     * @throws IOException when something goes wrong reading the file
     */
    public static String read(String path) throws IOException {
        InputStream s = FileRead.class.getClassLoader().getResourceAsStream(path);
        if(s == null)
            throw new IllegalArgumentException("File not found: " + path);
        byte[] bts = s.readAllBytes();
        s.close();
        StringBuilder sb = new StringBuilder("");
        for(byte b: bts)
            sb.append((char) b);
        return sb.toString();
    }

    /**
     * Exactly the same as read(), but help clarify that it does read in the resources directory
     * Convention should be to use this for reading resources and read() for files in the packages
     * @param path is the path to the file
     * @return the contents of the file
     * @throws IOException when something goes wrong reading the file
     */
    public static String readResource(String path) throws IOException{
        return read(path);
    }
}
