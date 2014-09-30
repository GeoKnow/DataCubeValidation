/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
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
    
    public IntegrityConstraintComponent(Repository repository, String graph){
        this.rootLayout = new VerticalLayout();
        this.repository = repository;
        this.graph = graph;
        this.icQuery = generateICQuery();
        this.icQuery.addQueryListener(this);
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

    public Boolean getStatus() {
        return icQuery.getStatus();
    }

    @Override
    public void icQueryChanged(ICQuery ic) {
        refreshGUI();
    }

    public ICQuery getIcQuery() {
        return icQuery;
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
    
    public void uploadStatements(Iterable<? extends Statement> statements){
        try {
            RepositoryConnection con = repository.getConnection();
            URI graph = repository.getValueFactory().createURI(this.graph);
            con.add(statements, graph);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
	
    public void removeStatements(Iterable<? extends Statement> statements){
        try {
            RepositoryConnection con = repository.getConnection();
            URI graph = repository.getValueFactory().createURI(this.graph);
            con.remove(statements, graph);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
    
}
