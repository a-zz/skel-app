/* ****************************************************************************************************************** *
 * Ui.java                                                                                                            *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.client;

import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * Sample entrypoint module
 * @author a-zz
 */
public class Ui implements EntryPoint {

	// (Localizable) messages 
	private UiMessages msg = GWT.create(UiMessages.class);

	/// Remote service proxy to talk to the server-side Lorem Ipsum generator
	private final LoremIpsumServiceAsync loremIpsumService = GWT.create(LoremIpsumService.class);
	
	// UI components
	private RootLayoutPanel rootCntnr;
	private SplitLayoutPanel mainCntnr;
	private DockLayoutPanel side;
	private StackLayoutPanel menu;
	private TabLayoutPanel workspace;
	private HTML statusArea;
	
	// A simple flag for initializacion control
	private boolean uiIsInitialized = false;
	
	// Entry point method
	public void onModuleLoad() {

		// Setting the window title
		Window.setTitle(msg._windowTitle() + " ");
			
		UiBuild();
		UiInitialize();
	}		
	
	// Builds the UI
	private void UiBuild() {
		
		side = new DockLayoutPanel(Unit.EM);
		side.addNorth(new HTML("[logo]"), 4);
		
		menu = buildMenu();
		side.add(menu);					
		
		workspace = buildWorkspaceTabContainer();
		statusArea = new HTML("");
		mainCntnr = new SplitLayoutPanel(4);
		mainCntnr.addWest(side, 0.1 * Window.getClientWidth());
		mainCntnr.addSouth(statusArea, 0.1 * Window.getClientHeight());
		mainCntnr.add(workspace);
		
		rootCntnr = RootLayoutPanel.get();
		rootCntnr.add(mainCntnr);		
	}
	
	// Runs initialization (post UI build) code
	private void UiInitialize() {

		LoremIpsumAsyncCallback loremIpsumResponse = new LoremIpsumAsyncCallback((HTML)workspace.getWidget(0));
		loremIpsumService.getLoremIpsum(3, 50, loremIpsumResponse);
		logToStatusArea(msg._initializationComplete());
		uiIsInitialized = true;
	}
	
	// Building the menu
	private StackLayoutPanel buildMenu() {
		
		StackLayoutPanel result = new StackLayoutPanel(Unit.EM);
		
		MenuBar menu1 = new MenuBar(true);
		menu1.addItem(msg.menu1_1(), new MenuCommandExecutor("1.1"));
		menu1.addItem(msg.menu1_2(), new MenuCommandExecutor("1.2"));
		menu1.addItem(msg.menu1_3(), new MenuCommandExecutor("1.3"));
		menu1.addItem(msg.menu1_4(), new MenuCommandExecutor("1.4"));
		menu1.addItem(msg.menu1_5(), new MenuCommandExecutor("1.5"));
		menu1.addItem(msg.menu1_6(), new MenuCommandExecutor("1.6"));		
		
		MenuBar menu2 = new MenuBar(true);
		menu2.addItem(msg.menu2_1(), new MenuCommandExecutor("2.1"));
		menu2.addItem(msg.menu2_2(), new MenuCommandExecutor("2.2"));
		menu2.addItem(msg.menu2_3(), new MenuCommandExecutor("2.3"));
		menu2.addItem(msg.menu2_4(), new MenuCommandExecutor("2.4"));
		
		MenuBar menu3 = new MenuBar(true);
		menu3.addItem(msg.menu3_1(), new MenuCommandExecutor("3.1"));
		menu3.addItem(msg.menu3_2(), new MenuCommandExecutor("3.2"));
		menu3.addItem(msg.menu3_3(), new MenuCommandExecutor("3.3"));
		menu3.addItem(msg.menu3_4(), new MenuCommandExecutor("3.4"));
				
		int hdrHeight = 2;
		int maxSubMenuItemCount = 6;
		result.add(menu1, msg.menu1(), hdrHeight);
		result.add(menu2, msg.menu2(), hdrHeight);
		result.add(menu3, msg.menu3(), hdrHeight);
		result.setHeight((2 * maxSubMenuItemCount) + ( + result.getWidgetCount() * hdrHeight) + "em");
		
		return result;
	}
	
	// Building the workspace tab container
	private TabLayoutPanel buildWorkspaceTabContainer() {
		
		TabLayoutPanel result = new TabLayoutPanel(2, Unit.EM);
		
		result.add(new HTML("<p>" + msg._initializing() + "</p>"), msg.tab1());
		result.getTabWidget(0).setTitle(msg.tab1Description());
		result.add(new HTML(msg.tab2()), msg.tab2());
		result.getTabWidget(1).setTitle(msg.tab2Description());
		result.add(new HTML(msg.tab3()), msg.tab3());
		result.getTabWidget(2).setTitle(msg.tab3Description());
		result.add(new HTML(msg.tab4()), msg.tab4());
		result.getTabWidget(3).setTitle(msg.tab4Description());
		result.add(new HTML(msg.tab5()), msg.tab5());
		result.getTabWidget(4).setTitle(msg.tab5Description());
		result.add(new HTML(msg.tab6()), msg.tab6());
		result.getTabWidget(5).setTitle(msg.tab6Description());
		result.add(new HTML(msg.tab7()), msg.tab7());
		result.getTabWidget(6).setTitle(msg.tab7Description());
		result.addSelectionHandler(new WorkSpaceTabSelectionHandler());
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	private void logToStatusArea(String message) {

		// TODO Write a shared date formatter
		Date now = new Date();
		int h = now.getHours();
		int m = now.getMinutes();
		int s = now.getSeconds();
		String time = (h<10?"0"+h:""+h) + ":" + (m<10?"0"+m:""+m) + ":" + (s<10?"0"+s:""+s);	
		statusArea.setHTML(time + " - " + message + "<br/>" + statusArea.getHTML());
	}
	
	// Callback handler for Lorem Ipsum service
	private class LoremIpsumAsyncCallback implements AsyncCallback<String> {
		
		private HTML target;
		
		LoremIpsumAsyncCallback(HTML target) {
			
			this.target = target;
		}

		@Override
		public void onFailure(Throwable caught) {
			
			target.setHTML(msg._serverError() + ": " + caught.getMessage());
			logToStatusArea(msg._serverError() + ": " + caught.getMessage());
		}

		@Override
		public void onSuccess(String result) {
			
			target.setHTML(result);
			logToStatusArea(msg.gotLoremIpsum());

		}		
	}
	
	// Selection handler for workspace tab container
	private class WorkSpaceTabSelectionHandler implements SelectionHandler<Integer> {

		@Override
		public void onSelection(SelectionEvent<Integer> event) {
						
			if(workspace.getSelectedIndex()==0) {
				
				HTML targetWidget = (HTML)workspace.getWidget(0);
				targetWidget.setHTML("<p>" + msg._updating() + "</p>");
				LoremIpsumAsyncCallback loremIpsumResponse = new LoremIpsumAsyncCallback(targetWidget);
				loremIpsumService.getLoremIpsum(3, 50, loremIpsumResponse);							
			}
		}
	}
	
	// Command executor for the menu options
	private class MenuCommandExecutor implements ScheduledCommand {
		
		private String commandCode;
		
		public MenuCommandExecutor(String code) {
			
			commandCode = code;
		}
		
		@Override
		public void execute() {
			
			if(uiIsInitialized)			
				logToStatusArea(msg._executingMenuCmd() + ": " + commandCode);
		}	
	}
}
/* ****************************************************************************************************************** */
