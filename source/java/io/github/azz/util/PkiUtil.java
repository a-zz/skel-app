/* ****************************************************************************************************************** *
 * PkiUtil.java                                                                                                       *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;

/**
 * Utility class for Public Key Infraestructure related operations
 * @author a-zz
 */
public class PkiUtil {

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
	
	private final static String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
	private final static String END_CERTIFICATE = "-----END CERTIFICATE-----";

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
		
		return pemToX509(formatPem(base64cert));
	}
	
	/**
	 * Gets the PEM representation of a X509 certificate
	 * @param cert (X509Certificate)
	 * @return (String)
	 * @throws CertificateEncodingException
	 */
	public static String x509ToPem(X509Certificate cert) throws CertificateEncodingException {
		
		return formatPem(x509ToBase64(cert));
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
	 * Sanitizes the Base64-encoded PEM representation of a X509 certificate
	 * @param base64Pem (String) The Base64-encoded representation
	 * @return (String)
	 */
	public static String formatPem(String base64cert) {
		
		String result = "";
		
		// Normalization of input string
		String normalized = base64cert;
		if(normalized.startsWith(BEGIN_CERTIFICATE))
			normalized = normalized.substring(BEGIN_CERTIFICATE.length());
		if(normalized.endsWith(END_CERTIFICATE))
			normalized = normalized.substring(0, normalized.length()-END_CERTIFICATE.length());
		normalized = normalized.replaceAll("[^A-Za-z0-9\\+/=]", "");
		normalized = normalized.trim();
				
		// Line-wrapping	
		int linea = 0;
		for(int i = 0; i<normalized.length(); i++) {
			linea++;
			result += normalized.charAt(i);
			if(linea==64) {
				result += System.getProperty("line.separator");
				linea = 0;
			}
		}
		
		// Header and footer
		result = BEGIN_CERTIFICATE + 
				System.getProperty("line.separator") + 
				result + 
				System.getProperty("line.separator") + 
				END_CERTIFICATE;
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {

		System.out.println(getBase64Hash("hola".getBytes(), EnumHashAlg.SHA256));
		System.out.println(getBase64Hash("hola", EnumHashAlg.SHA256));
		System.out.println(getBase64Hash(new File("/home/DPA/a.suarez/Escritorio/Ahora/prueba1.txt"), EnumHashAlg.SHA256));
	}	
}
/* ****************************************************************************************************************** */