/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import org.openrdf.repository.Repository;

/**
 *
 * @author vukm
 */
public class ValidationComponent extends CustomComponent {
    
    private final Repository repository;
    private HorizontalLayout headerLayout;
    private Button btnSettings;
    private Button btnClearAll;
    private Button btnEvalAll;
    private final VerticalLayout rootLayout;
    private Tree criteriaTree;
    
    public ValidationComponent(Repository repository){
        this.repository = repository;
        setSizeFull();
        rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        setCompositionRoot(rootLayout);
    }
    
    private void createUI(){
        rootLayout.removeAllComponents();
        createHeader();
        createListeners();
    }
    
    private void createHeader(){
        headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setSpacing(true);
        headerLayout.addStyleName("header");
        rootLayout.addComponent(headerLayout);
        
        Label lbl = new Label("Data Cube Validation");
        headerLayout.addComponent(lbl);
        headerLayout.setExpandRatio(lbl, 2.0f);
        headerLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        
        btnSettings = new Button("Settings");
        btnClearAll = new Button("Clear");
        btnEvalAll = new Button("Evaluate All");
        headerLayout.addComponent(btnClearAll);
        headerLayout.addComponent(btnEvalAll);
        headerLayout.addComponent(btnSettings);
    }
    
    private void createConstraints(){
        criteriaTree = new Tree("Validation criteria");
        criteriaTree.setNullSelectionAllowed(false);
        criteriaTree.setImmediate(true);
        // TODO create tree items
    }
    
    private void createListeners(){
        btnSettings.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Notification.show("Settings pressed!");
            }
        });
    }

    @Override
    public void detach() {
        super.detach(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void attach() {
        createUI();
    }
    
}
