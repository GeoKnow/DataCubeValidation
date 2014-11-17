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
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.ValidationFixUtils;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC06 extends IntegrityConstraintComponent {

    public IC06(Repository repository, String graph) {
        super(repository, graph);
    }
    
    @Override
    public String getName() {
        return "IC-6 Only attributes may be optional";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select distinct ?dsd ?componentSpec ?component \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?dsd qb:component ?componentSpec . \n");
        strBuilder.append("  ?componentSpec qb:componentRequired \"false\"^^xsd:boolean . \n");
        strBuilder.append("  ?componentSpec qb:componentProperty ?component . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?component a qb:AttributeProperty } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        Iterator<BindingSet> res = icQuery.getResults();

        if (res == null) {
            Label label = new Label();
            label.setValue("ERROR");
            rootLayout.addComponent(label);
            return;
        }

        final HashMap<String, String> compMap = new HashMap<String, String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            compMap.put(set.getValue("component").stringValue(), set.getValue("dsd").stringValue());
        }

        if (compMap.isEmpty()) {
            Label label = new Label();
            label.setValue("No problems were detected - if there are any optional components, they are attributes");
            rootLayout.addComponent(label);
            return;
        }

        Label lbl = new Label();
        lbl.setValue("Following components are marked as optional, but they are not attributes");
        rootLayout.addComponent(lbl);

        final ListSelect listComponents = new ListSelect("Component Properties", compMap.keySet());
        listComponents.setNullSelectionAllowed(false);
        rootLayout.addComponent(listComponents);

        final Table detailsTable = new Table("Details");
        detailsTable.setHeight("200px");
        detailsTable.setWidth("100%");
        detailsTable.addContainerProperty("Property", String.class, null);
        detailsTable.addContainerProperty("Object", String.class, null);
        rootLayout.addComponent(detailsTable);
        listComponents.addValueChangeListener(new DetailsListener(detailsTable));

//		final Label lblProblem = new Label("<b>Problem description: </b>", Label.CONTENT_XHTML);
//		validationTab.addComponent(lblProblem);
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
        Button editOW = new Button("Edit in OntoWiki");
        Button removeCompReq = new Button("Remove qb:componentRequired");
        Button turnToAttr = new Button("Turn to attribute");
        btnLayout.addComponent(removeCompReq);
        btnLayout.addComponent(turnToAttr);
        btnLayout.addComponent(editOW);
        panelLayout.addComponent(btnLayout);
        panelLayout.setExpandRatio(btnLayout, 2.0f);

        removeCompReq.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String chosenComponent = (String) listComponents.getValue();
                if (chosenComponent == null) {
                    Notification.show("Cannot execute the action",
                            "A component needs to be chosen first",
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }
                String chosenDSD = compMap.get(chosenComponent);
                String query = ValidationFixUtils.ic06_removeComponentRequired(graph, chosenDSD, chosenComponent);
                executeGraphQuery(query);
                Notification.show("Fix executed");
                // evaluate again after the fix
                icQuery.eval();
            }
        });
        turnToAttr.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String chosenComponent = (String) listComponents.getValue();
                if (chosenComponent == null) {
                    Notification.show("Cannot execute the action",
                            "A component needs to be chosen first",
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }
                String query = ValidationFixUtils.ic06_changeToAttribute(graph, chosenComponent);
                String query2 = ValidationFixUtils.ic06_changeToAttribute2(graph, chosenComponent);
                executeGraphQuery(query);
                executeGraphQuery(query2);
                Notification.show("Fix executed");
                // evaluate again after the fix
                icQuery.eval();
            }
        });
        editOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // TODO create replacement
            }
        });
    }
    
}
