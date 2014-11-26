/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aksw.rdfunit.RDFUnit;
import org.aksw.rdfunit.RDFUnitConfiguration;
import org.aksw.rdfunit.Utils.RDFUnitUtils;
import org.aksw.rdfunit.io.reader.RDFReaderException;
import org.aksw.rdfunit.io.writer.RDFHTMLResultsWriter;
import org.aksw.rdfunit.io.writer.RDFWriterFactory;
import org.aksw.rdfunit.sources.Source;
import org.aksw.rdfunit.tests.TestSuite;
import org.aksw.rdfunit.tests.executors.TestExecutor;
import org.aksw.rdfunit.tests.executors.TestExecutorFactory;
import org.aksw.rdfunit.tests.executors.monitors.SimpleTestExecutorMonitor;
import org.aksw.rdfunit.tests.generators.TestGeneratorExecutor;
import org.aksw.rdfunit.validate.ParameterException;
import org.aksw.rdfunit.validate.utils.ValidateUtils;
import org.apache.commons.cli.CommandLine;
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
    
    private final Repository repository;
    private final String endpoint;
    private final String graph;
    private final VerticalLayout layout;
    private Button btnValidate;
    private Label statusLabel;
    private Label resLabel;
    private final Panel scrollPane;
    
    public RDFUnitWindow(Repository repository, String endpoint, String graph){
        this.repository = repository;
        this.endpoint = endpoint;
        this.graph = graph;
        
        setModal(true);
        setResizable(false);
        setDraggable(false);
        setWidth("90%");
        setHeight("90%");
        
        scrollPane = new Panel();
        scrollPane.setSizeFull();
        
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeUndefined();
        layout.setWidth("100%");
        
        scrollPane.setContent(layout);
        setContent(scrollPane);
    }

    @Override
    public void attach() {
        super.attach(); 
        Label lbl = new Label("RDFUnit validation");
        layout.addComponent(lbl);
        layout.setExpandRatio(lbl, 0.0f);
        
        btnValidate = new Button("Validate");
        btnValidate.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                statusLabel.setValue("Creating a file...");
//                createFile();
                statusLabel.setValue("Executing...");
                validate();
                statusLabel.setValue("Finished");
            }
        });
        layout.addComponent(btnValidate);
        layout.setExpandRatio(btnValidate, 0.0f);
        
        statusLabel = new Label("Click execute to validate the graph with RDFUnit");
        layout.addComponent(statusLabel);
        layout.setExpandRatio(statusLabel, 0.0f);
        
        resLabel = new Label("Results...", ContentMode.HTML);
        resLabel.setSizeUndefined();
        resLabel.setWidth("100%");
        layout.addComponent(resLabel);
        layout.setExpandRatio(resLabel, 2.0f);
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
        String [] args = {"-d", graph, "-e", endpoint, "-g", graph};
        try { 
            CommandLine commandLine = ValidateUtils.parseArguments(args);
            
//            String dataFolder = commandLine.getOptionValue("f", "../data/");
            RDFUnitUtils.fillSchemaServiceFromLOV(); // this can probably be moved to app initialization
//            RDFUnitUtils.fillSchemaServiceFromFile(dataFolder + "schemaDecl.csv");
            
            RDFUnitConfiguration configuration = null;
            try {
                configuration = ValidateUtils.getConfigurationFromArguments(commandLine);
            } catch (ParameterException e) {
                String message = e.getMessage();
                if (message != null) {
                    Notification.show(message, Notification.Type.ERROR_MESSAGE);
                    return;
                } else {
                    Notification.show("Parameter exception occured!", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            assert (configuration != null);
            
//            if (!RDFUnitUtils.fileExists(configuration.getDataFolder())) {
//                Notification.show("Path : " + configuration.getDataFolder() + " does not exists, use -f argument", Notification.Type.ERROR_MESSAGE);
//                return;
//            } // this can probably be removed

            RDFUnit rdfunit = new RDFUnit(/*configuration.getDataFolder()*/);
            try {
                rdfunit.init();
            } catch (RDFReaderException e) {
                Notification.show("Cannot read patterns and/or pattern generators", Notification.Type.ERROR_MESSAGE);
                return;
            }
            
            final Source dataset = configuration.getTestSource();
            /* </cliStuff> */

            TestGeneratorExecutor testGeneratorExecutor = new TestGeneratorExecutor(
                    configuration.isAutoTestsEnabled(),
                    configuration.isTestCacheEnabled(),
                    configuration.isManualTestsEnabled());
            TestSuite testSuite = testGeneratorExecutor.generateTestSuite(configuration.getTestFolder(), dataset, rdfunit.getAutoGenerators());

            TestExecutor testExecutor = TestExecutorFactory.createTestExecutor(configuration.getTestCaseExecutionType());
            if (testExecutor == null) {
                Notification.show("Cannot initialize test executor. Exiting", Notification.Type.ERROR_MESSAGE);
                return;
            }
            SimpleTestExecutorMonitor testExecutorMonitor = new SimpleTestExecutorMonitor();
            testExecutor.addTestExecutorMonitor(testExecutorMonitor);

            // warning, caches intermediate results
            testExecutor.execute(dataset, testSuite, 0);
            
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            RDFHTMLResultsWriter resWriter = RDFWriterFactory.createHTMLWriter(configuration.getTestCaseExecutionType(), outStream);
            
            resWriter.write(testExecutorMonitor.getModel());
            resLabel.setValue(outStream.toString());
        } catch (Exception e) {
            Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
}
