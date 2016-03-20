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
import rs.pupin.jpo.validation.ic.ICQueryTemplate;

/**
 *
 * @author vukm
 */
public class IC21 extends IntegrityConstraintComponent {

    public IC21(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-21 Codes from hierarchy (inverse)";
    }

    @Override
    public ICQuery generateICQuery() {
        // template
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix skos: <http://www.w3.org/2004/02/skos/core#> \n");
        strBuilder.append("prefix owl: <http://www.w3.org/2002/07/owl#> \n");
        strBuilder.append("select distinct ?p \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?hierarchy a qb:HierarchicalCodeList . \n");
        strBuilder.append("  ?hierarchy qb:parentChildProperty ?pcp . \n");
        strBuilder.append("  FILTER (isBlank(?pcp) )\n");
        strBuilder.append("  ?pcp  owl:inverseOf ?p . \n");
        strBuilder.append("  FILTER (isIRI(?p) )\n");
        strBuilder.append("}");
        String templateQuery = strBuilder.toString();
        // query
        strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix skos: <http://www.w3.org/2004/02/skos/core#> \n");
        strBuilder.append("select distinct ?dim <@p> as ?p \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  ?dsd qb:component ?cs . \n");
        strBuilder.append("  ?cs qb:componentProperty ?dim . \n");
        strBuilder.append("  ?dim a qb:DimensionProperty . \n");
        strBuilder.append("  ?dim qb:codeList ?list . \n");
        strBuilder.append("  ?list a qb:HierarchicalCodeList . \n");
        strBuilder.append("  ?obs ?dim ?v . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?list qb:hierarchyRoot ?root . \n");
        strBuilder.append("    ?root <@p>* ?v . \n");
        strBuilder.append("  } \n");
        strBuilder.append("}");
        String query = strBuilder.toString();
        return new ICQueryTemplate(repository, templateQuery, query);
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        
        final Iterator<BindingSet> res = icQuery.getResults();
        
        if (icQuery.getStatus() == ICQuery.Status.ERROR) {
            Label label = new Label();
            label.setValue("ERROR \n" + icQuery.getErrorMessage());
            rootLayout.addComponent(label);
            return;
        }

        final HashMap<String, String> map = new HashMap<String, String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            map.put(set.getValue("dim").stringValue(), set.getValue("p").stringValue());
        }

        if (map.isEmpty()) {
            Label label = new Label();
            label.setValue("All values of hierarchical dimensions with an inverse qb:parentChildProperty are reachable from a root of the hierarchy");
            rootLayout.addComponent(label);
            return;
        }

        Label label = new Label();
        label.setValue("Following dimensions need to be fixed becaue some of its values cannot be reached from root of the hierarchy along the inverse qb:parentChildProperty links");
        rootLayout.addComponent(label);

        final ListSelect listValues = new ListSelect("Resources", map.keySet());
        listValues.setNullSelectionAllowed(false);
        listValues.setImmediate(true);
        rootLayout.addComponent(listValues);

        Button editInOW = new Button("Edit in OntoWiki");
        editInOW.setEnabled(owUrl != null);
        editInOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editManually((String)listValues.getValue());
            }
        });
    }

}
