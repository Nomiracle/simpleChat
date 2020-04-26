package algorithm;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class DES {

    public static String encrypt(String plaintStr,String keyStr) throws Exception {
        byte[] keyDataBytes = Base64.decodeBase64(keyStr);
        SecretKey originalKey = new SecretKeySpec(keyDataBytes, 0, keyDataBytes.length, "DES");
        Cipher ecipher = Cipher.getInstance("DES");
        ecipher.init(Cipher.ENCRYPT_MODE, originalKey);
        // Encode the string into bytes using utf-8
        byte[] utf8 = plaintStr.getBytes(StandardCharsets.UTF_8);
        // Encrypt
        byte[] enc = ecipher.doFinal(utf8);
        // Encode bytes to base64 to get a string
        return new sun.misc.BASE64Encoder().encode(enc);
    }
    public static String decrypt(String encryptStr,String keyStr) throws Exception {
        byte[] keyDataBytes = Base64.decodeBase64(keyStr);
        SecretKey originalKey = new SecretKeySpec(keyDataBytes, 0, keyDataBytes.length, "DES");
        Cipher dcipher = Cipher.getInstance("DES");
        dcipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(encryptStr);
        byte[] utf8 = dcipher.doFinal(dec);
        return new String(utf8, StandardCharsets.UTF_8);
    }

    public static SecretKey getSecretEncryptionKey() throws Exception{
        KeyGenerator generator = KeyGenerator.getInstance("DES");
        generator.init(new SecureRandom()); // The AES key size in number of bits
        return generator.generateKey();
    }
}
