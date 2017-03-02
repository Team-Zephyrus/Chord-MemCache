package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class Hash {

	private static String FUNCTION_NAME = "SHA-1";

	private static byte[] HASHED_VALUE;
	
	private static int KEY_LENGTH = 160;


	public static void hash(String id){
		try {
			MessageDigest md = MessageDigest.getInstance(FUNCTION_NAME);
			md.reset();
			Hash.HASHED_VALUE = md.digest(id.getBytes());
			Hash.setKeyLength(HASHED_VALUE.length*8);
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void setFunctionName(String name){
		Hash.FUNCTION_NAME = name;
	}

	public static byte[] getHashedValue(){
		return Arrays.copyOf(Hash.HASHED_VALUE, HASHED_VALUE.length);
	}

	public static int getKeyLength() {
		return KEY_LENGTH;
	}

	public static void setKeyLength(int length) {
		KEY_LENGTH = length;
	}
	
	
	
}
