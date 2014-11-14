/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 *
 * @author vukm
 */
public class RDFUnitWindow extends Window {
    
    private Repository repository;
    private String graph;
    private final VerticalLayout layout;
    private Button btnValidate;
    private Label statusLabel;
    
    public RDFUnitWindow(Repository repository, String graph){
        this.repository = repository;
        this.graph = graph;
        
        setModal(true);
//        setClosable(false);
        setResizable(false);
        setDraggable(false);
        setSizeFull();
        
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        layout.setSpacing(true);
        setContent(layout);
    }

    @Override
    public void attach() {
        super.attach(); 
        Label lbl = new Label("RDFUnit validation");
        layout.addComponent(lbl);
        
        btnValidate = new Button("Validate");
        btnValidate.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                statusLabel.setValue("Creating a file...");
                createFile();
                statusLabel.setValue("Executing...");
//                validate();
                statusLabel.setValue("Finished");
            }
        });
        layout.addComponent(btnValidate);
        
        statusLabel = new Label("Click execute to validate the graph with RDFUnit");
        layout.addComponent(statusLabel);
    }
    
    private void createFile(){
        final String query = "CONSTRUCT { ?s ?p ?o } where { graph<" + graph + "> { ?s ?p ?o } }";
        try {
            // workaround for Virtuoso bug (returns result with wrong MIME type)
            RDFParserRegistry r = RDFParserRegistry.getInstance();
            RDFParserFactory something = r.get(RDFFormat.NTRIPLES);
            r.remove(something);
            
            // execute query and write to file
            RepositoryConnection conn = repository.getConnection();
            GraphQueryResult res = conn.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate();
            int hash = UI.getCurrent().hashCode();
            FileOutputStream out = new FileOutputStream("/tmp/data-cube-validation-" + hash + ".ttl");
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
            writer.startRDF();
            while (res.hasNext()){
                writer.handleStatement(res.next());
            }
            writer.endRDF();
            
            // undo changes of the workaround for the Virtuoso bug
            r.add(something);
        } catch (RepositoryException ex) {
            Logger.getLogger(RDFUnitWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(RDFUnitWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(RDFUnitWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RDFUnitWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RDFHandlerException ex) {
            Logger.getLogger(RDFUnitWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void validate(){
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
}
