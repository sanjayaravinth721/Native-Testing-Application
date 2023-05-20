// package com;

// import java.nio.charset.StandardCharsets;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;


// import org.apache.catalina.CredentialHandler;

// public class PasswordCredentialHandler implements CredentialHandler {

//     @Override
//     public boolean matches(String inputCredentials, String storedCredentials) {
//         return storedCredentials.equals(hashPassword(inputCredentials));
//     }

//     @Override
//     public String mutate(String userCredentials) {
//         return hashPassword(userCredentials);
//     }

//     private String hashPassword(String password) {
//         try {
//             MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
//             byte[] hash = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
//             StringBuilder stringBuilder = new StringBuilder();
//             for (int i = 0; i < hash.length; i++) {
//                 String hex = Integer.toHexString(0xff & hash[i]);
//                 if (hex.length() == 1) stringBuilder.append('0');
//                 stringBuilder.append(hex);
//             }
//             return stringBuilder.toString();
//         } catch (NoSuchAlgorithmException e) {
//             throw new RuntimeException(e);
//         }
//     }
// }