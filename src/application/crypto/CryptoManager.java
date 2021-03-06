package application.crypto;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import application.file.FileManager;

public class CryptoManager {

	private static final String SYMMETRIC_ALGORITHM = "AES";
	private static final String ASYMMETRIC_ALGORITHM = "RSA";
	private static final String HASH_METHOD = "SHA-1";

	private static final int DEFAULT_SYMM_KEY_SIZE = 128;
	private static final int DEFAULT_ASYMM_KEY_SIZE = 512;

	private FileManager fileManager = new FileManager();

	public CryptoManager() {
	}

	public void generateSymmetricKey(String keyFilePath, int keySize) throws IOException, CryptoException {
		// Generate a symmetric key
		String keyHex = CryptoMethod.generateKeyHex(SYMMETRIC_ALGORITHM, keySize);

		// Define its properties and write it to the specified file
		CryptoProperties keyProperties = new CryptoProperties();

		keyProperties.addProperty(CryptoProperties.DESCRIPTION, "Secret key");
		keyProperties.addProperty(CryptoProperties.METHOD, SYMMETRIC_ALGORITHM);
		keyProperties.addProperty(CryptoProperties.KEY_LENGTH, Integer.toHexString(keySize));
		keyProperties.addProperty(CryptoProperties.SECRET_KEY, keyHex);

		fileManager.writePropertiesToFile(keyProperties, keyFilePath);
	}

	public void encryptFileSymmetric(String inputFilePath, String outputFilePath, String keyFilePath)
			throws IOException, CryptoException {
		// Read all the data from the file to encrypt
		String data = fileManager.readFile(inputFilePath);

		// Read the secret key
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String key = keyProperties.valueAssembled(CryptoProperties.SECRET_KEY);
		SecretKey secretKey = CryptoMethod.getSecretKey(SYMMETRIC_ALGORITHM, key);

		// Encrypt the data
		String encryptedData = CryptoMethod.cryptBase64(SYMMETRIC_ALGORITHM, secretKey, Cipher.ENCRYPT_MODE,
				data.getBytes());

		// Define the file properties and write it to the specified file
		CryptoProperties properties = new CryptoProperties();

		properties.addProperty(CryptoProperties.DESCRIPTION, "Crypted file");
		properties.addProperty(CryptoProperties.METHOD, SYMMETRIC_ALGORITHM);
		properties.addProperty(CryptoProperties.FILE_NAME, inputFilePath);
		properties.addProperty(CryptoProperties.DATA, encryptedData);

		fileManager.writePropertiesToFile(properties, outputFilePath);
	}

	public void decryptFileSymmetric(String inputFilePath, String outputFilePath, String keyFilePath)
			throws IOException, CryptoException {
		// Read the encrypted data
		CryptoProperties fileProperties = fileManager.readPropertiesFromFile(inputFilePath);
		String data = fileProperties.valueAssembled(CryptoProperties.DATA);

		// Read the secret key
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String key = keyProperties.valueAssembled(CryptoProperties.SECRET_KEY);
		SecretKey secretKey = CryptoMethod.getSecretKey(SYMMETRIC_ALGORITHM, key);

		// Decrypt the data and write the content to the file
		byte[] encrypted = Base64.getDecoder().decode(data);
		byte[] decrypted = CryptoMethod.crypt(SYMMETRIC_ALGORITHM, secretKey, Cipher.DECRYPT_MODE, encrypted);

		fileManager.writeFile(outputFilePath, new String(decrypted));
	}

	public void generateAsymmetricKeys(String publicKeyFilePath, String privateKeyFilePath, int keySize)
			throws IOException, CryptoException {
		// Generate the key pair, public and private keys
		KeyPair keyPair = CryptoMethod.generateKeyPair(ASYMMETRIC_ALGORITHM, keySize);
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		// Define the public key properties
		CryptoProperties publicKeyProperties = new CryptoProperties();

		publicKeyProperties.addProperty(CryptoProperties.DESCRIPTION, "Public key");
		publicKeyProperties.addProperty(CryptoProperties.METHOD, ASYMMETRIC_ALGORITHM);
		publicKeyProperties.addProperty(CryptoProperties.KEY_LENGTH,
				Integer.toHexString(publicKey.getModulus().bitLength()));
		publicKeyProperties.addProperty(CryptoProperties.MODULUS, publicKey.getModulus().toString(16));
		publicKeyProperties.addProperty(CryptoProperties.PUBLIC_EXP, publicKey.getPublicExponent().toString(16));

		// Define the private key properties
		CryptoProperties privateKeyProperties = new CryptoProperties();

		privateKeyProperties.addProperty(CryptoProperties.DESCRIPTION, "Private key");
		privateKeyProperties.addProperty(CryptoProperties.METHOD, ASYMMETRIC_ALGORITHM);
		privateKeyProperties.addProperty(CryptoProperties.KEY_LENGTH,
				Integer.toHexString(privateKey.getModulus().bitLength()));
		privateKeyProperties.addProperty(CryptoProperties.MODULUS, privateKey.getModulus().toString(16));
		privateKeyProperties.addProperty(CryptoProperties.PRIVATE_EXP, privateKey.getPrivateExponent().toString(16));

		// Write the defined properties to the file
		fileManager.writePropertiesToFile(publicKeyProperties, publicKeyFilePath);
		fileManager.writePropertiesToFile(privateKeyProperties, privateKeyFilePath);

	}

