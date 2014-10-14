/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
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
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
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
    
    private Repository repository;
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
        
        criteriaTree = new Tree("Validation criteria");
        criteriaTree.setNullSelectionAllowed(false);
        criteriaTree.setImmediate(true);
        criteriaTree.setWidth("100%");
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
    
    private void refresh(){
        criteriaTree.removeAllItems();
        contentLayout.removeAllComponents();
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
                final Window settingsWindow = new Window();
                settingsWindow.setModal(true);
                settingsWindow.setWidth("700px");
                settingsWindow.setHeight("400px");
                GridLayout settingsLayout = new GridLayout(2,6);
                settingsLayout.setMargin(true);
                settingsLayout.setSizeFull();
                settingsLayout.setColumnExpandRatio(1, 2.0f);
                settingsLayout.setRowExpandRatio(4, 2.0f);
                settingsLayout.setSpacing(true);
                settingsWindow.setContent(settingsLayout);
                
                // add endpoint field
                Label lbl = new Label("Endpoint:");
                lbl.setSizeUndefined();
                settingsLayout.addComponent(lbl,0,0,0,0);
                settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
                final TextField endpointInput = new TextField();
                endpointInput.setWidth("100%");
                settingsLayout.addComponent(endpointInput,1,0,1,0);
                
                // add graph field
                lbl = new Label("Graph:");
                lbl.setSizeUndefined();
                settingsLayout.addComponent(lbl,0,1,0,1);
                settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
                final TextField graphInput = new TextField();
                graphInput.setWidth("100%");
                settingsLayout.addComponent(graphInput,1,1,1,1);
                
                // add username field
                lbl = new Label("Username:");
                lbl.setSizeUndefined();
                settingsLayout.addComponent(lbl,0,2);
                settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
                final TextField usernameInput = new TextField();
                usernameInput.setWidth("100%");
                settingsLayout.addComponent(usernameInput,1,2);
                
                // add password field
                lbl = new Label("Password:");
                lbl.setSizeUndefined();
                settingsLayout.addComponent(lbl,0,3);
                settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
                final PasswordField passwordInput = new PasswordField();
                passwordInput.setWidth("100%");
                settingsLayout.addComponent(passwordInput,1,3);
                
                // add buttons
                HorizontalLayout hl = new HorizontalLayout();
                hl.setSpacing(true);
                hl.setSizeUndefined();
                Button btnOK = new Button("OK");
                Button btnCancel = new Button("Cancel");
                hl.addComponent(btnOK);
                hl.addComponent(btnCancel);
                settingsLayout.addComponent(hl, 1, 5);
                settingsLayout.setComponentAlignment(hl, Alignment.MIDDLE_RIGHT);
                
                btnCancel.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        settingsWindow.close();
                    }
                });
                btnOK.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        System.out.println("====================");
                        System.out.println(endpointInput.getValue());
                        System.out.println(usernameInput.getValue());
                        System.out.println(passwordInput.getValue());
                        SPARQLRepository r = new SPARQLRepository(endpointInput.getValue());
                        r.setUsernameAndPassword(usernameInput.getValue(), passwordInput.getValue());
                        
//                        Map<String, String> additionalHeaders = new HashMap<String, String>();
//                        String user = usernameInput.getValue();
//                        String pass = passwordInput.getValue();
//                        additionalHeaders.put("Authorization", "Basic " + encodeBase64(user + ":" + pass));
//                        r.setAdditionalHttpHeaders(additionalHeaders);
                        
                        try {
                            r.initialize();
                            System.out.println(r.isWritable());
                            TupleQueryResult res = r.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, "select distinct ?a where { ?s a ?a }").evaluate();
                            while (res.hasNext()){
                                BindingSet set = res.next();
                                System.out.println("   " + set.getValue("a").toString());
                            }
                            repository = r;
                            graph = graphInput.getValue();
                            // TODO refresh
                        } catch (RepositoryException ex) {
                            Logger.getLogger(ValidationComponent.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Threw exception:\n" + ex.getMessage());
                        } catch (MalformedQueryException ex) {
                            Logger.getLogger(ValidationComponent.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (QueryEvaluationException ex) {
                            Logger.getLogger(ValidationComponent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        settingsWindow.close();
                    }

                    private String encodeBase64(String string) {
                        return new String(Base64.encode(string.getBytes()));
                    }
                });
                
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
