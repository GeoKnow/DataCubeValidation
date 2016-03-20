/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.ValidationFixUtils;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC13 extends IntegrityConstraintComponent {

    public IC13(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-13 Required attributes";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?obs ?attr \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  ?dsd qb:component ?component . \n");
        strBuilder.append("  ?component qb:componentRequired \"true\"^^xsd:boolean . \n");
        strBuilder.append("  ?component qb:componentProperty ?attr . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?obs ?attr [] } \n");
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

        final HashMap<String, String> obsMap = new HashMap<String, String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            obsMap.put(set.getValue("obs").stringValue(), set.getValue("attr").stringValue());
        }

        if (obsMap.isEmpty()) {
            Label label = new Label();
            label.setValue("No problems were detected - Every qb:Observation has a value for each declared attribute that is marked as required");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following observations do not have a value for required attribute(s)");
        rootLayout.addComponent(lbl);

        final ListSelect listObservations = new ListSelect("Observations", obsMap.keySet());
        listObservations.setNullSelectionAllowed(false);
        listObservations.setImmediate(true);
        rootLayout.addComponent(listObservations);

        // TODO: add label that tells which attribute is missing
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
        fixLabel.setValue(""); // TODO
        panelLayout.addComponent(fixLabel);

        HorizontalLayout btnLayout = new HorizontalLayout();
        btnLayout.setSpacing(true);
        Button removeRequired = new Button("Remove qb:componentRequired");
        Button editOW = new Button("Edit in OntoWiki");
        editOW.setEnabled(owUrl != null);
        btnLayout.addComponent(removeRequired);
        btnLayout.addComponent(editOW);
        panelLayout.addComponent(btnLayout);
        panelLayout.setExpandRatio(btnLayout, 2.0f);

        removeRequired.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                String chosenObs = (String) listObservations.getValue();
                if (chosenObs == null) {
                    Notification.show("Cannot execute the action",
                            "Observation needs to be chosen first",
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }
                String query = ValidationFixUtils.ic13_removeComponentRequiredTrue(graph,
                        chosenObs,
                        obsMap.get(chosenObs));
                GraphQueryResult fixRes = executeGraphQuery(query);
                if (fixRes != null) { 
                    Notification.show("Fix executed");
                    // evaluate again after the fix
                    icQuery.eval();
                }
            }
        });
        editOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editManually((String)listObservations.getValue());
            }
        });
    }
    
}
