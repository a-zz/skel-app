/* ****************************************************************************************************************** *
 * GreetinService.java                                                                                                *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Sample RPC client stub (from GWT SDK)
 * @author GWT SDK
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	String[] greetServer(String name) throws IllegalArgumentException;
}
/* ****************************************************************************************************************** */
