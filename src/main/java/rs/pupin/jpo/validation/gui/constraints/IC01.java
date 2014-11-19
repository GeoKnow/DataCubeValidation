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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class IC01 extends IntegrityConstraintComponent {

    public IC01(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
    }

    @Override
    public String getName() {
        return "IC-1 Observations have links";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?obs ?dsNum \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  { SELECT DISTINCT ?obs (0 as ?dsNum) WHERE { ?obs a qb:Observation . FILTER NOT EXISTS { ?obs qb:dataSet ?ds . } } } \n");
        strBuilder.append("  UNION \n");
        strBuilder.append("  { SELECT ?obs (count(distinct ?ds) as ?dsNum) { ?obs a qb:Observation . ?obs qb:dataSet ?ds . } group by ?obs } \n");
        strBuilder.append("  FILTER (?dsNum != 1) \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        Iterator<BindingSet> res = icQuery.getResults();
		
        if (icQuery.getStatus() == ICQuery.Status.ERROR) {
            Label label = new Label();
            label.setValue("ERROR \n" + icQuery.getErrorMessage());
            rootLayout.addComponent(label);
            return;
        }
		
        final HashMap<String, String> map = new HashMap<String, String>();
		
        while (res.hasNext()){
            BindingSet set = res.next();
            map.put(set.getValue("obs").stringValue(), set.getValue("dsNum").stringValue());
        }
		
        if (map.isEmpty()){
            Label label = new Label();
            label.setValue("All observations have links to data sets");
            rootLayout.addComponent(label);
            return;
        }
		
        Label label = new Label();
        label.setValue("Below is the list of observations that are not linked to exactly one data set. Click on any of them to get more information and either edit the resource in OntoWiki or choose a quick solution");
        rootLayout.addComponent(label);
		
        final ListSelect listObs = new ListSelect("Observations", map.keySet());
        listObs.setNullSelectionAllowed(false);
        rootLayout.addComponent(listObs);
        listObs.setImmediate(true);
        listObs.setWidth("100%");
		
        final Table detailsTable = new Table("Details");
        detailsTable.setHeight("250px");
        detailsTable.setWidth("100%");
        detailsTable.addContainerProperty("Property", String.class, null);
        detailsTable.addContainerProperty("Object", String.class, null);
        rootLayout.addComponent(detailsTable);
		
        final Label lblProblem = new Label("<b>Problem description: </b>", ContentMode.HTML);
        rootLayout.addComponent(lblProblem);
		
        Button editInOW = new Button("Edit in OntoWiki");
        editInOW.setEnabled(owUrl != null);
        editInOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editManually((String)listObs.getValue());
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
        panelLayout.addComponent(new Label("After the fix the selected observation will belong only to the data set selected below or you can choose to edit the selected observation manually in OntoWiki"));
        final ComboBox comboDataSets = new ComboBox(null, getDataSets());
        comboDataSets.setNullSelectionAllowed(false);
        comboDataSets.setWidth("100%");
        panelLayout.addComponent(comboDataSets);
        final Button fix = new Button("Quick Fix");
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(fix);
        buttonsLayout.addComponent(editInOW);
        panelLayout.addComponent(buttonsLayout);
        panelLayout.setExpandRatio(buttonsLayout, 2.0f);
		
        listObs.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                TupleQueryResult res = getResourceProperties((String)event.getProperty().getValue());
                int i=1;
                detailsTable.removeAllItems();
                try {
                    while (res.hasNext()){
                        BindingSet set = res.next();
                        detailsTable.addItem(new Object [] { set.getValue("p").stringValue(),
                                set.getValue("o").stringValue() }, i++);
                    }
                } catch (QueryEvaluationException e) {
                    e.printStackTrace();
                }
                String chosenObs = (String)event.getProperty().getValue();
                lblProblem.setValue("<b>Problem description: </b>The selected observation belongs to " + map.get(chosenObs) +
                                " data sets. It should belong to exactly one.");
            }
        });
		
        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String chosenDataSet = (String)comboDataSets.getValue();
                String observation = (String)listObs.getValue();

                if (chosenDataSet == null) {
                    Notification.show("DataSet was not selected", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                if (observation == null) {
                    Notification.show("Observation was not selected", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                String dataSetProp = "http://purl.org/linked-data/cube#dataSet";
                List<String> forRemoval = getObsDataSets(observation);
                if (forRemoval.size()>0){
                    ArrayList<Statement> stmts = new ArrayList<Statement>();
                    for (String ds: forRemoval)
                        stmts.add(getStatementFromUris(observation, dataSetProp, ds));
                    removeStatements(stmts);
                }
                ArrayList<Statement> addStmts = new ArrayList<Statement>();
                addStmts.add(getStatementFromUris(observation, dataSetProp, chosenDataSet));
                uploadStatements(addStmts);
                Notification.show("Fix executed");
                icQuery.eval();
            }
        });
    }
    
    private List<String> getObsDataSets(String obs){
        StringBuilder q = new StringBuilder();
        q.append("select ?ds from <").append(graph);
        q.append("> where { <").append(obs).append("> <http://purl.org/linked-data/cube#dataSet> ?ds . ");
        q.append("?ds a <http://purl.org/linked-data/cube#DataSet> . }");
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, q.toString());
            TupleQueryResult result = tupleQuery.evaluate();
            ArrayList<String> list = new ArrayList<String>();
            while (result.hasNext())
                list.add(result.next().getValue("ds").stringValue());
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