	public void encryptFileAsymmetric(String inputFilePath, String outputFilePath, String keyFilePath)
			throws IOException, CryptoException {
		// Read all the data to be encrypted
		String data = fileManager.readFile(inputFilePath);

		// Read the public key from the file
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String modulus = keyProperties.valueAssembled(CryptoProperties.MODULUS);
		String exp = keyProperties.valueAssembled(CryptoProperties.PUBLIC_EXP);
		PublicKey key = CryptoMethod.getPublicKey(ASYMMETRIC_ALGORITHM, modulus, exp);

		// Encrypt the data
		String encryptedData = CryptoMethod.cryptBase64(ASYMMETRIC_ALGORITHM, key, Cipher.ENCRYPT_MODE,
				data.getBytes());

		// Define the file properties and write it to the specified file
		CryptoProperties properties = new CryptoProperties();

		properties.addProperty(CryptoProperties.DESCRIPTION, "Crypted file");
		properties.addProperty(CryptoProperties.METHOD, ASYMMETRIC_ALGORITHM);
		properties.addProperty(CryptoProperties.FILE_NAME, inputFilePath);
		properties.addProperty(CryptoProperties.DATA, encryptedData);

		fileManager.writePropertiesToFile(properties, outputFilePath);
	}

	public void decryptFileAsymmetric(String inputFilePath, String outputFilePath, String keyFilePath)
			throws IOException, CryptoException {
		// Read the encrypted data
		CryptoProperties fileProperties = fileManager.readPropertiesFromFile(inputFilePath);
		String data = fileProperties.valueAssembled(CryptoProperties.DATA);

		// Read the private key from the file
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String modulus = keyProperties.valueAssembled(CryptoProperties.MODULUS);
		String exp = keyProperties.valueAssembled(CryptoProperties.PRIVATE_EXP);
		PrivateKey key = CryptoMethod.getPrivateKey(ASYMMETRIC_ALGORITHM, modulus, exp);

		// Decrypt the data
		byte[] encrypted = Base64.getDecoder().decode(data);
		byte[] decrypted = CryptoMethod.crypt(ASYMMETRIC_ALGORITHM, key, Cipher.DECRYPT_MODE, encrypted);

		// Write the data to the file
		fileManager.writeFile(outputFilePath, new String(decrypted));
	}

	public String calculateHash(String inputFilePath, String outputFilePath) throws IOException {
		// Read the data and calculate the hash value
		String data = fileManager.readFile(inputFilePath);
		String hash = CryptoMethod.calculateHash(data);

		// Define the properties and write it to the file
		CryptoProperties properties = new CryptoProperties();

		properties.addProperty(CryptoProperties.DESCRIPTION, "File hash");
		properties.addProperty(CryptoProperties.METHOD, HASH_METHOD);
		properties.addProperty(CryptoProperties.FILE_NAME, inputFilePath);
		properties.addProperty(CryptoProperties.SIGNATURE, hash);

		fileManager.writePropertiesToFile(properties, outputFilePath);

		return hash;
	}

	public void createEnvelope(String inputFilePath, String keyFilePath, String envelopeFilePath)
			throws IOException, CryptoException {
		// Read the data to be encrypted
		String data = fileManager.readFile(inputFilePath);

		// Generate the symmetric key for data encryption
		SecretKey key = CryptoMethod.generateKey(SYMMETRIC_ALGORITHM, DEFAULT_SYMM_KEY_SIZE);
		String keyHex = CryptoMethod.keyToHex(key);

		// Encrypt the data
		String encryptedData = CryptoMethod.cryptBase64(SYMMETRIC_ALGORITHM, key, Cipher.ENCRYPT_MODE, data.getBytes());

		// Read the public key for the symmetric key encryption
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String modulus = keyProperties.valueAssembled(CryptoProperties.MODULUS);
		String exp = keyProperties.valueAssembled(CryptoProperties.PUBLIC_EXP);
		PublicKey publicKey = CryptoMethod.getPublicKey(ASYMMETRIC_ALGORITHM, modulus, exp);

		// Encrypt the symmetric key using the asymmetric algorithm
		String encryptedKey = CryptoMethod.cryptHex(ASYMMETRIC_ALGORITHM, publicKey, Cipher.ENCRYPT_MODE,
				keyHex.getBytes());

		// Write all the properties to the file
		CryptoProperties properties = new CryptoProperties();

		properties.addProperty(CryptoProperties.DESCRIPTION, "Envelope");
		properties.addProperty(CryptoProperties.METHOD, SYMMETRIC_ALGORITHM);
		properties.addProperty(CryptoProperties.METHOD, ASYMMETRIC_ALGORITHM);
		properties.addProperty(CryptoProperties.KEY_LENGTH, Integer.toHexString(DEFAULT_SYMM_KEY_SIZE));
		properties.addProperty(CryptoProperties.KEY_LENGTH, Integer.toHexString(DEFAULT_ASYMM_KEY_SIZE));
		properties.addProperty(CryptoProperties.ENVELOPE_DATA, encryptedData);
		properties.addProperty(CryptoProperties.ENVELOPE_CRYPT_KEY, encryptedKey);

		fileManager.writePropertiesToFile(properties, envelopeFilePath);
	}

