package dev.amble.lib.skin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SkinConstants {
	public static final String SKIN_URL = "https://mineskin.eu/skin/";

	public static String encodeURL(String input) {
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        byte[] hash = md.digest(input.getBytes());
	        StringBuilder hexString = new StringBuilder();
	        for (byte b : hash) {
	            String hex = Integer.toHexString(0xff & b);
	            if (hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }
	        return hexString.toString();
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException(e);
	    }
	}
}
