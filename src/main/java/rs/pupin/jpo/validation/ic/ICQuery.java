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
    public static enum Status { 
        NEW, GOOD, BAD, ERROR, UNKNOWN
    };
    
    protected List<ICQuery> list = new LinkedList<ICQuery>();
    protected List<ICQueryListener> listeners = new LinkedList<ICQueryListener>();
    
    protected abstract List<BindingSet> evaluate();
    public List<BindingSet> eval(){
        List<BindingSet> res = evaluate();
        for (ICQueryListener l: listeners) l.icQueryChanged(this);
        return res;
    }
    public abstract Status getStatus();
    public abstract Iterator<BindingSet> getResults();
    public abstract String getErrorMessage();
    public void add(ICQuery q) {}
    public void remove(ICQuery q) {}
    public void addQueryListener(ICQueryListener listener){
        listeners.add(listener);
    }
    public void removeQueryListener(ICQueryListener l){
        listeners.remove(l);
    }
    public void clearListeners(){
        listeners.clear();
    }
}
