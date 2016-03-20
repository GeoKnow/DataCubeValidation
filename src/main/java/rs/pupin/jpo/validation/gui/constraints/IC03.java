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
public class IC03 extends IntegrityConstraintComponent {

    public IC03(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-3 DSD includes measure";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?dsd \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?dsd a qb:DataStructureDefinition . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?dsd qb:component ?cs . \n");
        strBuilder.append("    ?cs qb:componentProperty ?prop . \n");
        strBuilder.append("    ?prop a qb:MeasureProperty . \n");
        strBuilder.append("  } \n");
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

        final List<String> dsdList = new ArrayList<String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            dsdList.add(set.getValue("dsd").stringValue());
        }

        if (dsdList.isEmpty()) {
            Label label = new Label();
            label.setValue("All DSDs contain at least one measure");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following DSDs do not have at least one measure defined");
        rootLayout.addComponent(lbl);

        final ListSelect listDSDs = new ListSelect("DSDs", dsdList);
        listDSDs.setNullSelectionAllowed(false);
        listDSDs.setImmediate(true);
        rootLayout.addComponent(listDSDs);

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
        fixLabel.setValue("After the fix, component selected in the combo box below will be turned to measure, "
                + "or you can choose to edit the above selected DSD manually in OntoWiki");
        panelLayout.addComponent(fixLabel);
        final ComboBox comboComponents = new ComboBox();
        comboComponents.setWidth("100%");
        comboComponents.setNullSelectionAllowed(false);
        comboComponents.setImmediate(true);
        panelLayout.addComponent(comboComponents);
        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setSpacing(true);
        Button editOW = new Button("Edit in OntoWiki");
        editOW.setEnabled(owUrl != null);
        Button turnToMeasure = new Button("Turn to measure");
        btnLayout.addComponent(turnToMeasure);
        btnLayout.addComponent(editOW);
        panelLayout.addComponent(btnLayout);
        panelLayout.setExpandRatio(btnLayout, 2.0f);

        listDSDs.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String dsd = event.getProperty().getValue().toString();
                if (dsd == null || dsd.equalsIgnoreCase("")) {
                    return;
                }

                comboComponents.removeAllItems();
                TupleQueryResult qRes = executeTupleQuery(ValidationFixUtils.ic03_getRequiredAttributes(graph, dsd));
                try {
                    while (qRes.hasNext()) {
                        comboComponents.addItem(qRes.next().getValue("attr").toString());
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
            }
        });
        turnToMeasure.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Object selVal = comboComponents.getValue();
                if (selVal == null) {
                    Notification.show("No component was selected in the combo box");
                    return;
                }

                GraphQueryResult resFix = executeDoubleGraphQuery(ValidationFixUtils.ic03_turnToMeasure(graph, selVal.toString()), 
                        ValidationFixUtils.ic03_turnToMeasure2(graph, selVal.toString()));
                if (resFix != null) {
                    Notification.show("Fix executed");
                    icQuery.eval();
                }
            }
        });
        editOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editManually((String)listDSDs.getValue());
            }
        });
    }
    
}
