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
import org.openrdf.repository.Repository;

/**
 *
 * @author vukm
 */
public class IntegrityConstraint {
    private final ICQuery icQuery;
    private final List<BindingSet> resList = new LinkedList<BindingSet>();
    private Boolean status = null;

    public IntegrityConstraint(Repository repository, String query){
        this.icQuery = new ICQuerySimple(repository, query);
    }
    public IntegrityConstraint(Repository repository, String query, StatusFunction statusFunction){
        this.icQuery = new ICQuerySimple(repository, query, statusFunction);
    }
    public IntegrityConstraint(ICQuery icQuery){
        this.icQuery = icQuery;
    }
                
    public void evaluate(){
        resList.clear();
        resList.addAll(icQuery.evaluate());
        status = icQuery.getStatus();
    }
    public Iterator<BindingSet> getResults(){ 
        return resList.iterator();
    }
    public Boolean getStatus(){
        return status;
    }
}
