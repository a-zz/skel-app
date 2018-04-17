/* ****************************************************************************************************************** *
 * GreetinServiceImpl.java                                                                                            *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.server;

import io.github.azz.logging.AppLogger;
import io.github.azz.ui.client.GreetingService;
import io.github.azz.ui.shared.FieldVerifier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Sample RPC service (from GWT SDK)
 * @author GWT SDK
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

	// TODO Maybe this should go into a common super-class...?
	private static AppLogger logger = new AppLogger(GreetingServiceImpl.class);

	public String greetServer(String input) throws IllegalArgumentException {

		logger.info(">>> greetServer()");

		// Verify that the input is valid.
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back
			// to
			// the client.
			throw new IllegalArgumentException("Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script
		// vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		String result = "Hello, " + input + "!<br><br>I am running " + serverInfo +
				".<br><br>It looks like you are using:<br>" + userAgent;

		logger.debug("<<< greetServer(): " + result);

		return result;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html
	 *            the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}
}
/* ****************************************************************************************************************** */
