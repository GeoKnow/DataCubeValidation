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
public class DefaultStatusToIconMapper implements StatusToIconMapper {
    
    private final ThemeResource nullIcon = new ThemeResource("icons/comments_color.png");
    private final ThemeResource trueIcon = new ThemeResource("icons/thumbs_up_color.png");
    private final ThemeResource falseIcon = new ThemeResource("icons/thumbs_down_color.png");
    private final ThemeResource unknownIcon = new ThemeResource("icons/question_mark_color.png");
    private final ThemeResource errorIcon = new ThemeResource("icons/exclamation_mark_color.png");
    private static DefaultStatusToIconMapper instance = null;

    @Override
    public ThemeResource map(ICQuery.Status status) {
        if (status == null) return nullIcon;
        else if (status == ICQuery.Status.GOOD) return trueIcon;
        else if (status == ICQuery.Status.BAD) return falseIcon;
        else if (status == ICQuery.Status.ERROR) return errorIcon;
        else return unknownIcon;
    }
    
    private DefaultStatusToIconMapper(){
        
    }
    
    public static DefaultStatusToIconMapper getInstance(){
        if (instance == null) instance = new DefaultStatusToIconMapper();
        return instance;
    }
    
}
