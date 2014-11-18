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
public class ICQueryComposite extends ICQuery {
    
    private List<BindingSet> resList = new LinkedList<BindingSet>();
    private boolean isNew = true;
    
    @Override
    public void add(ICQuery q) { list.add(q); }
    
    @Override
    public void remove(ICQuery q) { list.remove(q); }
    
    @Override
    public Status getStatus(){
        if (isNew) return Status.NEW;
        
        boolean hasBad = false;
        boolean hasError = false;
        boolean hasNew = false;
        boolean allGood = true;
        for (ICQuery q:list){
            if (q.getStatus() == null) return null;
            if (q.getStatus() == Status.ERROR) hasError = true;
            if (q.getStatus() == Status.BAD) hasBad = true;
            if (q.getStatus() != Status.GOOD) allGood = false;
            if (q.getStatus() == Status.NEW) hasNew = true;
        }
        if (allGood) return Status.GOOD;
        else if (hasError) return Status.ERROR;
        else if (hasBad) return Status.BAD;
        else if (hasNew) return Status.NEW;
        
        return Status.UNKNOWN;
    }
    
    @Override
    public List<BindingSet> evaluate(){
        isNew = false;
        resList.clear();
        for (ICQuery q: list) resList.addAll(q.evaluate());
        return resList;
    }

    @Override
    public Iterator<BindingSet> getResults() {
        return resList.iterator();
    }

    @Override
    public String getErrorMessage() {
        StringBuilder builder = new StringBuilder();
        for (ICQuery q: list) {
            String msg = q.getErrorMessage();
            if (msg != null) builder.append(msg).append("\n");
        }
        
        if (builder.length() == 0) return null;
        else return builder.toString();
    }
}
