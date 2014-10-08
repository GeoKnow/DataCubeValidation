/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author vukm
 */
public class ValidationSettingsWindow extends Window {
    private TextField endpointInput;
    private TextField graphInput;
    private final GridLayout rootLayout;
    
    public ValidationSettingsWindow(){
        setWidth("500px");
        rootLayout = new GridLayout();
        rootLayout.setColumns(2);
        rootLayout.setSizeFull();
        rootLayout.setColumnExpandRatio(1, 2.0f);
        rootLayout.setColumnExpandRatio(0, 0.0f);
        setContent(rootLayout);
    }

    @Override
    public void detach() {
        super.detach(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void attach() {
        rootLayout.addComponent(new Label("Endpoint"), 0, 0);
        endpointInput = new TextField();
        endpointInput.setWidth("100%");
        rootLayout.addComponent(endpointInput, 0, 1);
        rootLayout.addComponent(new Label("Graph"), 1, 0);
        graphInput = new TextField();
        graphInput.setWidth("100%");
        rootLayout.addComponent(graphInput, 1, 1);
    }
    
}
