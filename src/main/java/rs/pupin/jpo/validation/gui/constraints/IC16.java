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
public class IC16 extends IntegrityConstraintComponent {

    public IC16(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-16 Single measure";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?obs ?measure ?omeasure \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  ?obs qb:measureType ?measure . \n");
        strBuilder.append("  ?obs ?omeasure [] . \n");
        strBuilder.append("  ?dsd qb:component ?cs1 . \n");
        strBuilder.append("  ?cs1 qb:componentProperty qb:measureType . \n");
        strBuilder.append("  ?dsd qb:component ?cs2 . \n");
        strBuilder.append("  ?cs2 qb:componentProperty ?omeasure . \n");
        strBuilder.append("  ?omeasure a qb:MeasureProperty . \n");
        strBuilder.append("  FILTER (?omeasure != ?measure) \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        Iterator<BindingSet> res = icQuery.getResults();

        @SuppressWarnings("unused")
        final class MeasureOmeasurePair {

            String measure;
            String omeasure;
        }

        if (res == null) {
            Label label = new Label();
            label.setValue("ERROR");
            rootLayout.addComponent(label);
            return;
        }

        final HashMap<String, MeasureOmeasurePair> obsMap = new HashMap<String, MeasureOmeasurePair>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            MeasureOmeasurePair pair = new MeasureOmeasurePair();
            pair.measure = set.getValue("measure").stringValue();
            pair.omeasure = set.getValue("omeasure").stringValue();
            obsMap.put(set.getValue("obs").stringValue(), pair);
        }

        if (obsMap.size() == 0) {
            Label label = new Label();
            label.setValue("No problems were detected - In Data Sets that use a Measure dimension (if there are any) each Observation only has a value for one measure");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following observations belong to data sets that use a Measure dimension and have a value for more than one measure");
        rootLayout.addComponent(lbl);

        final ListSelect listObservations = new ListSelect("Observations", obsMap.keySet());
        listObservations.setNullSelectionAllowed(false);
        rootLayout.addComponent(listObservations);

		// TODO: add label that tells what is the measure dimension and mention the omeasure, perhaps details table
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
