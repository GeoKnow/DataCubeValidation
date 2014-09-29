/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author vukm
 */
public class NonBlockingQueryEvaluator implements QueryEvaluator {
    
    private final Repository repository;
    private final ExecutorService executor;
    
    public NonBlockingQueryEvaluator(Repository repository){
        this.repository = repository;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void evaluate(final String query, final Runnable postRun) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RepositoryConnection conn = repository.getConnection();
                    TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                    TupleQueryResult res = q.evaluate();
                } catch (RepositoryException ex) {
                    Logger.getLogger(NonBlockingQueryEvaluator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(NonBlockingQueryEvaluator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(NonBlockingQueryEvaluator.class.getName()).log(Level.SEVERE, null, ex);
                }
                postRun.run();
            }
        });
        
    }

    @Override
    public void evaluate(final String query) {
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult res = q.evaluate();
        } catch (RepositoryException ex) {
            Logger.getLogger(NonBlockingQueryEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(NonBlockingQueryEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(NonBlockingQueryEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void cancel() {
        executor.shutdownNow();
    }

    @Override
    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
    
}
