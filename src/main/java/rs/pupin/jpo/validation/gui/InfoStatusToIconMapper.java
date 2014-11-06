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
public class InfoStatusToIconMapper implements StatusToIconMapper {

    private final ThemeResource infoIcon = new ThemeResource("icons/comments_color.png");
    private static InfoStatusToIconMapper instance = null;
    
    @Override
    public ThemeResource map(ICQuery.Status status) {
        return infoIcon;
    }
    
    private InfoStatusToIconMapper(){
        
    }
    
    public static InfoStatusToIconMapper getInstance(){
        if (instance == null) instance = new InfoStatusToIconMapper();
        return instance;
    }
    
}
