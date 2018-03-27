/* ****************************************************************************************************************** *
 * PkiUtil.java                                                                                                       *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.crypto.SecretKey;

import io.github.azz.logging.AppLogger;

/**
 * Utility class for Public Key Infraestructure related operations
 * @author a-zz
 */
public class PkiUtil {

	private static final AppLogger logger = new AppLogger(PkiUtil.class); 
	
	/**
	 * Supported hash algorithms: SHA1, SHA256, SHA384, SHA512
	 */
	public enum EnumHashAlg { SHA1, SHA256, SHA384, SHA512 };
	
	private static final HashMap<EnumHashAlg,String> hashAlgMap;
	static {
		hashAlgMap = new HashMap<EnumHashAlg,String>();
		hashAlgMap.put(EnumHashAlg.SHA1, 	"SHA-1");
		hashAlgMap.put(EnumHashAlg.SHA256, 	"SHA-256");
		hashAlgMap.put(EnumHashAlg.SHA384, 	"SHA-384");
		hashAlgMap.put(EnumHashAlg.SHA512, 	"SHA-512");
	}
	
	/**
	 * PEM-encodable data types as per RFC 7468 (https://tools.ietf.org/html/rfc7468) and other sources (including 
	 * 	some de-facto-standard types). 
	 */
	public enum EnumPemDataTypes {
		CERTIFICATE, 
		X509_CRL, 
		CERTIFICATE_REQUEST, 
		PKCS7, 
		CMS, 
		PRIVATE_KEY, 
		ENCRYPTED_PRIVATE_KEY, 
		ATTRIBUTE_CERTIFICATE, 
		PUBLIC_KEY, 
		NO_TYPE};
	// Type labels for the data types enumerated above
	private final static HashMap<EnumPemDataTypes,String> begin = new HashMap<>();
	private final static HashMap<EnumPemDataTypes,String> end =   new HashMap<>();
	static {
		begin.put(EnumPemDataTypes.CERTIFICATE, 			"-----BEGIN CERTIFICATE-----");
		end.put(  EnumPemDataTypes.CERTIFICATE, 			"-----END CERTIFICATE-----");
		
		begin.put(EnumPemDataTypes.X509_CRL, 				"-----BEGIN X509 CRL-----");
		end.put(  EnumPemDataTypes.X509_CRL, 				"-----END X509 CRL-----");
		
		begin.put(EnumPemDataTypes.CERTIFICATE_REQUEST, 	"-----BEGIN CERTIFICATE REQUEST-----");
		end.put(  EnumPemDataTypes.CERTIFICATE_REQUEST, 	"-----END CERTIFICATE REQUEST-----");
		
		begin.put(EnumPemDataTypes.PKCS7, 					"-----BEGIN PKCS7-----");
		end.put(  EnumPemDataTypes.PKCS7, 					"-----END PKCS7-----");
		
		begin.put(EnumPemDataTypes.CMS, 					"-----BEGIN CMS-----");
		end.put(  EnumPemDataTypes.CMS, 					"-----END CMS-----");
		
		begin.put(EnumPemDataTypes.PRIVATE_KEY, 			"-----BEGIN PRIVATE KEY-----");
		end.put(  EnumPemDataTypes.PRIVATE_KEY, 			"-----END PRIVATE KEY-----");
		
		begin.put(EnumPemDataTypes.ENCRYPTED_PRIVATE_KEY,	"-----BEGIN ENCRYPTED PRIVATE KEY-----");
		end.put(  EnumPemDataTypes.ENCRYPTED_PRIVATE_KEY, 	"-----END ENCRYPTED PRIVATE KEY-----");
		
		begin.put(EnumPemDataTypes.ATTRIBUTE_CERTIFICATE, 	"-----BEGIN ATTRIBUTE CERTIFICATE-----");
		end.put(  EnumPemDataTypes.ATTRIBUTE_CERTIFICATE, 	"-----END ATTRIBUTE CERTIFICATE-----");
		
		begin.put(EnumPemDataTypes.PUBLIC_KEY, 				"-----BEGIN PUBLIC KEY-----");
		end.put(  EnumPemDataTypes.PUBLIC_KEY, 				"-----END PUBLIC KEY-----");	
		
		begin.put(EnumPemDataTypes.NO_TYPE, 				"-----BEGIN-----");
		end.put(  EnumPemDataTypes.NO_TYPE, 				"-----END-----");
	}

