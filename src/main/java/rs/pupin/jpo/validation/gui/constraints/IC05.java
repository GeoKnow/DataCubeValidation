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
public class IC05 extends IntegrityConstraintComponent {

    public IC05(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-5 Concept dimensions have code lists";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix skos: <http://www.w3.org/2004/02/skos/core#> \n");
        strBuilder.append("select distinct ?dim \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?dim a qb:DimensionProperty . \n");
        strBuilder.append("  ?dim rdfs:range skos:Concept . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?dim qb:codeList [] } \n");
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
            label.setValue("No problems were detected - every dimension with range skos:Concept has a code list");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following dimensions with range skos:Concept do not have a code list");
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
        fixLabel.setValue("After the fix, dimension chosen above will be associated with the code list chosen in the combo box below. "
                + "Alternatively, the problematic dimension can be edited manually in OntoWiki");
        panelLayout.addComponent(fixLabel);
        final ComboBox comboCodeLists = new ComboBox();
        comboCodeLists.setWidth("100%");
        comboCodeLists.setNullSelectionAllowed(false);
        panelLayout.addComponent(comboCodeLists);
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
                String query = ValidationFixUtils.ic05_getMathingCodeLists(graph, dim);
                TupleQueryResult qRes = executeTupleQuery(query);
                comboCodeLists.removeAllItems();
                try {
                    while (qRes.hasNext()) {
                        comboCodeLists.addItem(qRes.next().getValue("list").stringValue());
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
            }
        });
        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Object selDim = listDimensions.getValue();
                Object selList = comboCodeLists.getValue();
                if (selDim == null || selList == null) {
                    Notification.show("Dimension or code list was not selected");
                    return;
                }

                GraphQueryResult fixRes = executeGraphQuery(ValidationFixUtils.ic05_insertCodeList(
                        graph, selDim.toString(), selList.toString()));
                if (fixRes != null) {
                    Notification.show("Fix executed");
                    icQuery.eval();
                }
            }
        });
    }
    
}
