/*
 * Copyright© 2015 yuxinlin. All rights reserved.
 */

package com.jiusit.common.utils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class DataCoding {
	private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
	private static final String DEF_KEY = "XcKi0k89";// 向量

	public static String encode(String encryptString) throws Exception {
		DESKeySpec dks = new DESKeySpec(DEF_KEY.getBytes());
		Key secretKey = SecretKeyFactory.getInstance("DES").generateSecret(dks);

		IvParameterSpec paramSpec = new IvParameterSpec(DEF_KEY.getBytes());

		Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
		byte[] bytes = cipher.doFinal(encryptString.getBytes("UTF-8"));
		return new String(Base64.encodeBase64(bytes), "UTF-8");
	}

	public static String encode(String encryptString, String encryptKey)
			throws Exception {
		DESKeySpec dks = new DESKeySpec(encryptKey.getBytes());
		Key secretKey = SecretKeyFactory.getInstance("DES").generateSecret(dks);

		IvParameterSpec paramSpec = new IvParameterSpec(encryptKey.getBytes());

		Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
		byte[] bytes = cipher.doFinal(encryptString.getBytes("UTF-8"));
		return new String(Base64.encodeBase64(bytes), "UTF-8");
	}

	public static String decode(String decrypt) throws Exception {
		return decode(decrypt.getBytes("UTF-8"), DEF_KEY);
	}

	public static String decode(String decrypt, String decryptKey)
			throws Exception {
		return decode(decrypt.getBytes("UTF-8"), decryptKey);
	}

	public static String decode(byte[] decryptBytes, String decryptKey)
			throws Exception {
		byte[] data = Base64.decodeBase64(decryptBytes);

		DESKeySpec dks = new DESKeySpec(decryptKey.getBytes());
		Key secretKey = SecretKeyFactory.getInstance("DES").generateSecret(dks);

		IvParameterSpec paramSpec = new IvParameterSpec(decryptKey.getBytes());

		Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
		return new String(cipher.doFinal(data), "UTF-8");
	}
}
