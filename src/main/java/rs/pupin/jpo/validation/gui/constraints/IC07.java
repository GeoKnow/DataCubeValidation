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
public class IC07 extends IntegrityConstraintComponent {

    public IC07(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-7 Slice Keys must be declared";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?sliceKey \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?sliceKey a qb:SliceKey . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?dsd a qb:DataStructureDefinition . \n");
        strBuilder.append("    ?dsd qb:sliceKey ?sliceKey . \n");
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
            label.setValue("No problems were detected - either there are no slice keys or every slice key is associated with a DSD");
            rootLayout.addComponent(label);
            return;
        }

        Label label = new Label();
        label.setValue("Following slice keys should be associated with a DSD");
        rootLayout.addComponent(label);
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
        fixLabel.setValue("After the fix, slice key chosen above will be associated with the DSD chosen below, or you can edit the slice key manually.");
        panelLayout.addComponent(fixLabel);
        final ComboBox comboDSDs = new ComboBox();
        comboDSDs.setNullSelectionAllowed(false);
        comboDSDs.setWidth("100%");
        panelLayout.addComponent(comboDSDs);
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
        lsSliceKeys.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String sk = event.getProperty().getValue().toString();
                if (sk == null || sk.equalsIgnoreCase("")) {
                    return;
                }

                TupleQueryResult resSliceKeys = executeTupleQuery(ValidationFixUtils.ic07_getMatchingDSDs(graph, sk));
                comboDSDs.removeAllItems();
                try {
                    while (resSliceKeys.hasNext()) {
                        comboDSDs.addItem(resSliceKeys.next().getValue("dsd").stringValue());
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
            }
        });
        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Object selVal = comboDSDs.getValue();
                if (selVal == null) {
                    Notification.show("No DSD was selected");
                    return;
                }
                GraphQueryResult fixRes = executeGraphQuery(ValidationFixUtils.ic07_insertConnection(graph,
                        selVal.toString(), lsSliceKeys.getValue().toString()));

                if (fixRes != null) {
                    Notification.show("Fix executed");
                    // evaluate again thereby also updating the GUI
                    icQuery.eval();
                }
            }
        });
    }
    
}
