/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Iterator;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.ValidationFixUtils;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC09 extends IntegrityConstraintComponent {

    public IC09(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-9 Unique slice structure";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?slice \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  { \n");
        strBuilder.append("    ?slice a qb:Slice . \n");
        strBuilder.append("    FILTER NOT EXISTS { ?slice qb:sliceStructure ?key } \n");
        strBuilder.append("  } UNION { \n");
        strBuilder.append("    ?slice a qb:Slice . \n");
        strBuilder.append("    ?slice qb:sliceStructure ?key1 . \n");
        strBuilder.append("    ?slice qb:sliceStructure ?key2 . \n");
        strBuilder.append("    FILTER (?key1 != ?key2) \n");
        strBuilder.append("  } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();

        final Iterator<BindingSet> res = icQuery.getResults();
        if (res == null) {
            Label label = new Label();
            label.setValue("ERROR");
            rootLayout.addComponent(label);
            return;
        }

        final ArrayList<String> listSlices = new ArrayList<String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            listSlices.add(set.getValue("slice").stringValue());
        }
        if (listSlices.isEmpty()) {
            Label label = new Label();
            label.setValue("No problems were detected - either there are no slices or every slice has a unique structure, i.e. exactly one associated slice key (via property qb:sliceStructure)");
            rootLayout.addComponent(label);
            return;
        }

        Label label = new Label();
        label.setValue("Following slices have 0 or more than 1 associated slice keys (via property qb:sliceStructure)");
        rootLayout.addComponent(label);
        final ListSelect lsSlices = new ListSelect("Slices", listSlices);
        lsSlices.setImmediate(true);
        lsSlices.setNullSelectionAllowed(false);
        rootLayout.addComponent(lsSlices);

        final Table detailsTable = new Table("Slice details");
        detailsTable.setHeight("200px");
        detailsTable.setWidth("100%");
        detailsTable.addContainerProperty("Property", String.class, null);
        detailsTable.addContainerProperty("Object", String.class, null);
        rootLayout.addComponent(detailsTable);

        Form panelQuickFix = new Form();
        panelQuickFix.setCaption("Quick Fix");
        panelQuickFix.setSizeFull();
        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setSpacing(true);
        panelLayout.setSizeFull();
        panelQuickFix.setLayout(panelLayout);
        rootLayout.addComponent(panelQuickFix);
        rootLayout.setExpandRatio(panelQuickFix, 2.0f);

        Label fixLabel = new Label();
        fixLabel.setContentMode(ContentMode.HTML);
        fixLabel.setValue("After the fix, slice chosen above will be associated with the slice key chosen in the below combo box, "
                + "or the problematic slice can be edited manuallz in OntoWiki");
        panelLayout.addComponent(fixLabel);
        final ComboBox comboKeys = new ComboBox();
        comboKeys.setWidth("100%");
        comboKeys.setNullSelectionAllowed(false);
        panelLayout.addComponent(comboKeys);
        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setSpacing(true);
        Button editOW = new Button("Edit in OntoWiki");
        Button fix = new Button("Quick fix");
        btnLayout.addComponent(fix);
        btnLayout.addComponent(editOW);
        panelLayout.addComponent(btnLayout);
        panelLayout.setExpandRatio(btnLayout, 2.0f);

        editOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // TODO create replacement
            }
        });
        lsSlices.addValueChangeListener(new Property.ValueChangeListener() {
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
        lsSlices.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String slice = event.getProperty().toString();
                comboKeys.removeAllItems();
                TupleQueryResult resKeys = executeTupleQuery(ValidationFixUtils.ic09_getMatchingKeys(graph, slice));
                try {
                    while (resKeys.hasNext()) {
                        comboKeys.addItem(resKeys.next().getValue("key"));
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
            }
        });
        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Object selKey = comboKeys.getValue();
                Object selSlice = lsSlices.getValue();
                if (selKey == null || selSlice == null) {
                    Notification.show("No slice key or slice was selected");
                    return;
                }

                GraphQueryResult resFix = executeDoubleGraphQuery(ValidationFixUtils.ic09_removeSliceKeys(graph, selSlice.toString()), 
                        ValidationFixUtils.ic09_insertSliceKey(graph, selSlice.toString(), selKey.toString()));
                if (resFix != null) {
                    Notification.show("Fix executed");
                    // evaluate again after the fix
                    icQuery.eval();
                }
            }
        });
    }
    
}
