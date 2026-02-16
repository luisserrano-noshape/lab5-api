package cncs.academy.ess.service;


import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PassUtil {


    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 128;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";


    public static String hashPass(String password) {
        try {

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            Base64.Encoder enc = Base64.getEncoder();
            return enc.encodeToString(salt) + ":" + enc.encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erro ao criar hash da password", e);
        }
    }

    public static boolean validatePassword(String originalPassword, String storedPassword) {
        try {

            String[] parts = storedPassword.split(":");
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);


            PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = skf.generateSecret(spec).getEncoded();


            int diff = storedHash.length ^ testHash.length;
            for (int i = 0; i < storedHash.length && i < testHash.length; i++) {
                diff |= storedHash[i] ^ testHash[i];
            }
            return diff == 0;

        } catch (Exception e) {
            return false;
        }
    }
}
