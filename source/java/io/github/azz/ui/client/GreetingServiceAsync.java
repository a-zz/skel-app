/* ****************************************************************************************************************** *
 * GreetinServiceAsync.java                                                                                           *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Sample asynchronous RPC client stub (from GWT SDK)
 * @author GWT SDK
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<String[]> callback) throws IllegalArgumentException;
}
/* ****************************************************************************************************************** */
