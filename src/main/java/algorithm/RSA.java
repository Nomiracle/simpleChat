package algorithm;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSA {
    public static byte[] signature(byte[] data, String privateKey) {
        byte[] result = null;
        try {
            byte[] keyBytes = Base64.decodeBase64(privateKey);
            // 构造PKCS8EncodedKeySpec对象
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            // 指定加密算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            // 取得私钥对象
            PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(priKey);
            signature.update(data);
            result = signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static boolean verify(byte[] data, String publicKey, byte[] sign) {
        try {
            byte[] publicKeyBytes = Base64.decodeBase64(publicKey);

            //构造X509EncodedKeySpec对象
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            //指定加密算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            //取公钥匙对象
            PublicKey pubKey = keyFactory.generatePublic(x509EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(pubKey);
            signature.update(data);

            //验证签名是否正常
            return signature.verify(sign);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static byte[] encrypt(byte[] data, String publicKey) {
        try {
            byte[] publicKeyBytes = Base64.decodeBase64(publicKey);

            //构造X509EncodedKeySpec对象
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            //指定加密算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            //取公钥匙对象
            PublicKey pubKey = keyFactory.generatePublic(x509EncodedKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decrypt(byte[] data, String privateKey) {
        try {
            byte[] privateKeyBytes = Base64.decodeBase64(privateKey);

            // 构造PKCS8EncodedKeySpec对象
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            //指定加密算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            //取公钥匙对象
            PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);

            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成密钥对
     * @return KeyPair
     */
    public static KeyPair pairGenerator() throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048,new SecureRandom());
        return keyPairGenerator.genKeyPair();
    }

}
