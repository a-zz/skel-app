/* ****************************************************************************************************************** *
 * LoremIpsumServiceAsync.java                                                                                        *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Sample asynchronous RPC client stub
 * @author a-zz
 */
public interface LoremIpsumServiceAsync {
	void getLoremIpsum(int paragraphs, int maxWordsPerPrgrph, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
}
/* ****************************************************************************************************************** */
