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
public class IC14 extends IntegrityConstraintComponent {

    public IC14(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-14 All measures present";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?obs ?measure \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?dsd qb:component ?cs0 . \n");
        strBuilder.append("    ?cs0 qb:componentProperty qb:measureType . \n");
        strBuilder.append("  } \n");
        strBuilder.append("  ?dsd qb:component ?cs . \n");
        strBuilder.append("  ?cs qb:componentProperty ?measure . \n");
        strBuilder.append("  ?measure a qb:MeasureProperty . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?obs ?measure [] } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        Iterator<BindingSet> res = icQuery.getResults();

        if (icQuery.getStatus() == ICQuery.Status.ERROR) {
            Label label = new Label();
            label.setValue("ERROR \n" + icQuery.getErrorMessage());
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
            label.setValue("No problems were detected - In Data Sets that do not use a Measure dimension (if there are any) each Observation has a value for every declared measure");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following observations are missing a value for declared measure(s)");
        rootLayout.addComponent(lbl);

        final ListSelect listObservations = new ListSelect("Observations", obsMap.keySet());
        listObservations.setNullSelectionAllowed(false);
        rootLayout.addComponent(listObservations);

		// TODO: add label that tells which measure is missing
        Button fix = new Button("Edit in OntoWiki");
        fix.setEnabled(owUrl != null);
        rootLayout.addComponent(fix);
        rootLayout.setExpandRatio(fix, 2.0f);

        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editManually((String)listObservations.getValue());
            }
        });
    }
    
}
