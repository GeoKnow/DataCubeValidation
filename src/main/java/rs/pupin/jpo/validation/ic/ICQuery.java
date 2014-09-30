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

/**
 *
 * @author vukm
 */
public abstract class ICQuery {
    protected List<ICQuery> list = new LinkedList<ICQuery>();
    public abstract List<BindingSet> evaluate();
    public abstract Boolean getStatus();
    public abstract Iterator<BindingSet> getResults();
    public void add(ICQuery q) {}
    public void remove(ICQuery q) {}
}
