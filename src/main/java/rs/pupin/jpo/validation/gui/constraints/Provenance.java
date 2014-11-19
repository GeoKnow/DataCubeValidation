/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.gui.InfoStatusToIconMapper;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class Provenance extends IntegrityConstraintComponent {

    public Provenance(Repository repository, String graph, String owUrl) {
        super(repository, graph, owUrl);
        this.statusMapper = InfoStatusToIconMapper.getInstance();
    }

    @Override
    public String getName() {
        return "Provenance";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix dct: <http://purl.org/dc/terms/> \n");
        strBuilder.append("select ?ds ?label ?comment ?title ?description ?issued ?modified ?subject ?publisher ?licence \n");
        strBuilder.append("from <").append(graph).append("> \n where { \n");
        strBuilder.append("  ?ds a qb:DataSet . \n");
        strBuilder.append("  OPTIONAL { ?ds rdfs:label ?label . } \n");
        strBuilder.append("  OPTIONAL { ?ds rdfs:comment ?comment . } \n");
        strBuilder.append("  OPTIONAL { ?ds dct:title ?title . } \n");
        strBuilder.append("  OPTIONAL { ?ds dct:description ?description . } \n");
        strBuilder.append("  OPTIONAL { ?ds dct:issued ?issued . } \n");
        strBuilder.append("  OPTIONAL { ?ds dct:modified ?modified . } \n");
        strBuilder.append("  OPTIONAL { ?ds dct:subject ?subject . } \n");
        strBuilder.append("  OPTIONAL { ?ds dct:publisher ?publisher . } \n");
        strBuilder.append("  OPTIONAL { ?ds dct:licence ?licence . } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        final String[] metaProps = new String[]{
            "rdfs:label",
            "rdfs:comment",
            "dct:title",
            "dct:description",
            "dct:issued",
            "dct:modified",
            "dct:subject",
            "dct:publisher",
            "dct:licence"
        };

        final Label label = new Label("It is recommended to mark datasets with metadata tu support discovery, presentation and processing. Choose a dataset below and check the values for recommended core set of metadata", Label.CONTENT_TEXT);
        rootLayout.addComponent(label);
        
        Iterator<BindingSet> res = icQuery.getResults();
        if (res == null) {
            label.setValue("ERROR");
            return;
        }
        final HashMap<String, ArrayList<Value>> map = new HashMap<String, ArrayList<Value>>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            String ds = set.getValue("ds").stringValue();
            ArrayList<Value> values = new ArrayList<Value>(9);
            values.add(set.getValue("label"));
            values.add(set.getValue("comment"));
            values.add(set.getValue("title"));
            values.add(set.getValue("description"));
            values.add(set.getValue("issued"));
            values.add(set.getValue("modified"));
            values.add(set.getValue("subject"));
            values.add(set.getValue("publisher"));
            values.add(set.getValue("licence"));
            map.put(ds, values);
        }

        final ComboBox combo = new ComboBox("Choose dataset", map.keySet());
        combo.setWidth("100%");
        combo.setNullSelectionAllowed(false);
        combo.setImmediate(true);
        rootLayout.addComponent(combo);

        final Table table = new Table("Metadata of the chosen dataset");
        table.setWidth("100%");
        table.addContainerProperty("Property", String.class, null);
        table.addContainerProperty("Value", Value.class, null);
        rootLayout.addComponent(table);

        Button editInOW = new Button("Edit in OntoWiki");
        rootLayout.addComponent(editInOW);
        rootLayout.setExpandRatio(editInOW, 2.0f);
        editInOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // TODO do something here
            }
        });

        combo.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ArrayList<Value> list = map.get((String) event.getProperty().getValue());
                table.removeAllItems();
                for (int i = 0; i < metaProps.length; i++) {
                    table.addItem(new Object[]{metaProps[i], list.get(i)}, i);
                }
            }
        });
    }
    
}
