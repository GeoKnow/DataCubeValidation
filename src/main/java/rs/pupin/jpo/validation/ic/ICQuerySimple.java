/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.ic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author vukm
 */
public class ICQuerySimple extends ICQuery {
    
    private Repository repository;
    private Boolean status = null;
    private StatusFunction statusFunction;
    private String query;
    private TupleQueryResult res;
    private List<BindingSet> resList = new LinkedList<BindingSet>();
    
    public ICQuerySimple(Repository repository, String query){
        this.repository = repository;
        this.query = query;
        this.statusFunction = new StatusFunction() {
            @Override
            public Boolean getStatus(Iterator<BindingSet> queryResult) {
                if (queryResult == null) return null;
                return !queryResult.hasNext();
            }
        };
    }
    public ICQuerySimple(Repository repository, String query, StatusFunction statusFunction){
        this.repository = repository;
        this.query = query;
        this.statusFunction = statusFunction;
    }
    
    @Override
    public List<BindingSet> evaluate(){
        try {
            RepositoryConnection conn = repository.getConnection();
            res = conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
            resList.clear();
            while (res.hasNext()) resList.add(res.next());
            status = statusFunction.getStatus(resList.iterator());
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        try { if (res!=null) res.close(); } catch (QueryEvaluationException e) {}
        return resList;
    }
    @Override
    public Boolean getStatus(){
        return status;
    }
    @Override
    public Iterator<BindingSet> getResults() {
        return resList.iterator();
    }
}