	public void openEnvelope(String envelopeFilePath, String keyFilePath, String outputFilePath)
			throws IOException, CryptoException {
		// Read the encrypted data and key from the envelope
		CryptoProperties envelopeProperties = fileManager.readPropertiesFromFile(envelopeFilePath);
		String dataEncrypted = envelopeProperties.valueAssembled(CryptoProperties.ENVELOPE_DATA);
		String keyEncrypted = envelopeProperties.valueAssembled(CryptoProperties.ENVELOPE_CRYPT_KEY);

		// Read the private key
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String modulus = keyProperties.valueAssembled(CryptoProperties.MODULUS);
		String exp = keyProperties.valueAssembled(CryptoProperties.PRIVATE_EXP);
		PrivateKey privateKey = CryptoMethod.getPrivateKey(ASYMMETRIC_ALGORITHM, modulus, exp);

		// Use the private key to decrypt the symmetric key
		byte[] keyDecrypted = CryptoMethod.crypt(ASYMMETRIC_ALGORITHM, privateKey, Cipher.DECRYPT_MODE,
				DatatypeConverter.parseHexBinary(keyEncrypted));
		SecretKey key = CryptoMethod.getSecretKey(SYMMETRIC_ALGORITHM, new String(keyDecrypted));

		// Use the symmetric key to decrypt the data
		byte[] dataDecrypted = CryptoMethod.crypt(SYMMETRIC_ALGORITHM, key, Cipher.DECRYPT_MODE,
				Base64.getDecoder().decode(dataEncrypted));

		// Write the data to the file
		fileManager.writeFile(outputFilePath, new String(dataDecrypted));
	}

	public void createSignature(String inputFilePath, String keyFilePath, String signatureFilePath)
			throws IOException, CryptoException {
		// Read the file data and calculate its hash value
		String data = fileManager.readFile(inputFilePath);
		String hash = CryptoMethod.calculateHash(data);

		// Read the private key
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String modulus = keyProperties.valueAssembled(CryptoProperties.MODULUS);
		String exp = keyProperties.valueAssembled(CryptoProperties.PRIVATE_EXP);
		PrivateKey privateKey = CryptoMethod.getPrivateKey(ASYMMETRIC_ALGORITHM, modulus, exp);

		// Encrypt the hash with the private key
		String signature = CryptoMethod.cryptHex(ASYMMETRIC_ALGORITHM, privateKey, Cipher.ENCRYPT_MODE,
				hash.getBytes());

		CryptoProperties properties = new CryptoProperties();

		properties.addProperty(CryptoProperties.DESCRIPTION, "Signature");
		properties.addProperty(CryptoProperties.FILE_NAME, inputFilePath);
		properties.addProperty(CryptoProperties.METHOD, HASH_METHOD);
		properties.addProperty(CryptoProperties.METHOD, ASYMMETRIC_ALGORITHM);
		properties.addProperty(CryptoProperties.KEY_LENGTH, Integer.toHexString(hash.length() * 4));
		properties.addProperty(CryptoProperties.KEY_LENGTH, Integer.toHexString(DEFAULT_ASYMM_KEY_SIZE));
		properties.addProperty(CryptoProperties.SIGNATURE, signature);

		fileManager.writePropertiesToFile(properties, signatureFilePath);
	}

	public boolean verifySignature(String inputFilePath, String signatureFilePath, String keyFilePath)
			throws IOException, CryptoException {
		// Read the file data and calculate its hash value
		String data = fileManager.readFile(inputFilePath);
		String hash = CryptoMethod.calculateHash(data);

		// Read the public key
		CryptoProperties keyProperties = fileManager.readPropertiesFromFile(keyFilePath);
		String modulus = keyProperties.valueAssembled(CryptoProperties.MODULUS);
		String exp = keyProperties.valueAssembled(CryptoProperties.PUBLIC_EXP);
		PublicKey publicKey = CryptoMethod.getPublicKey(ASYMMETRIC_ALGORITHM, modulus, exp);

		// Read the encrypted hash value
		CryptoProperties signatureProperties = fileManager.readPropertiesFromFile(signatureFilePath);
		String signatureHex = signatureProperties.valueAssembled(CryptoProperties.SIGNATURE);

		// Decrypt the signature with the public key
		byte[] signature = CryptoMethod.crypt(ASYMMETRIC_ALGORITHM, publicKey, Cipher.DECRYPT_MODE,
				DatatypeConverter.parseHexBinary(signatureHex));

		// Check if the calculated hash value and the decrypted signature do
		// match
		return hash.equals(new String(signature));
	}

}
