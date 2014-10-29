/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import java.util.ArrayList;
import java.util.Iterator;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC11 extends IntegrityConstraintComponent {

    public IC11(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-11 All dimensions required";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix skos: <http://www.w3.org/2004/02/skos/core#> \n");
        strBuilder.append("select distinct ?obs \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  ?dsd qb:component ?cs . \n");
        strBuilder.append("  ?cs qb:componentProperty ?dim . \n");
        strBuilder.append("  ?dim a qb:DimensionProperty . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?obs ?dim [] } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        rootLayout.addComponent(new Label("Following observation don't have a value for each dimension: "));
        Iterator<BindingSet> res = icQuery.getResults();
        ArrayList<String> listObs = new ArrayList<String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            listObs.add(set.getValue("obs").stringValue());
        }
        ListSelect ls = new ListSelect("Observations", listObs);
        ls.setNullSelectionAllowed(false);
        ls.setWidth("100%");
        rootLayout.addComponent(ls);
        Button fix = new Button("Quick Fix");
        fix.setEnabled(false);
        rootLayout.addComponent(fix);
        rootLayout.setExpandRatio(fix, 2.0f);
    }
    
}
