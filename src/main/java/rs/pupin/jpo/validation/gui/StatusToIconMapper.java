/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui;

import com.vaadin.server.ThemeResource;
import rs.pupin.jpo.validation.ic.ICQuery;

/**
 *
 * @author vukm
 */
public interface StatusToIconMapper {
    
    public ThemeResource map(ICQuery.Status status);
    
}
