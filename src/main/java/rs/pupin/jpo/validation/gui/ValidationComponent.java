/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import rs.pupin.jpo.validation.gui.constraints.*;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQueryListener;

/**
 *
 * @author vukm
 */
public class ValidationComponent extends CustomComponent implements ICQueryListener {
    
    private Repository repository;
    private String graph;
    private String endpoint;
    private String owUrl;
    private HorizontalLayout headerLayout;
    private Button btnSettings;
    private Button btnClearAll;
    private Button btnEvalAll;
    private final VerticalLayout rootLayout;
    private Tree criteriaTree;
    private HorizontalSplitPanel splitPanel;
    private VerticalLayout contentLayout;
    private final HashMap<ICQuery, IntegrityConstraintComponent> icHash;
    private Button btnRDFUnit;
    private Button btnRefresh;
    
    public ValidationComponent(Repository repository, String endpoint, String graph, String owUrl){
        this.repository = repository;
        this.endpoint = endpoint;
        this.graph = graph;
        this.owUrl = owUrl;
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
        
        criteriaTree = new Tree("Validation criteria");
        criteriaTree.setNullSelectionAllowed(false);
        criteriaTree.setImmediate(true);
        criteriaTree.setWidth("100%");
        splitPanel.setFirstComponent(criteriaTree);
        createConstraints();
        splitPanel.setSplitPosition(400, Unit.PIXELS);
        contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        contentLayout.addStyleName("content");
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
        btnRDFUnit = new Button("RDFUnit Validation");
        btnRefresh = new Button("Refresh");
        headerLayout.addComponent(btnRefresh);
        headerLayout.addComponent(btnRDFUnit);
//        headerLayout.addComponent(btnClearAll);
        headerLayout.addComponent(btnEvalAll);
        headerLayout.addComponent(btnSettings);
    }
    
    private void createConstraints(){
        addIC(new Summary(repository, graph, owUrl));
        addIC(new Provenance(repository, graph, owUrl));
        addIC(new IC01(repository, graph, owUrl));
        addIC(new IC02(repository, graph, owUrl));
        addIC(new IC03(repository, graph, owUrl));
        addIC(new IC04(repository, graph, owUrl));
        addIC(new IC05(repository, graph, owUrl));
        addIC(new IC06(repository, graph, owUrl));
        addIC(new IC07(repository, graph, owUrl));
        addIC(new IC08(repository, graph, owUrl));
        addIC(new IC09(repository, graph, owUrl));
        addIC(new IC10(repository, graph, owUrl));
        addIC(new IC11(repository, graph, owUrl));
        addIC(new IC12(repository, graph, owUrl));
        addIC(new IC13(repository, graph, owUrl));
        addIC(new IC14(repository, graph, owUrl));
        addIC(new IC15(repository, graph, owUrl));
        addIC(new IC16(repository, graph, owUrl));
        addIC(new IC17(repository, graph, owUrl));
        addIC(new IC18(repository, graph, owUrl));
        addIC(new IC19(repository, graph, owUrl));
        addIC(new IC20(repository, graph, owUrl));
        addIC(new IC21(repository, graph, owUrl));
        
        criteriaTree.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ICQuery ic = (ICQuery)event.getProperty().getValue();
                if (ic.getStatus() == ICQuery.Status.NEW) ic.eval();
                else icQueryChanged(ic);
            }
        });
    }
    
    private void refresh(){
        splitPanel.removeComponent(criteriaTree);
        contentLayout.removeAllComponents();
        for (ICQuery ic: icHash.keySet()) ic.removeQueryListener(this);
        icHash.clear();
        criteriaTree = new Tree("Validation criteria");
        criteriaTree.setNullSelectionAllowed(false);
        criteriaTree.setImmediate(true);
        criteriaTree.setWidth("100%");
        splitPanel.setFirstComponent(criteriaTree);
        createConstraints();
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
                final ValidationSettingsWindow.ValidationSettingsState state = new ValidationSettingsWindow.ValidationSettingsState();
                state.endpoint = endpoint;
                state.graph = graph;
                state.owUrl = owUrl;
                Window w = new ValidationSettingsWindow(state);
                w.addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent e) {
                        try {
                            if (!endpoint.equals(state.endpoint) || !graph.equals(state.graph))
                                repository.shutDown();
                        } catch (RepositoryException ex) {
                            Logger.getLogger(ValidationComponent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (state.repository != null && state.repository.isInitialized()) {
                            repository = state.repository;
                            endpoint = state.endpoint;
                            graph = state.graph;
                            owUrl = state.owUrl;
                            refresh();
                        }
                    }
                });
                getUI().addWindow(w);
            }
        });
        btnRDFUnit.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Window w = new RDFUnitWindow(repository, endpoint, graph);
                getUI().addWindow(w);
            }
        });
        btnRefresh.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Object selection = criteriaTree.getValue();
                if (selection == null) return;
                ((ICQuery)selection).eval();
            }
        });
        btnEvalAll.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (ICQuery ic: icHash.keySet()) ic.eval();
            }
        });
    }

    @Override
    public void detach() {
        super.detach(); 
        System.out.println("Detach Validation called!");
    }

    @Override
    public void attach() {
        super.attach();
        createUI();
        System.out.println("Attach called!");
    }

    @Override
    public void icQueryChanged(ICQuery ic) {
        // if it's not the currently selected IC only update the icon
        if (!ic.equals(criteriaTree.getValue())) {
            criteriaTree.setItemIcon(ic, icHash.get(ic).getIcon());
            return;
        }
        
        // if it is the currently selected IC put proper Component in content pane
        contentLayout.removeAllComponents();
        IntegrityConstraintComponent icComponent = icHash.get(ic);
        contentLayout.addComponent(icComponent);
        criteriaTree.setItemIcon(ic, icComponent.getIcon());
    }
    
}
