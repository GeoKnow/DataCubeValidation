/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
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
public class IC12 extends IntegrityConstraintComponent {

    public IC12(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-12 No duplicate observations";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix skos: <http://www.w3.org/2004/02/skos/core#> \n");
        strBuilder.append("select distinct ?obs \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  ?dsd qb:component ?cs . \n");
        strBuilder.append("  ?cs qb:componentProperty ?dim . \n");
        strBuilder.append("  ?dim a qb:DimensionProperty . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?obs ?dim [] } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        
        if (icQuery.getStatus() == ICQuery.Status.ERROR) {
            Label label = new Label();
            label.setValue("ERROR \n" + icQuery.getErrorMessage());
            rootLayout.addComponent(label);
            return;
        }
        
        rootLayout.addComponent(new Label("Following observations belong to the same data set and have the same value for all dimensions."));

        final ListSelect ls1 = new ListSelect("Observations");
        ls1.setNullSelectionAllowed(false);
        ls1.setImmediate(true);
        ls1.setWidth("100%");
        rootLayout.addComponent(ls1);

        Iterator<BindingSet> res = icQuery.getResults();
        final HashMap<String, List<String>> mapDuplicates = new HashMap<String, List<String>>();
        String lastObs = "";
        List<String> lastDuplicates = null;
        while (res.hasNext()) {
            BindingSet set = res.next();
            String obs1 = set.getValue("obs1").stringValue();
            if (!obs1.equals(lastObs)) {
                lastObs = obs1;
                lastDuplicates = new ArrayList<String>();
                mapDuplicates.put(lastObs, lastDuplicates);
                ls1.addItem(lastObs);
            }
            lastDuplicates.add(set.getValue("obs2").stringValue());
        }

        final ListSelect ls2 = new ListSelect("Duplicates");
        ls2.setNullSelectionAllowed(false);
        ls2.setImmediate(true);
        ls2.setWidth("100%");
        rootLayout.addComponent(ls2);

        ls1.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ls2.removeAllItems();
                for (String duplicate : mapDuplicates.get(event.getProperty().getValue())) {
                    ls2.addItem(duplicate);
                }
            }
        });

        Form panelQuickFix = new Form();
        panelQuickFix.setCaption("Quick Fix");
        panelQuickFix.setSizeFull();
        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setSpacing(true);
        panelLayout.setSizeFull();
        panelQuickFix.setLayout(panelLayout);
        rootLayout.addComponent(panelQuickFix);
        rootLayout.setExpandRatio(panelQuickFix, 2.0f);
        panelLayout.addComponent(new Label("After the fix duplicates of the selected observation will be removed from the graph"));

        Button fix = new Button("Quick Fix");
        panelLayout.addComponent(fix);
        panelLayout.setExpandRatio(fix, 2.0f);

        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String obsString = (String) ls1.getValue();
                if (obsString == null || obsString.isEmpty()) {
                    Notification.show("Select observation first", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                ValueFactory factory = repository.getValueFactory();

                for (String duplicateString : mapDuplicates.get(obsString)) {
                    URI duplicateURI = factory.createURI(duplicateString);
                    TupleQueryResult res = getResourceProperties(duplicateString);
                    ArrayList<Statement> stmts = new ArrayList<Statement>();
                    try {
                        while (res.hasNext()) {
                            BindingSet set = res.next();
                            URI propURI = factory.createURI(set.getValue("p").stringValue());
                            Value objValue = set.getValue("o");
                            stmts.add(factory.createStatement(duplicateURI, propURI, objValue));
                        }
                        res = getResourceLinks(duplicateString);
                        while (res.hasNext()) {
                            BindingSet set = res.next();
                            URI propURI = factory.createURI(set.getValue("p").stringValue());
                            URI subURI = factory.createURI(set.getValue("s").stringValue());
                            stmts.add(factory.createStatement(subURI, propURI, duplicateURI));
                        }
                        removeStatements(stmts);
                    } catch (QueryEvaluationException e) {
                        e.printStackTrace();
                    }
                }
                Notification.show("Fix executed");
                // evaluate again after the fix
                icQuery.eval();
            }
        });
    }
    
}
