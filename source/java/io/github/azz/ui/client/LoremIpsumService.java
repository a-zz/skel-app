/* ****************************************************************************************************************** *
 * LoremIpsumService.java                                                                                             *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Sample RPC client stub
 * @author a-zz
 */
@RemoteServiceRelativePath("loremIpsum") // As referred in WEB-INF/web.xml
public interface LoremIpsumService extends RemoteService {
	public String getLoremIpsum(int paragraphs, int maxWordsPerPrgrph) throws IllegalArgumentException;
}
/* ****************************************************************************************************************** */
