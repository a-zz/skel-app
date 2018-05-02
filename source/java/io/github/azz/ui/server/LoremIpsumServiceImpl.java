/* ****************************************************************************************************************** *
 * LoremIpsumServiceImpl.java                                                                                         *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import io.github.azz.logging.AppLogger;
import io.github.azz.ui.client.LoremIpsumService;
import io.github.azz.util.StringUtil;

/**
 * Sample RPC service (from GWT SDK)
 * @author GWT SDK
 */
@SuppressWarnings("serial")
public class LoremIpsumServiceImpl extends RemoteServiceServlet implements LoremIpsumService {

	private static AppLogger logger = new AppLogger(LoremIpsumServiceImpl.class);
	
	public String getLoremIpsum(int paragraphs, int maxWordsPerPrgrph) throws IllegalArgumentException {
		
		logger.info("Lorem ipsum requested");
		String result = StringUtil.loremIpsum(paragraphs, maxWordsPerPrgrph, true);
		logger.debug("Lorem ipsum produced: " + result);
		return result;
	}
}
/* ****************************************************************************************************************** */
