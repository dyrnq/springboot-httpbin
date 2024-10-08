package com.dyrnq.httpbin.component;

import jakarta.annotation.Nonnull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * Return string if not null, else specific default value.
     *
     * @param s            string
     * @param defaultValue default value
     * @return string if not null, or else default value
     */
    public static String ifNullSetDefault(String s, String defaultValue) {
        return s == null ? defaultValue : s;
    }

    /**
     * Choice an element from an array randomly.
     *
     * @param array target array
     * @param <T>   type of element
     * @return randomly chosen element
     */
    public static <T> T randomChoice(@Nonnull T[] array) {
        Random rand = new Random((new Date()).getTime());
        return array[rand.nextInt(array.length)];
    }

    /**
     * Join byte arrays into one byte array.
     *
     * @param delimiter  delimiter
     * @param byteArrays byte arrays
     * @return byte array
     */
    public static byte[] joinByteArrays(@Nonnull byte[] delimiter, @Nonnull byte[]... byteArrays) {
        if (byteArrays.length == 0) {
            return new byte[0];
        }
        if (byteArrays.length == 1) {
            return byteArrays[0];
        }
        byte[] result = byteArrays[0];
        for (int i = 1; i < byteArrays.length; ++i) {
            result = ByteBuffer.allocate(result.length + delimiter.length)
                    .put(result)
                    .put(delimiter)
                    .array();

            byte[] temp = byteArrays[i];
            result = ByteBuffer.allocate(result.length + temp.length)
                    .put(result)
                    .put(temp)
                    .array();
        }
        return result;
    }

    /**
     * Convert byte array to a hex string.
     *
     * @param bytes byte array
     * @return hex string
     */
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    /**
     * <a href="https://stackoverflow.com/questions/8571501/how-to-check-whether-a-string-is-base64-encoded-or-not">...</a>
     *
     * @param s
     * @return
     */
    public static boolean isBase64Encoded(String s) {
        String pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);
        return m.find();
    }

}
