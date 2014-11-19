/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC10 extends IntegrityConstraintComponent {

    public IC10(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-10 Slice dimensions complete";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?slice ?dim \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?slice qb:sliceStructure ?key . \n");
        strBuilder.append("  ?key qb:componentProperty ?dim . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?slice ?dim ?val . \n");
        strBuilder.append("  } \n");
        strBuilder.append("} order by ?slice");
        return new ICQuerySimple(repository, strBuilder.toString());
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

        final HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        String lastSlice = null;
        ArrayList<String> lastDimensions = new ArrayList<String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            String s = set.getValue("slice").stringValue();
            if (lastSlice == null) {
                lastSlice = s;
            }
            String d = set.getValue("dim").stringValue();
            if (!s.equals(lastSlice)) {
                map.put(lastSlice, lastDimensions);
                lastSlice = s;
                lastDimensions = new ArrayList<String>();
            }
            lastDimensions.add(d);
        }
        if (lastSlice != null) {
            map.put(lastSlice, lastDimensions);
        }
        if (map.isEmpty()) {
            Label label = new Label();
            label.setValue("No problems were detected - either there are no slices or every slice has a value for every dimension declared in its associated slice key (via property qb:sliceStructure)");
            rootLayout.addComponent(label);
            return;
        }

        Label label = new Label();
        label.setValue("Following slices do not have a value for every dimension declared in its associated slice key (via property qb:sliceStructure)");
        rootLayout.addComponent(label);
        final ListSelect lsSlices = new ListSelect("Slices", map.keySet());
        lsSlices.setImmediate(true);
        lsSlices.setNullSelectionAllowed(false);
        rootLayout.addComponent(lsSlices);

        final Table detailsTable = new Table("Slice details");
        detailsTable.setHeight("250px");
        detailsTable.setWidth("100%");
        detailsTable.addContainerProperty("Property", String.class, null);
        detailsTable.addContainerProperty("Object", String.class, null);
        rootLayout.addComponent(detailsTable);

        final Label lblProblem = new Label("<b>Problem description: </b>", ContentMode.HTML);
        rootLayout.addComponent(lblProblem);

        Button editInOW = new Button("Edit in OntoWiki");
        editInOW.setEnabled(owUrl != null);
        rootLayout.addComponent(editInOW);

        editInOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editManually((String)lsSlices.getValue());
            }
        });
        lsSlices.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String slice = (String) event.getProperty().getValue();
                TupleQueryResult res = getResourceProperties(slice);
                int i = 1;
                detailsTable.removeAllItems();
                try {
                    while (res.hasNext()) {
                        BindingSet set = res.next();
                        detailsTable.addItem(new Object[]{set.getValue("p").stringValue(),
                            set.getValue("o").stringValue()}, i++);
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("<b>Problem description: </b>Selected slice is missing a value for the following dimensions:");
                for (String dim : map.get(slice)) {
                    sb.append(" ").append(dim).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                lblProblem.setValue(sb.toString());
            }
        });
    }
    
}
