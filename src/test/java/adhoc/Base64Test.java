package adhoc;

import com.google.common.io.BaseEncoding;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class Base64Test {
    private static final Method JAVA_UTIL_BASE64_GETENCODER;

    static {
        Method getEncoderMethod;
        try {
            final Class<?> base64Class = Class.forName("java.util.Base64");
            getEncoderMethod = base64Class.getMethod("getEncoder");

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            getEncoderMethod = null;
        }
        JAVA_UTIL_BASE64_GETENCODER = getEncoderMethod;
    }

    static String base64EncodeToString(String s) {
        final byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
        if (JAVA_UTIL_BASE64_GETENCODER == null) {
            System.out.println("DBG: Java 7 in runtime");
            // Java 7 and older // TODO: remove this branch after switching to Java 8
            return DatatypeConverter.printBase64Binary(bytes);
        } else {
            System.out.println("DBG: Java 8 in runtime");
            // Java 8 and newer
            try {
                final Object encoder = JAVA_UTIL_BASE64_GETENCODER.invoke(null);
                final Class<?> encoderClass = encoder.getClass();
                final Method encodeMethod = encoderClass.getMethod("encode", byte[].class);
                final byte[] encodedBytes = (byte[]) encodeMethod.invoke(encoder, bytes);
                return new String(encodedBytes);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }


    private static final String TEST_STRING = "Hello:World";
    private static final String BASE64_EXPECTED = "SGVsbG86V29ybGQ=";

    @Test
    public void datatypeConverter() {
        final String result = DatatypeConverter.printBase64Binary(TEST_STRING.getBytes());
        System.out.println("B64 = " + result);
    }

    @Test
    public void j8Base64_getEncoder() {
        final String result = new String(java.util.Base64.getEncoder().encode(TEST_STRING.getBytes()));
        System.out.println("B64 = " + result);
    }

    @Test
    public void reflectiveApproach() {
        final String result = base64EncodeToString(TEST_STRING);
        System.out.println("B64 = " + result);
        Assert.assertEquals(BASE64_EXPECTED, result);
    }

    @Test
    public void guavab64() {
        final String result = BaseEncoding.base64().encode(TEST_STRING.getBytes());
        System.out.println("B64 = " + result);
        Assert.assertEquals(BASE64_EXPECTED, result);
    }
    @Test

    public void apacheB64() {
        final String result = Base64.encodeBase64String(TEST_STRING.getBytes());
        System.out.println("B64 = " + result);
        Assert.assertEquals(BASE64_EXPECTED, result);
    }
}
