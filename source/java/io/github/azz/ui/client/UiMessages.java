package io.github.azz.ui.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface UiMessages extends Messages {

	// Internal messages
	@DefaultMessage("An error occurred while attempting to contact the server. "
			+ "Please check your network connection and try again.")
	String serverError();
	
	// Common HTML elements
	@DefaultMessage("Is it working?")
	String _window_title();
	
	@DefaultMessage("Let''s test whether it works...")
	String _body_title();
	
	@DefaultMessage("(Sample & simple app from <a href=\"http://www.gwtproject.org\">GWT</a> SDK)")
	SafeHtml _section_title_1();
	
	// UI module messages
	@DefaultMessage("Send")
	String sendBtnTxt();
	
	@DefaultMessage("Please enter your name:")
	String nameFldCaption();
	
	@DefaultMessage("GWT user")
	String defaultUserName();
	
	@DefaultMessage("Remote procedure call")
	String dlgTitle();
	
	@DefaultMessage("Close")
	String dlgCloseBtnTxt();
	
	@DefaultMessage("Hello, {0}!<br><br>"
			+ "I am running {1}.<br><br>"
			+ "It looks like you are using:<br>{2}")
	SafeHtml greetingServiceServerReply(String userName, String serverInfo, String userAgent);
	
	@DefaultMessage("Sending name to the server")
	String sendingNameToTheServer();
	
	@DefaultMessage("Server replies")
	String serverReplies();
	
	@DefaultMessage("Failure")
	String failure();
	
	@DefaultMessage("Please enter at least four characters")
	String atLeast4();
}
