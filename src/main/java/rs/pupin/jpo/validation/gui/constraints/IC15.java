/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC15 extends IntegrityConstraintComponent {

    public IC15(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-15 Measure dimension consistent";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?obs ?measure \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  ?obs qb:measureType ?measure . \n");
        strBuilder.append("  ?dsd qb:component ?cs . \n");
        strBuilder.append("  ?component qb:componentProperty qb:measureType . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?obs ?measure [] } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        Iterator<BindingSet> res = icQuery.getResults();

        if (res == null) {
            Label label = new Label();
            label.setValue("ERROR");
            rootLayout.addComponent(label);
            return;
        }

        final HashMap<String, String> obsMap = new HashMap<String, String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            obsMap.put(set.getValue("obs").stringValue(), set.getValue("measure").stringValue());
        }

        if (obsMap.isEmpty()) {
            Label label = new Label();
            label.setValue("No problems were detected - In Data Sets that a Measure dimension (if there are any) each Observation has a value for the measure corresponding to its given qb:measureType");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following observations are missing a value for the measure corresponding to its given qb:measureType");
        rootLayout.addComponent(lbl);

        final ListSelect listObservations = new ListSelect("Observations", obsMap.keySet());
        listObservations.setNullSelectionAllowed(false);
        rootLayout.addComponent(listObservations);

		// TODO: add label that tells which measure is missing
        Button fix = new Button("Edit in OntoWiki");
        rootLayout.addComponent(fix);
        rootLayout.setExpandRatio(fix, 2.0f);

        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // create replacement
            }
        });
    }
    
}
