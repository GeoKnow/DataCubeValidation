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
public class IC18 extends IntegrityConstraintComponent {

    public IC18(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-18 Consistent data set links";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?obs ?dataset ?slice \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?dataset qb:slice ?slice . \n");
        strBuilder.append("  ?slice   qb:observation ?obs . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?obs qb:dataSet ?dataset . } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        Iterator<BindingSet> res = icQuery.getResults();

        @SuppressWarnings("unused")
        final class DataSetSlicePair {

            String dataset;
            String slice;
        }

        if (res == null) {
            Label label = new Label();
            label.setValue("ERROR");
            rootLayout.addComponent(label);
            return;
        }

        final HashMap<String, DataSetSlicePair> obsMap = new HashMap<String, DataSetSlicePair>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            DataSetSlicePair pair = new DataSetSlicePair();
            pair.dataset = set.getValue("dataset").stringValue();
            pair.slice = set.getValue("slice").stringValue();
            obsMap.put(set.getValue("obs").stringValue(), pair);
        }

        if (obsMap.size() == 0) {
            Label label = new Label();
            label.setValue("No problems were detected - If a qb:DataSet D has a qb:slice S, and S has an qb:observation O, then the qb:dataSet corresponding to O must be D");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following observations are missing a link to the appropriate data set");
        rootLayout.addComponent(lbl);

        final ListSelect listObservations = new ListSelect("Observations", obsMap.keySet());
        listObservations.setNullSelectionAllowed(false);
        rootLayout.addComponent(listObservations);

		// TODO: add label that tells which dataset and slice are in question, perhaps details table
        Button fix = new Button("Edit in OntoWiki");
        rootLayout.addComponent(fix);
        rootLayout.setExpandRatio(fix, 2.0f);

        fix.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                // TODO create a replacement
            }
        });
    }
    
}
