/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.ic;

import java.util.List;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;

/**
 *
 * @author vukm
 */
public class ICQueryTemplate extends ICQueryComposite {
    private final ICQuery icTemplate;
    private final String query;
    private final Repository repository;
    
    public ICQueryTemplate(Repository repository, String template, String query){
        this.repository = repository;
        icTemplate = new ICQuerySimple(repository, template);
        this.query = query;
    }
    
    public void init(){
        List<BindingSet> res = icTemplate.evaluate();
        for (BindingSet set: res){
            String q = query.replace("@p", set.getValue("p").toString());
            ICQuerySimple ic = new ICQuerySimple(repository, q);
            add(ic);
        }
    }
}
