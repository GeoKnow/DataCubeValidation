/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 *
 * @author vukm
 */
public class ValidationSettingsWindow extends Window {
    private CheckBox authCheckBox;
    
    public static class ValidationSettingsState {
        public Repository repository = null;
        public String endpoint;
        public String graph;
    }
    
    private ValidationSettingsState state;
    private final GridLayout settingsLayout;
    private TextField endpointInput;
    private TextField graphInput;
    private TextField usernameInput;
    private PasswordField passwordInput;
    private Button btnOK;
    private Button btnCancel;
    
    public ValidationSettingsWindow(ValidationSettingsState state){
        this.state = state;
        setModal(true);
        setClosable(false);
        setResizable(false);
        setDraggable(false);
        setWidth("700px");
        setHeight("400px");
        
        settingsLayout = new GridLayout(2,8);
        settingsLayout.setMargin(true);
        settingsLayout.setSizeFull();
        settingsLayout.setColumnExpandRatio(1, 2.0f);
        settingsLayout.setRowExpandRatio(4, 2.0f);
        settingsLayout.setSpacing(true);
        setContent(settingsLayout);
    }

    @Override
    public void detach() {
        super.detach(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void attach() {
        // add endpoint field
        Label lbl = new Label("Endpoint:");
        lbl.setSizeUndefined();
        settingsLayout.addComponent(lbl, 0, 0, 0, 0);
        settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        endpointInput = new TextField();
        endpointInput.setValue(state.endpoint);
        endpointInput.setWidth("100%");
        settingsLayout.addComponent(endpointInput, 1, 0, 1, 0);

        // add graph field
        lbl = new Label("Graph:");
        lbl.setSizeUndefined();
        settingsLayout.addComponent(lbl, 0, 1, 0, 1);
        settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        graphInput = new TextField();
        graphInput.setValue(state.graph);
        graphInput.setWidth("100%");
        settingsLayout.addComponent(graphInput, 1, 1, 1, 1);
        
        lbl = new Label("");
        lbl.setHeight("30px");
        settingsLayout.addComponent(lbl, 0, 2, 1, 2);
        
        // add basic authentication check box
        authCheckBox = new CheckBox("Use basic authentication");
        authCheckBox.setSizeUndefined();
        settingsLayout.addComponent(authCheckBox, 0, 3, 1, 3);
        settingsLayout.setComponentAlignment(authCheckBox, Alignment.MIDDLE_LEFT);

        // add username field
        lbl = new Label("Username:");
        lbl.setSizeUndefined();
        settingsLayout.addComponent(lbl, 0, 4);
        settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        usernameInput = new TextField();
        usernameInput.setWidth("100%");
        usernameInput.setEnabled(false);
        settingsLayout.addComponent(usernameInput, 1, 4);
        settingsLayout.setComponentAlignment(usernameInput, Alignment.MIDDLE_LEFT);

        // add password field
        lbl = new Label("Password:");
        lbl.setSizeUndefined();
        settingsLayout.addComponent(lbl, 0, 5);
        settingsLayout.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
        passwordInput = new PasswordField();
        passwordInput.setWidth("100%");
        passwordInput.setEnabled(false);
        settingsLayout.addComponent(passwordInput, 1, 5);
        
        lbl = new Label("");
        lbl.setHeight("20px");
        settingsLayout.addComponent(lbl, 0, 6, 1, 6);

        // add buttons
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setSizeUndefined();
        btnOK = new Button("OK");
        btnCancel = new Button("Cancel");
        hl.addComponent(btnOK);
        hl.addComponent(btnCancel);
        settingsLayout.addComponent(hl, 1, 7);
        settingsLayout.setComponentAlignment(hl, Alignment.MIDDLE_RIGHT);
        
        createListeners();
        center();
    }
    
    private void createListeners(){
        btnCancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        btnOK.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (state.endpoint.equals(endpointInput.getValue()) && state.graph.equals(graphInput.getValue())) {
                    close();
                    return;
                }
                
                SPARQLRepository r = new SPARQLRepository(endpointInput.getValue());
                if (authCheckBox.getValue()) r.setUsernameAndPassword(usernameInput.getValue(), passwordInput.getValue());

//                        Map<String, String> additionalHeaders = new HashMap<String, String>();
//                        String user = usernameInput.getValue();
//                        String pass = passwordInput.getValue();
//                        additionalHeaders.put("Authorization", "Basic " + encodeBase64(user + ":" + pass));
//                        r.setAdditionalHttpHeaders(additionalHeaders);
                try {
                    r.initialize();
                    System.out.println(r.isWritable());
                    StringBuilder qBuilder = new StringBuilder("ASK { GRAPH <");
                    qBuilder.append(graphInput.getValue());
                    qBuilder.append("> { ?s ?p ?o . } }");
                    boolean graphExists = r.getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, qBuilder.toString()).evaluate();
                    if (!graphExists) {
                        Notification.show("Selected graph doesn't exist", Notification.Type.ERROR_MESSAGE);
                        return;
                    }
                    state.repository = r;
                    state.endpoint = endpointInput.getValue();
                    state.graph = graphInput.getValue();
                    // TODO denote if input changed or not
                } catch (RepositoryException ex) {
                    Logger.getLogger(ValidationComponent.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Threw exception:\n" + ex.getMessage());
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(ValidationComponent.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(ValidationComponent.class.getName()).log(Level.SEVERE, null, ex);
                    // TODO notify user
                    Notification.show("Error connecting to the endpoint: " + ex.getMessage() + "\nCaused by: " + ex.getCause().getMessage());
                    return;
                }
                close();
            }

            private String encodeBase64(String string) {
                return new String(Base64.encode(string.getBytes()));
            }
        });
        
        authCheckBox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                usernameInput.setEnabled(authCheckBox.getValue());
                passwordInput.setEnabled(authCheckBox.getValue());
            }
        });
    }
    
}
