/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ken.kenutils.securityUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 *
 * @author K3N
 */
public class PasswordManager {
	
	private String ADMIN_PWD = "kenrig";
    private String ADMIN_UNAME = "arunken";
    
    public static String get_SHA_1_SecurePassword(String passwordToHash, byte[] salt)
    {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(salt);
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }
     
    public static String get_SHA_256_SecurePassword(String passwordToHash, byte[] salt)
    {
        //Use MessageDigest md = MessageDigest.getInstance("SHA-256");
        return "";
    }
     
    public static String get_SHA_384_SecurePassword(String passwordToHash, byte[] salt)
    {
        //Use MessageDigest md = MessageDigest.getInstance("SHA-384");
        return "";
    }
     
    public static String get_SHA_512_SecurePassword(String passwordToHash, byte[] salt)
    {
        //Use MessageDigest md = MessageDigest.getInstance("SHA-512");
        return "";
    }
     
    //Add salt
    public static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
    
}