	/**
	 * Makes a key-value map from a certificate subject field
	 * @param subject (String)
	 * @return (HashMap<String,String>)
	 */
	public static HashMap<String,String> getCertSubjectMap(String subject) {
		
		HashMap<String,String> result = new HashMap<>();
		String[] pairs = subject.trim().split(",");
		for(String pair : pairs) {
			String[] splittedPair = pair.split("=");
			result.put(splittedPair[0], splittedPair[1]);
		}
		return result;
	}
			
	/**
	 * Gets the Base64-enconded hash of some binary content
	 * @param input (byte[]) The original binary content
	 * @param hashAlg (EnumHashAlg) The hash algorithm to be used 
	 * @return (String) 
	 * @throws NoSuchAlgorithmException
	 */
	public static String getBase64Hash(byte[] input, EnumHashAlg hashAlg) 
			throws NoSuchAlgorithmException {
		
        MessageDigest md = MessageDigest.getInstance(hashAlgMap.get(hashAlg));
        byte[] hash = md.digest(input);
        return Base64Serializer.serialize(hash);
	}	
	
	/**
	 * Gets the Base64-enconded hash of a string
	 * @param input (String) The original string
	 * @param hashAlg (EnumHashAlg) The hash algorithm to be used 
	 * @return (String) 
	 * @throws NoSuchAlgorithmException
	 */
	public static String getBase64Hash(String input, EnumHashAlg hashAlg) 
			throws NoSuchAlgorithmException {		
        
		return getBase64Hash(input.getBytes(), hashAlg);
	}
	
	/**
	 * Gets the Base64-enconded hash of a file
	 * @param input (File) The original file
	 * @param hashAlg (EnumHashAlg) The hash algorithm to be used 
	 * @return (String) 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException 
	 */
	public static String getBase64Hash(File input, EnumHashAlg hashAlg) 
			throws NoSuchAlgorithmException, IOException {
		
        return getBase64Hash(FileUtil.readBinary(input), hashAlg);
	}
		
	/**
	 * Gets a X509 certificate from its PEM representation
	 * @param pem (String)
	 * @return (X509Certificate)
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static X509Certificate pemToX509(String pem) throws CertificateException, IOException {
		
		ByteArrayInputStream bais = null;
		try {
			pem = pem.trim();		
			bais = new ByteArrayInputStream(pem.getBytes());
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(bais);
			return cert;
		}
		finally {
			if(bais!=null)
				bais.close();
		}
	}
		
	/**
	 * Gets a X509 certificate from its Base64 encoding
	 * @param base64cert (String)
	 * @return (X509Certificate)
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static X509Certificate base64ToX509(String base64cert) throws CertificateException, IOException {
		
		return pemToX509(formatPem(EnumPemDataTypes.CERTIFICATE, base64cert));
	}
	
	/**
	 * Gets the PEM representation of a X509 certificate
	 * @param cert (X509Certificate)
	 * @return (String)
	 * @throws CertificateEncodingException
	 */
	public static String x509ToPem(X509Certificate cert) throws CertificateEncodingException {
		
		return formatPem(EnumPemDataTypes.CERTIFICATE, x509ToBase64(cert));
	}
	
	/**
	 * Encodes a certificate in Base64 
	 * @param cert (X509Certificate)
	 * @return (String) Base64 enconding of the certificate. In order to get a normalized PEM string, 
	 * 	formatPemCerticate() may be used. 
	 * @throws CertificateEncodingException
	 * @see formatPem
	 */
	public static String x509ToBase64(X509Certificate cert) throws CertificateEncodingException {
		
		return Base64Serializer.serialize(cert.getEncoded());
	}	
	
	/**
	 * Sanitizes the Base64-encoded PEM representation of an object (certificate, key, etc.)
	 * @param base64data (String) The dirty Base64-encoded representation
	 * @return (String)
	 */
	public static String formatPem(EnumPemDataTypes pemDataType, String base64data) {
		
		String result = "";
		
		// Normalization of input string
		String normalized = base64data;
		if(normalized.startsWith(begin.get(pemDataType)))
			normalized = normalized.substring(begin.get(pemDataType).length());
		if(normalized.endsWith(end.get(pemDataType)))
			normalized = normalized.substring(0, normalized.length()-end.get(pemDataType).length());
		normalized = normalized.replaceAll("[^A-Za-z0-9\\+/=]", "");
		normalized = normalized.trim();
		result = StringUtil.lineWrap(normalized, 64);
		
		// Header and footer type labels
		result = begin.get(pemDataType) + 
				 System.getProperty("line.separator") + 
				 result + 
				 System.getProperty("line.separator") + 
				 end.get(pemDataType);
		
		return result;
	}

