/* ****************************************************************************************************************** *
 * UiMessages.java                                                                                                    *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.ui.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Ui - Localization interface
 * @author a-zz
 */
public interface UiMessages extends Messages {

	// -- Common HTML elements -----------------------------------------------------------------------------------------
	@DefaultMessage("Is it working?")
	String _windowTitle();
	
	// -- Control messages ---------------------------------------------------------------------------------------------
	@DefaultMessage("Initializing, please wait...")
	String _initializing();
	
	@DefaultMessage("Initializacion complete. Ready!")
	String _initializationComplete();
	
	@DefaultMessage("Updating, please wait...")
	String _updating();
	
	@DefaultMessage("Executing command from menu")
	String _executingMenuCmd();
	
	@DefaultMessage("An error occurred while attempting to contact the server")
	String _serverError();	 
		
	// -- UI elements --------------------------------------------------------------------------------------------------
	@DefaultMessage("Menu 1")
	String menu1();
	
	@DefaultMessage("Option 1.1")
	String menu1_1();
	
	@DefaultMessage("Option 1.2")
	String menu1_2();
	
	@DefaultMessage("Option 1.3")
	String menu1_3();
	
	@DefaultMessage("Option 1.4")
	String menu1_4();
	
	@DefaultMessage("Option 1.5")
	String menu1_5();
	
	@DefaultMessage("Option 1.6")
	String menu1_6();
	
	@DefaultMessage("Menu 2")
	String menu2();
	
	@DefaultMessage("Option 2.1")
	String menu2_1();
	
	@DefaultMessage("Option 2.2")
	String menu2_2();
	
	@DefaultMessage("Option 2.3")
	String menu2_3();
	
	@DefaultMessage("Option 2.4")
	String menu2_4();
	
	@DefaultMessage("Menu 3")
	String menu3();
	
	@DefaultMessage("Option 3.1")
	String menu3_1();
	
	@DefaultMessage("Option 3.2")
	String menu3_2();
	
	@DefaultMessage("Option 3.3")
	String menu3_3();
	
	@DefaultMessage("Option 3.4")
	String menu3_4();
	
	@DefaultMessage("Lorem Ipsum")
	String tab1();
	
	@DefaultMessage("Randomly generated Lorem Ipsum")
	String tab1Description();
	
	@DefaultMessage("Tab 2")
	String tab2();
	
	@DefaultMessage("Description for tab 2")
	String tab2Description();

	@DefaultMessage("Tab 3")
	String tab3();
	
	@DefaultMessage("Description for tab 3")
	String tab3Description();

	@DefaultMessage("Tab 4")
	String tab4();
	
	@DefaultMessage("Description for tab 4")
	String tab4Description();
	
	@DefaultMessage("Tab 5")
	String tab5();
	
	@DefaultMessage("Description for tab 5")
	String tab5Description();
	
	@DefaultMessage("Tab 6")
	String tab6();
	
	@DefaultMessage("Description for tab 6")
	String tab6Description();
	
	@DefaultMessage("Tab 7")
	String tab7();
	
	@DefaultMessage("Description for tab 7")
	String tab7Description();

	// -- Bussiness messages -------------------------------------------------------------------------------------------
	@DefaultMessage("Got some random Lorem Ipsum from server")
	String gotLoremIpsum();
}
/* ****************************************************************************************************************** */