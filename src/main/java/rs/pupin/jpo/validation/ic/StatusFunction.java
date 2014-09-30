/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.ic;

import java.util.Iterator;
import org.openrdf.query.BindingSet;

/**
 *
 * @author vukm
 */
public interface StatusFunction {
    public Boolean getStatus(Iterator<BindingSet> queryResult);
}