	/**
	 * Gets a private key from a key store. JKS key store format supported so far. 
	 * @param keyStoreFile (File) The file holding the key store.
	 * @param keyStorePassword (String) The key store password. null or empty string if it isn't password protected.
	 * 	<br/><br/>Recent versions of Java keytool will refuse to create passwordless key stores; although that still
	 * 	can be done programmatically, it's to be considered bad practice.
	 * @param keyAlias (String) The key alias.   
	 * @param keyPassword (String) The key password; null if it matches the key store password (common practice). Both
	 * 	keyStorePassword and keyPassword can't be null, though.
	 * @return (PrivateKey)
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws UnrecoverableEntryException
	 */
	public static PrivateKey getPrivateKeyFromKeyStore(File keyStoreFile, String keyStorePassword, String keyAlias,
			String keyPassword) throws KeyStoreException, CertificateException, FileNotFoundException, 
			NoSuchAlgorithmException, IOException, UnrecoverableEntryException {
			
		KeyStore keyStore = openKeyStore(keyStoreFile, keyStorePassword);
		if(keyPassword==null)
			keyPassword = keyStorePassword;
		PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(keyAlias, 
				new KeyStore.PasswordProtection(keyPassword.toCharArray()));
		PrivateKey result = entry.getPrivateKey();
		logger.trace("Got private key for alias " + keyAlias);
		return result;
	}
	
	/**
	 * Gets a certificate from a key store. JKS key store format supported so far.
	 * @param keyStoreFile (File) The file holding the key store.
	 * @param keyStorePassword (String) The key store password. null or empty string if it isn't password protected.
	 * 	<br/><br/>Recent versions of Java keytool will refuse to create passwordless key stores; although that still
	 * 	can be done programmatically, it's to be considered bad practice.
	 * @param certificateAlias (String) The certificate alias.
	 * @return (X509Certificate)
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws UnrecoverableEntryException
	 */
	public static X509Certificate getCertificateFromKeyStore(File keyStoreFile, String keyStorePassword, 
			String certificateAlias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, 
			IOException {
			
		KeyStore keyStore = openKeyStore(keyStoreFile, keyStorePassword);
		X509Certificate result = (X509Certificate)keyStore.getCertificate(certificateAlias);
		logger.trace("Got certificate for alias " + certificateAlias + ": " + result.getSubjectDN().toString());
		return result;
	}
	
	/**
	 * Gets a secret (symmetric) key from a key store. JKS format supported so far. 
	 * @param keyStoreFile (File) The file holding the key store.
	 * @param keyStorePassword (String) The key store password. null or empty string if it isn't password protected.
	 * 	<br/><br/>Recent versions of Java keytool will refuse to create passwordless key stores; although that still
	 * 	can be done programmatically, it's to be considered bad practice.
	 * @param keyAlias (String) The key alias.
	 * @param keyPassword (String) The key password; null if it matches the key store password (common practice). Both
	 * 	keyStorePassword and keyPassword can't be null, though.
	 * @return (PrivateKey)
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws UnrecoverableEntryException
	 */	
	public static SecretKey getSecretKeyFromKeyStore(File keyStoreFile, String keyStorePassword, String keyAlias,
			String keyPassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, 
			IOException, UnrecoverableEntryException {
			
		KeyStore keyStore = openKeyStore(keyStoreFile, keyStorePassword);
		if(keyPassword==null)
			keyPassword = keyStorePassword;
		SecretKeyEntry entry = (KeyStore.SecretKeyEntry)keyStore.getEntry(keyAlias, 
				new KeyStore.PasswordProtection(keyPassword.toCharArray()));
		SecretKey result = entry.getSecretKey();
		logger.trace("Got secret key for alias" + keyAlias);
		return result;
	}	
	
	private static KeyStore openKeyStore(File keyStoreFile, String keyStorePassword) throws KeyStoreException, 
	CertificateException, NoSuchAlgorithmException, IOException {
		
		KeyStore result = KeyStore.getInstance("JKS");
		char[] password = null;
		if(keyStorePassword!=null && !keyStorePassword.equals(""))
			password = keyStorePassword.toCharArray();
		result.load(new FileInputStream(keyStoreFile), password);
		logger.trace("Key store loaded from file " + keyStoreFile);
		return result;
	}
}
/* ****************************************************************************************************************** */