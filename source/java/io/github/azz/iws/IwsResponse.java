/* ****************************************************************************************************************** *
 * IwsResponse.java                                                                                                   *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.iws;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.HashMap;

import io.github.azz.logging.AppLogger;
import io.github.azz.util.Base64Serializer;
import io.github.azz.util.PkiUtil;
import io.github.azz.util.PkiUtil.EnumHashAlg;

/**
 * JAX-WS Integration web service response container. Provides (optionally) digital signature (SHA256 with RSA) for peer 
 * 	authentication and data integrity verification. Data privacy isn't enforced, though; if needed, the web service 
 * 	must be published on a SSL connector provided at the servlet container level.
 * @author a-zz
 */
public class IwsResponse implements Serializable {

	private static final long serialVersionUID = 7299942101170710607L;
	private static final AppLogger logger = new AppLogger(IwsResponse.class);

	private HashMap<String,Serializable> payload = null;
	private HashMap<String,String> signatures = null;
		
	/**
	 * Constructor: loads data objects and (optionally) signs them for verification by the client
	 * @param payload (HashMap<String,Serializable>) A map of data objects to be loaded; the key can be freely assigned.
	 * @param signKey (PrivateKey) The key to be used for object signature. null if no signature is required.
	 * @throws Exception
	 */
	public IwsResponse(HashMap<String,Serializable> payload, PrivateKey signKey) throws Exception {
		
		this.payload = new HashMap<String,Serializable>();
		if(signKey!=null)
			this.signatures = new HashMap<String,String>();
		else
			logger.warn("No sign key provided, payload won't be signed");
		
		for(String key : payload.keySet()) {
			this.payload.put(key, payload.get(key));
			if(signKey!=null)
				this.signatures.put(key, PkiUtil.rsaSignObject(payload.get(key), signKey, EnumHashAlg.SHA256));
		}
	}
			
	/**
	 * Serializes the object as a Base64 String appropiate for a web service response.
	 * @return (String) 
	 * @throws IOException
	 */
	public String serialize() throws IOException {
		
		return Base64Serializer.serialize(this);
	}
	
	/**
	 * Deserializes a web service response into an object, optionally verifying the contained object signatures.
	 * @param serializedResponse (String) Web service response, Base64 representation.
	 * @param verifyKey (PublicKey) Key for signature verification. Can be null if object signature isn't required; but
	 * 	must be non-null if response data is actually signed.  
	 * @return (IwsResponse)
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws SignatureException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public static IwsResponse deserialize(String serializedResponse, PublicKey verifyKey) throws ClassNotFoundException, 
	IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
	
		IwsResponse result = (IwsResponse)Base64Serializer.deserializeAsObject(serializedResponse);
		
		if(result.signatures!=null) {
			if(verifyKey!=null) {
				for(String key : result.payload.keySet())
					if(!PkiUtil.rsaVerifyObjectSignature(result.payload.get(key), result.signatures.get(key), 
							EnumHashAlg.SHA256, verifyKey))
						throw new SignatureException("Invalid signature for object " + key);
			}
			else
				throw new SignatureException("Trying to deserialize signed data but no verify key is provided"); 
		}
		
		return result;
	}
	
	/**
	 * Returns the whole payload
	 * @return (HashMap<String,Serializable>)
	 */
	public HashMap<String,Serializable> getPayload() {
		
		return payload;
	}
	
	/**
	 * Returns a single object from the payload 
	 * @param key (String) Object key
	 * @return (Serializable)
	 */
	public Serializable getPayloadItem(String key) {
		
		return payload.get(key);
	}	
}
