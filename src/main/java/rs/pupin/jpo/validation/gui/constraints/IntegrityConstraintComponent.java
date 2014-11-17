/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import rs.pupin.jpo.validation.gui.DefaultStatusToIconMapper;
import rs.pupin.jpo.validation.gui.StatusToIconMapper;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQueryListener;

/**
 *
 * @author vukm
 */
public abstract class IntegrityConstraintComponent extends CustomComponent implements ICQueryListener {
    
    protected Repository repository;
    protected String graph;
    protected ICQuery icQuery;
    protected VerticalLayout rootLayout;
    protected StatusToIconMapper statusMapper;
    
    protected class DetailsListener implements Property.ValueChangeListener {

        private final Table tbl;

        public DetailsListener(Table tbl) {
            this.tbl = tbl;
        }

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            TupleQueryResult res = getResourceProperties((String) event.getProperty().getValue());
            int i = 1;
            tbl.removeAllItems();
            try {
                while (res.hasNext()) {
                    BindingSet set = res.next();
                    tbl.addItem(new Object[]{set.getValue("p").stringValue(),
                        set.getValue("o").stringValue()}, i++);
                }
            } catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
        }
    }
    
    public IntegrityConstraintComponent(Repository repository, String graph){
        this.rootLayout = new VerticalLayout();
        this.rootLayout.setSpacing(true);
        this.repository = repository;
        this.graph = graph;
        this.icQuery = generateICQuery();
        this.icQuery.addQueryListener(this);
        this.statusMapper = DefaultStatusToIconMapper.getInstance();
        setCompositionRoot(rootLayout);
    }
    
    public abstract ICQuery generateICQuery();
    public abstract void generateGUI();
    
    public void refreshGUI(){
        rootLayout.removeAllComponents();
        generateGUI();
    }

    public List<BindingSet> eval() {
        return icQuery.eval();
    }

    public ICQuery.Status getStatus() {
        return icQuery.getStatus();
    }

    public void setStatusMapper(StatusToIconMapper statusMapper) {
        this.statusMapper = statusMapper;
    }
    
    @Override
    public ThemeResource getIcon(){
        return statusMapper.map(getStatus());
    }

    @Override
    public void icQueryChanged(ICQuery ic) {
        refreshGUI();
    }

    public ICQuery getIcQuery() {
        return icQuery;
    }
    
    public String getName(){
        return "Unknown";
    }
    
    public Statement getStatementFromUris(String s, String p, String o){
        ValueFactory factory = repository.getValueFactory();
        URI sub = factory.createURI(s);
        URI pre = factory.createURI(p);
        URI obj = factory.createURI(o);
        return factory.createStatement(sub, pre, obj);
    }
    
    public TupleQueryResult getResourceProperties(String resource){
        try {
            RepositoryConnection con = repository.getConnection();
            StringBuilder q = new StringBuilder();
            q.append("select ?p ?o from <").append(graph).append("> where { <");
            q.append(resource).append("> ?p ?o . }");
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            return tupleQuery.evaluate();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<String> getDataSets(){
        StringBuilder q = new StringBuilder();
        q.append("select ?ds from <").append(graph);
        q.append("> where { ?ds a <http://purl.org/linked-data/cube#DataSet> . }");
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            TupleQueryResult result = tupleQuery.evaluate();
            ArrayList<String> list = new ArrayList<String>();
            while (result.hasNext())
                list.add(result.next().getValue("ds").stringValue());
            return list;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void uploadStatements(Iterable<? extends Statement> statements){
        try {
            RepositoryConnection con = repository.getConnection();
            URI graphURI = repository.getValueFactory().createURI(this.graph);
            con.add(statements, graphURI);
        } catch (RepositoryException e) {
            e.printStackTrace();
            // inform user that the query failed and how to do it manually
        }
    }
	
    public void removeStatements(Iterable<? extends Statement> statements){
        try {
            RepositoryConnection con = repository.getConnection();
            URI graphURI = repository.getValueFactory().createURI(this.graph);
            con.remove(statements, graphURI);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
    
    public String normalizeQuery(StringBuilder builder){
        String res = builder.toString();
        res = res.replaceAll("(.*) a qb:DimensionProperty .", "{ { ($1) a qb:DimensionProperty . } UNION { [] qb:dimension ($1) . } }");
        res = res.replaceAll("(.*) a qb:AttributeProperty .", "{ { ($1) a qb:AttributeProperty . } UNION { [] qb:attribute ($1) . } }");
        res = res.replaceAll("(.*) a qb:MeasureProperty .", "{ { ($1) a qb:MeasureProperty . } UNION { [] qb:measure ($1) . } }");
        return res;
    }
    
    protected List<String> getDataStructureDefinitions() {
        StringBuilder q = new StringBuilder();
        q.append("select ?dsd from <").append(graph);
        q.append("> where { ?dsd a <http://purl.org/linked-data/cube#DataStructureDefinition> . }");
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            TupleQueryResult result = tupleQuery.evaluate();
            ArrayList<String> list = new ArrayList<String>();
            while (result.hasNext()) {
                list.add(result.next().getValue("dsd").stringValue());
            }
            return list;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected List<String> getObservations() {
        StringBuilder q = new StringBuilder();
        q.append("select ?o from <").append(graph);
        q.append("> where { ?o a <http://purl.org/linked-data/cube#Observation> . }");
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            TupleQueryResult result = tupleQuery.evaluate();
            ArrayList<String> list = new ArrayList<String>();
            while (result.hasNext()) {
                list.add(result.next().getValue("o").stringValue());
            }
            return list;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected GraphQueryResult executeGraphQuery(String query) {
        try {
            RepositoryConnection con = repository.getConnection();
            GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, query);
            GraphQueryResult result = graphQuery.evaluate();
            return result;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected TupleQueryResult executeTupleQuery(String query) {
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult tupleResult = tupleQuery.evaluate();
            return tupleResult;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected TupleQueryResult getResourceLinks(String resource) {
        try {
            RepositoryConnection con = repository.getConnection();
            StringBuilder q = new StringBuilder();
            q.append("select ?s ?p from <").append(graph).append("> where { ?s ?p <");
            q.append(resource).append("> . }");
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            return tupleQuery.evaluate();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
