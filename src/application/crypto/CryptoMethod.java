package application.crypto;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class CryptoMethod {

	public static SecretKey generateKey(String algorithm, int keySize) throws CryptoException {
		KeyGenerator keyGenerator = null;

		try {
			keyGenerator = KeyGenerator.getInstance(algorithm);
			keyGenerator.init(keySize);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException("Error during the key generation, no such algorithm: " + algorithm);
		}

		return keyGenerator.generateKey();
	}

	public static SecretKey getSecretKey(String algorithm, String key) throws CryptoException {
		byte[] keyDecoded = null;

		try {
			keyDecoded = Hex.decodeHex(key.toCharArray());
		} catch (DecoderException e) {
			throw new CryptoException("Error during the key decoding");
		}

		return new SecretKeySpec(keyDecoded, 0, keyDecoded.length, algorithm);
	}

	public static KeyPair generateKeyPair(String algorithm, int keySize) throws CryptoException {
		KeyPairGenerator keyGenerator = null;

		try {
			keyGenerator = KeyPairGenerator.getInstance(algorithm);
			keyGenerator.initialize(keySize);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException("Error during the key pair generation, no such algorithm: " + algorithm);
		}

		return keyGenerator.generateKeyPair();
	}

	public static PublicKey getPublicKey(String algorithm, String modulus, String exp) throws CryptoException {
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(exp, 16));

		PublicKey key = null;

		try {
			key = KeyFactory.getInstance(algorithm).generatePublic(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e.getMessage());
		}

		return key;
	}

	public static PrivateKey getPrivateKey(String algorithm, String modulus, String exp) throws CryptoException {
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(modulus, 16), new BigInteger(exp, 16));

		PrivateKey key = null;

		try {
			key = KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e.getMessage());
		}

		return key;
	}

	public static byte[] crypt(String algorithm, Key key, int cryptMode, byte[] input) throws CryptoException {
		byte[] crypted = null;

		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(cryptMode, key);
			crypted = cipher.doFinal(input);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new CryptoException(e.getMessage());
		}

		return crypted;
	}

}
