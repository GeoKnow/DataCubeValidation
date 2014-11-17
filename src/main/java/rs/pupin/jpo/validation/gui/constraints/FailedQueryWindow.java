/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author vukm
 */
public class FailedQueryWindow extends Window {
    
    private final String query1;
    private final String query2;
    private final VerticalLayout layout;
    private static final String messageSingleQuery = "Following query could not be succesfully executed. "
            + "This usually means that the repository you are using is not writeable. "
            + "You can use the query below and try to execute it some other way. "
            + "After this click refresh to update the integrity constraint results.";
    private static final String messageDoubleQuery = "Following queries could not be succesfully executed. "
            + "This usually means that the repository you are using is not writeable. "
            + "You can use the queries below and try to execute them some other way. "
            + "After this click refresh to update the integrity constraint results.";
    
    public FailedQueryWindow(String query){
        this(query, null);
    }
    
    public FailedQueryWindow(String query1, String query2){
        this.query1 = query1;
        this.query2 = query2;
        
        setModal(true);
        setResizable(false);
        setDraggable(false);
        setWidth("75%");
        setHeight("75%");
        
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();
        setContent(layout);
    }

    @Override
    public void detach() {
        super.detach();
    }

    @Override
    public void attach() {
        super.attach(); 
        String message = (query2 == null)?messageSingleQuery:messageDoubleQuery;
        Label lbl = new Label(message);
        lbl.setWidth("95%");
        layout.addComponent(lbl);
        layout.setExpandRatio(lbl, 0.0f);
        
        String textAreaCaption = (query2 != null)?"First Query":"Query";
        TextArea textArea = new TextArea(textAreaCaption);
        textArea.setValue(query1);
        textArea.setReadOnly(true);
        textArea.setSizeFull();
        layout.addComponent(textArea);
        layout.setExpandRatio(textArea, 2.0f);
        
        if (query2 != null){
            TextArea textArea2 = new TextArea("Second Query");
            textArea2.setValue(query2);
            textArea2.setReadOnly(true);
            textArea2.setSizeFull();
            layout.addComponent(textArea2);
            layout.setExpandRatio(textArea2, 2.0f);
        }
        
        center();
    }
    
}
