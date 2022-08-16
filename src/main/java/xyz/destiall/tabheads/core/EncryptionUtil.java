package xyz.destiall.tabheads.core;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

public class EncryptionUtil {

    public static final int VERIFY_TOKEN_LENGTH = 4;
    public static final String KEY_PAIR_ALGORITHM = "RSA";

    private EncryptionUtil() {}

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
            keyPairGenerator.initialize(1_024);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            throw new ExceptionInInitializerError(nosuchalgorithmexception);
        }
    }

    public static byte[] generateVerifyToken(Random random) {
        byte[] token = new byte[VERIFY_TOKEN_LENGTH];
        random.nextBytes(token);
        return token;
    }

    public static String getServerIdHashString(String sessionId, SecretKey sharedSecret, PublicKey publicKey) {
        try {
            byte[] serverHash = getServerIdHash(sessionId, publicKey, sharedSecret);
            return (new BigInteger(serverHash)).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static SecretKey decryptSharedKey(PrivateKey privateKey, byte[] sharedKey) throws GeneralSecurityException {
        return new SecretKeySpec(decrypt(privateKey, sharedKey), "AES");
    }

    public static byte[] decrypt(PrivateKey key, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, key);
        return decrypt(cipher, data);
    }

    private static byte[] decrypt(Cipher cipher, byte[] data) throws GeneralSecurityException {
        return cipher.doFinal(data);
    }

    private static byte[] getServerIdHash(String sessionId, PublicKey publicKey, SecretKey sharedSecret) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(sessionId.getBytes(StandardCharsets.ISO_8859_1));
        digest.update(sharedSecret.getEncoded());
        digest.update(publicKey.getEncoded());
        return digest.digest();
    }
}
