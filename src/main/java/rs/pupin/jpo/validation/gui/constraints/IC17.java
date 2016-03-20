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
public class IC17 extends IntegrityConstraintComponent {

    public IC17(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-17 All measures present in meas. dim. cube";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?obs1 ?numMeasures (COUNT(?obs2) AS ?count) \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  { \n");
        strBuilder.append("    SELECT ?dsd (COUNT(?m) AS ?numMeasures) WHERE { \n");
        strBuilder.append("      ?dsd qb:component ?cs0 . \n");
        strBuilder.append("      ?cs0 qb:componentProperty ?m . \n");
        strBuilder.append("      ?m a qb:MeasureProperty . \n");
        strBuilder.append("    } GROUP BY ?dsd \n");
        strBuilder.append("  } \n");
        strBuilder.append("  ?obs1 qb:dataSet ?dataset . \n");
        strBuilder.append("  ?dataset qb:structure ?dsd . \n");
        strBuilder.append("  ?obs1 qb:measureType ?m1 . \n");
        strBuilder.append("  ?obs2 qb:dataSet ?dataset . \n");
        strBuilder.append("  ?obs2 qb:measureType ?m2 . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?dsd qb:component ?cs1 . \n");
        strBuilder.append("    ?cs1 qb:componentProperty ?dim . \n");
        strBuilder.append("    FILTER (?dim != qb:measureType) \n");
        strBuilder.append("    ?dim a qb:DimensionProperty . \n");
        strBuilder.append("    ?obs1 ?dim ?v1 . \n");
        strBuilder.append("    ?obs2 ?dim ?v2 . \n");
        strBuilder.append("    FILTER (?v1 != ?v2) \n");
        strBuilder.append("  } \n");
        strBuilder.append("} GROUP BY ?obs1 ?numMeasures \n  HAVING (COUNT(?obs2) != ?numMeasures)");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        Iterator<BindingSet> res = icQuery.getResults();

        @SuppressWarnings("unused")
        final class NumMeasuresCountPair {

            String numMeasures;
            String count;
        }

        if (icQuery.getStatus() == ICQuery.Status.ERROR) {
            Label label = new Label();
            label.setValue("ERROR \n" + icQuery.getErrorMessage());
            rootLayout.addComponent(label);
            return;
        }

        final HashMap<String, NumMeasuresCountPair> obsMap = new HashMap<String, NumMeasuresCountPair>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            NumMeasuresCountPair pair = new NumMeasuresCountPair();
            pair.numMeasures = set.getValue("numMeasures").stringValue();
            pair.count = set.getValue("count").stringValue();
            obsMap.put(set.getValue("obs1").stringValue(), pair);
        }

        if (obsMap.isEmpty()) {
            Label label = new Label();
            label.setValue("No problems were detected - In a data set which uses a measure dimension then "
                    + "if there is an Observation for some combination of non-measure dimensions then "
                    + "there must be other Observations with the same non-measure dimension values for each of the declared measures");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following observations belong to data sets that use a Measure dimension and break a rule that "
                + "if there is an Observation for some combination of non-measure dimensions then "
                + "there must be other Observations with the same non-measure dimension values for each of the declared measures");
        rootLayout.addComponent(lbl);

        final ListSelect listObservations = new ListSelect("Observations", obsMap.keySet());
        listObservations.setNullSelectionAllowed(false);
        listObservations.setImmediate(true);
        rootLayout.addComponent(listObservations);

		// TODO: add label that tells what is the difference in counts, maybe even more, perhaps details table
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
