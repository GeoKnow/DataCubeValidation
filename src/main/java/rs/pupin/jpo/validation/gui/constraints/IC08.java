/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import java.util.ArrayList;
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
public class IC08 extends IntegrityConstraintComponent {

    public IC08(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-8 Slice Keys consistent with DSD";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?sliceKey \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?sliceKey a qb:SliceKey . \n");
        strBuilder.append("  ?sliceKey qb:componentProperty ?prop . \n");
        strBuilder.append("  ?dsd qb:sliceKey ?sliceKey . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?dsd qb:component ?cs . \n");
        strBuilder.append("    ?cs qb:componentProperty ?prop . \n");
        strBuilder.append("  } \n");
        strBuilder.append("}");
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

        final ArrayList<String> listSliceKeys = new ArrayList<String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            listSliceKeys.add(set.getValue("sliceKey").stringValue());
        }
        if (listSliceKeys.isEmpty()) {
            Label label = new Label();
            label.setValue("No problems were detected - either there are no slice keys or all slice keys are consistent with associated DSD, i.e. for every slice key holds: "
                    + "every component property of the slice key is also declared as a component of the associated DSD");
            rootLayout.addComponent(label);
            return;
        }

        Label label = new Label();
        label.setValue("All slice keys should be consistent with thier associated DSDs, i.e. for every slice key following should hold: "
                + "every component property of the slice key is also declared as a component of the associated DSD.");
        rootLayout.addComponent(label);
        Label label2 = new Label();
        label2.setValue("Following slice keys should be modified in order to be consistent with the associated DSD");
        rootLayout.addComponent(label2);
        final ListSelect lsSliceKeys = new ListSelect("Slice keys", listSliceKeys);
        lsSliceKeys.setImmediate(true);
        lsSliceKeys.setNullSelectionAllowed(false);
        rootLayout.addComponent(lsSliceKeys);

        final Table detailsTable = new Table("Slice key details");
        detailsTable.setHeight("250px");
        detailsTable.setWidth("100%");
        detailsTable.addContainerProperty("Property", String.class, null);
        detailsTable.addContainerProperty("Object", String.class, null);
        rootLayout.addComponent(detailsTable);

        Button editInOW = new Button("Edit in OntoWiki");
        rootLayout.addComponent(editInOW);

        editInOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // TODO create replacement
            }
        });
        lsSliceKeys.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                TupleQueryResult res = getResourceProperties((String) event.getProperty().getValue());
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
            }
        });
    }
    
}
