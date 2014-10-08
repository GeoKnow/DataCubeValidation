/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashMap;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.gui.constraints.IC1;
import rs.pupin.jpo.validation.gui.constraints.IC2;
import rs.pupin.jpo.validation.gui.constraints.IntegrityConstraintComponent;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQueryListener;

/**
 *
 * @author vukm
 */
public class ValidationComponent extends CustomComponent implements ICQueryListener {
    
    private final Repository repository;
    private String graph;
    private HorizontalLayout headerLayout;
    private Button btnSettings;
    private Button btnClearAll;
    private Button btnEvalAll;
    private final VerticalLayout rootLayout;
    private Tree criteriaTree;
    private HorizontalSplitPanel splitPanel;
    private VerticalLayout contentLayout;
    private final HashMap<ICQuery, IntegrityConstraintComponent> icHash;
    
    public ValidationComponent(Repository repository, String graph){
        this.repository = repository;
        this.graph = graph;
        this.icHash = new HashMap<ICQuery, IntegrityConstraintComponent>();
        setSizeFull();
        rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        setCompositionRoot(rootLayout);
    }
    
    private void createUI(){
        rootLayout.removeAllComponents();
        icHash.clear();
        createHeader();
        splitPanel = new HorizontalSplitPanel();
        splitPanel.setSizeFull();
        rootLayout.addComponent(splitPanel);
        rootLayout.setExpandRatio(splitPanel, 2.0f);
        createConstraints();
        splitPanel.setFirstComponent(criteriaTree);
        splitPanel.setSplitPosition(400, Unit.PIXELS);
        contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        splitPanel.setSecondComponent(contentLayout);
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
        criteriaTree.setWidth("100%");
        // TODO create tree items
        IC1 ic1 = new IC1(repository, graph);
        addIC(ic1);
        addIC(new IC2(repository, graph));
        
        criteriaTree.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ICQuery ic = (ICQuery)event.getProperty().getValue();
                ic.eval();
            }
        });
    }
    
    private void addIC(IntegrityConstraintComponent component){
        ICQuery ic = component.getIcQuery();
        criteriaTree.addItem(ic);
        criteriaTree.setItemCaption(ic, component.getName());
        criteriaTree.setItemIcon(ic, component.getIcon());
        criteriaTree.setChildrenAllowed(ic, false);
        ic.addQueryListener(this);
        icHash.put(ic, component);
    }
    
    private void createListeners(){
        btnSettings.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Notification.show("Settings pressed!");
                Window settingsWindow = new Window();
                settingsWindow.setModal(true);
                settingsWindow.setWidth("500px");
                settingsWindow.setHeight("300px");
                GridLayout settingsLayout = new GridLayout(2,2);
//                settingsLayout.setColumns(2);
//                settingsLayout.setRows(2);
                settingsLayout.setSizeFull();
                settingsLayout.setColumnExpandRatio(1, 2.0f);
                settingsLayout.setColumnExpandRatio(0, 0.0f);
                settingsWindow.setContent(settingsLayout);
                Label lbl = new Label("Endpoint");
                lbl.setWidth("100px");
                settingsLayout.addComponent(lbl,0,0,0,0);
                TextField endpointInput = new TextField();
                endpointInput.setWidth("100%");
                settingsLayout.addComponent(endpointInput,1,0,1,0);
                lbl = new Label("Graph");
                lbl.setWidth("100px");
                settingsLayout.addComponent(lbl,0,1,0,1);
                TextField graphInput = new TextField();
                graphInput.setWidth("100%");
                settingsLayout.addComponent(graphInput,1,1,1,1);
                settingsWindow.center();
                getUI().addWindow(settingsWindow);
            }
        });
    }

    @Override
    public void detach() {
        super.detach(); //To change body of generated methods, choose Tools | Templates.
        System.out.println("Detach Validation called!");
    }

    @Override
    public void attach() {
        createUI();
        System.out.println("Attach called!");
    }

    @Override
    public void icQueryChanged(ICQuery ic) {
        // TODO implement properly, aka add appropriate IntegrityConstraintComponent
        contentLayout.removeAllComponents();
        contentLayout.addComponent(new Label("Clicked " + ic));
        IntegrityConstraintComponent icComponent = icHash.get(ic);
        contentLayout.addComponent(icComponent);
        criteriaTree.setItemIcon(ic, icComponent.getIcon());
    }
    
}
