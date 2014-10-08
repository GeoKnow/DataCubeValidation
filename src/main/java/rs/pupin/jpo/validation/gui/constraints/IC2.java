/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.data.Property;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC2 extends IntegrityConstraintComponent {

    public IC2(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC2 - Unique DSD";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?dataSet ?dsdNum \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  { SELECT DISTINCT ?dataSet (0 as ?dsdNum) WHERE { ?dataSet a qb:DataSet . FILTER NOT EXISTS { ?dataSet qb:structure ?dsd . ?dsd a qb:DataStructureDefinition . } } } \n");
        strBuilder.append("  UNION \n");
        strBuilder.append("  { SELECT ?dataSet (count(distinct ?dsd) as ?dsdNum) { ?dataSet a qb:DataSet . ?dataSet qb:structure ?dsd . ?dsd a qb:DataStructureDefinition . } group by ?dataSet } \n");
        strBuilder.append("  FILTER (?dsdNum != 1) \n");
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

        final HashMap<String, String> map = new HashMap<String, String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            map.put(set.getValue("dataSet").stringValue(), set.getValue("dsdNum").stringValue());
        }

        if (map.size() == 0) {
            Label label = new Label();
            label.setValue("All data sets have exactly one link to the DSD");
            rootLayout.addComponent(label);
            return;
        }

        Label label = new Label();
        label.setValue("Below is the list of data sets that are not linked to exactly one DSD. Click on any of them to get more information and either edit the data set in OntoWiki or choose a quick solution");
        rootLayout.addComponent(label);

        final ListSelect listDataSets = new ListSelect("Data Sets", map.keySet());
        listDataSets.setNullSelectionAllowed(false);
        rootLayout.addComponent(listDataSets);
        listDataSets.setImmediate(true);
        listDataSets.setWidth("100%");

        final Table detailsTable = new Table("Details");
        detailsTable.setHeight("200px");
        detailsTable.setWidth("100%");
        detailsTable.addContainerProperty("Property", String.class, null);
        detailsTable.addContainerProperty("Object", String.class, null);
        rootLayout.addComponent(detailsTable);

        final Label lblProblem = new Label("<b>Problem description: </b>", Label.CONTENT_XHTML);
        rootLayout.addComponent(lblProblem);

        Button editInOW = new Button("Edit in OntoWiki");
        // Something instead

        Form panelQuickFix = new Form();
        panelQuickFix.setCaption("Quick Fix");
        panelQuickFix.setSizeFull();
        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setSpacing(true);
        panelLayout.setSizeFull();
        panelQuickFix.setLayout(panelLayout);
        rootLayout.addComponent(panelQuickFix);
        rootLayout.setExpandRatio(panelQuickFix, 2.0f);
        panelLayout.addComponent(new Label("After the fix the selected data sets will link only to the DSD selected below or you can choose to edit the selected data set manually in OntoWiki"));
        final ComboBox comboDSDs = new ComboBox(null, getDataStructureDefinitions());
        comboDSDs.setNullSelectionAllowed(false);
        comboDSDs.setWidth("100%");
        panelLayout.addComponent(comboDSDs);
        final Button fix = new Button("Quick Fix");
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(fix);
        buttonsLayout.addComponent(editInOW);
        panelLayout.addComponent(buttonsLayout);
        panelLayout.setExpandRatio(buttonsLayout, 2.0f);

        listDataSets.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                TupleQueryResult res = getResourceProperties((String) event.getProperty().getValue());
                int i = 1;
                detailsTable.removeAllItems();
                try {
                    while (res.hasNext()) {
                        BindingSet set = res.next();
                        detailsTable.addItem(new Object[]{set.getValue("p").stringValue(),
                            set.getValue("o").stringValue()}, new Integer(i++));
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
                String chosenDataSet = (String) event.getProperty().getValue();
                lblProblem.setValue("<b>Problem description: </b>The selected data set belongs to " + map.get(chosenDataSet)
                        + " DSDs. It should belong to exactly one.");
            }
        });

        fix.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                String chosenDSD = (String) comboDSDs.getValue();
                String dataSet = (String) listDataSets.getValue();

                if (chosenDSD == null) {
                    Notification.show("DSD was not selected", Notification.TYPE_ERROR_MESSAGE);
                    return;
                }
                if (dataSet == null) {
                    Notification.show("Data set was not selected", Notification.TYPE_ERROR_MESSAGE);
                    return;
                }

                String structProp = "http://purl.org/linked-data/cube#structure";
                List<String> forRemoval = getDataSetDSDs(dataSet);
                if (forRemoval.size() > 0) {
                    ArrayList<Statement> stmts = new ArrayList<Statement>();
                    for (String dsd : forRemoval) {
                        stmts.add(getStatementFromUris(dataSet, structProp, dsd));
                    }
                    removeStatements(stmts);
                }
                ArrayList<Statement> addStmts = new ArrayList<Statement>();
                addStmts.add(getStatementFromUris(dataSet, structProp, chosenDSD));
                uploadStatements(addStmts);
                Notification.show("Fix executed");
                icQuery.eval();
            }
        });
    }
    
    private List<String> getDataStructureDefinitions() {
        StringBuilder q = new StringBuilder();
        q.append("select ?dsd from <").append(graph);
        q.append("> where { ?dsd a <http://purl.org/linked-data/cube#DataStructureDefinition> . }");
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            TupleQueryResult result = tupleQuery.evaluate();
            ArrayList<String> list = new ArrayList<String>();
            while (result.hasNext()) {
                list.add(result.next().getValue("dsd").stringValue());
            }
            return list;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private List<String> getDataSetDSDs(String dataSet) {
        StringBuilder q = new StringBuilder();
        q.append("select ?dsd from <").append(graph);
        q.append("> where { <").append(dataSet).append("> <http://purl.org/linked-data/cube#structure> ?dsd . ");
        q.append("?dsd a <http://purl.org/linked-data/cube#DataStructureDefinition> . }");
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            TupleQueryResult result = tupleQuery.evaluate();
            ArrayList<String> list = new ArrayList<String>();
            while (result.hasNext()) {
                list.add(result.next().getValue("dsd").stringValue());
            }
            return list;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
