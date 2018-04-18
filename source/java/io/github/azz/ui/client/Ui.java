/* ****************************************************************************************************************** *
 * Ui.java                                                                                                            *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import io.github.azz.ui.shared.FieldVerifier;

/**
 * Sample entrypoint module (from GWT SDK)
 * @author GWT SDK
 */
public class Ui implements EntryPoint {

	/**
	 * (Localizable) messages 
	 */
	private UiMessages msg = GWT.create(UiMessages.class);

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		Window.setTitle(msg._window_title() + " ");
		
		final Button sendButton = new Button(msg.sendBtnTxt());
		final TextBox nameField = new TextBox();
		nameField.setText(msg.defaultUserName());
		final Label errorLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("_body_title").add(new Label(msg._body_title()));
		RootPanel.get("_section_title_1").add(new HTML(msg._section_title_1()));
		RootPanel.get("nameFieldCaption").add(new Label(msg.nameFldCaption()));
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText(msg.dlgTitle());
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button(msg.dlgCloseBtnTxt());
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>" + msg.sendingNameToTheServer() + ":</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>" + msg.serverReplies() + ":</b>")); 
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText(msg.atLeast4()); 
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				greetingService.greetServer(textToServer, new AsyncCallback<String[]>() {
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						dialogBox.setText(msg.dlgTitle() + " - " + msg.failure());
						serverResponseLabel.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(msg.serverError());
						dialogBox.center();
						closeButton.setFocus(true);
					}

					public void onSuccess(String[] result) {
						dialogBox.setText(msg.dlgTitle());
						serverResponseLabel.removeStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(msg.greetingServiceServerReply(result[0], result[1], result[2]));
						dialogBox.center();
						closeButton.setFocus(true);
					}
				});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
	}
}
/* ****************************************************************************************************************** */
