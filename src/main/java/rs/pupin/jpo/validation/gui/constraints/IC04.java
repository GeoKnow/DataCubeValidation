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
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class IC04 extends IntegrityConstraintComponent {

    public IC04(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-4 Dimensions have range";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?dim \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?dim a qb:DimensionProperty . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?dim rdfs:range [] . } \n");
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

        final List<String> dimList = new ArrayList<String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            dimList.add(set.getValue("dim").stringValue());
        }

        if (dimList.isEmpty()) {
            Label label = new Label();
            label.setValue("All dimensions have a defined range");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following dimensions do not have a defined range");
        rootLayout.addComponent(lbl);

        final ListSelect listDimensions = new ListSelect("Dimensions", dimList);
        listDimensions.setNullSelectionAllowed(false);
        rootLayout.addComponent(listDimensions);

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
        fixLabel.setValue("After the fix, dimension chosen above will have a range chosen in the combo box below. "
                + "Alternatively, the problematic dimension can be edited manually in OntoWiki");
        panelLayout.addComponent(fixLabel);
        final ComboBox comboType = new ComboBox();
        comboType.setWidth("100%");
        comboType.setNullSelectionAllowed(false);
        panelLayout.addComponent(comboType);
        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setSpacing(true);
        Button editOW = new Button("Edit in OntoWiki");
        editOW.setEnabled(owUrl != null);
        Button fix = new Button("Quick fix");
        btnLayout.addComponent(fix);
        btnLayout.addComponent(editOW);
        panelLayout.addComponent(btnLayout);
        panelLayout.setExpandRatio(btnLayout, 2.0f);

        editOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editManually((String)listDimensions.getValue());
            }
        });
        listDimensions.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String dim = event.getProperty().getValue().toString();
                String query = ValidationFixUtils.ic04_getMathingRange(graph, dim);
                TupleQueryResult qRes = executeTupleQuery(query);
                comboType.removeAllItems();
                try {
                    while (qRes.hasNext()) {
                        comboType.addItem(qRes.next().getValue("type").stringValue());
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
            }
        });
        fix.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                Object selDim = listDimensions.getValue();
                Object selType = comboType.getValue();
                if (selDim == null || selType == null) {
                    Notification.show("Dimension or type was not selected");
                    return;
                }

                GraphQueryResult fixRes = executeGraphQuery(ValidationFixUtils.ic04_insertRange(
                        graph, selDim.toString(), selType.toString()));
                if (fixRes != null) {
                    Notification.show("Fix executed");
                    // update GUI after the fix
                    icQuery.eval();
                }
            }
        });
    }
    
}
