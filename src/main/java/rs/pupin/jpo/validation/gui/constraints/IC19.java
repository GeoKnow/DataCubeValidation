/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.ic.ICQuery;
import rs.pupin.jpo.validation.ic.ICQuerySimple;

/**
 *
 * @author vukm
 */
public class IC19 extends IntegrityConstraintComponent {
    
    private class QuickFixCodesFromCodeLists extends Window {

        public QuickFixCodesFromCodeLists(final String resource, final String codeList) {
            this.setCaption("Quick Fix");
            this.setWidth("400px");
            this.setHeight("300px");
            VerticalLayout content = new VerticalLayout();
            content.setSizeFull();
            if (resource == null || resource.isEmpty()) {
                content.addComponent(new Label("You need to select a resource first"));
            } else {
                content.addComponent(new Label("If you choose to apply the fix, the selected resource ("
                        + resource + ") will be of type skos:Conept and linked to the code list " + codeList
                        + " via skos:inScheme property"));
            }
            HorizontalLayout layoutButtons = new HorizontalLayout();
            content.addComponent(layoutButtons);
            content.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);
            Button btnOK = new Button("OK");
            layoutButtons.addComponent(btnOK);
            Button btnCancel = new Button("Cancel");
            layoutButtons.addComponent(btnCancel);

            btnCancel.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    close();
                }
            });

            btnOK.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (resource == null || resource.isEmpty()) {
                        return;
                    }

                    ArrayList<Statement> statements = new ArrayList<Statement>();
                    String concept = "http://www.w3.org/2004/02/skos/core#Concept";
                    String inScheme = "http://www.w3.org/2004/02/skos/core#inScheme";
                    String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
                    statements.add(getStatementFromUris(resource, type, concept));
                    statements.add(getStatementFromUris(resource, inScheme, codeList));
                    uploadStatements(statements);
                    close();
                    // evaluate again after the fix
                    icQuery.eval();
                }
            });
            setContent(content);
            QuickFixCodesFromCodeLists.this.center();
        }
    }

    public IC19(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public String getName() {
        return "IC-19 Codes from code list";
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix skos: <http://www.w3.org/2004/02/skos/core#> \n");
        strBuilder.append("select distinct ?dim ?v ?list \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?obs qb:dataSet ?ds . \n");
        strBuilder.append("  ?ds qb:structure ?dsd . \n");
        strBuilder.append("  ?dsd qb:component ?cs . \n");
        strBuilder.append("  ?cs qb:componentProperty ?dim . \n");
        strBuilder.append("  ?dim a qb:DimensionProperty . \n");
        strBuilder.append("  ?dim qb:codeList ?list . \n");
        strBuilder.append("  ?list a skos:ConceptScheme . \n");
        strBuilder.append("  ?obs ?dim ?v . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?v a skos:Concept . ?v skos:inScheme ?list . } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        final Iterator<BindingSet> res = icQuery.getResults();
        if (res == null) {
            Label label = new Label();
            label.setValue("ERROR");
            rootLayout.addComponent(label);
            return;
        }

        final HashMap<String, String> map = new HashMap<String, String>();
        while (res.hasNext()) {
            BindingSet set = res.next();
            map.put(set.getValue("v").stringValue(), set.getValue("list").stringValue());
        }

        if (map.isEmpty()) {
            Label label = new Label();
            label.setValue("All values of coded dimensions are linked to the code lists");
            rootLayout.addComponent(label);
            return;
        }

        Label label = new Label();
        label.setValue("Following resources should be of type skos:Concept and linked to the appropriate code list");
        rootLayout.addComponent(label);

        final ListSelect listValues = new ListSelect("Resources", map.keySet());
        listValues.setNullSelectionAllowed(false);
        rootLayout.addComponent(listValues);

        Button editInOW = new Button("Edit in OntoWiki");
        editInOW.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // TODO create a replacement
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        Button fix = new Button("Quick Fix");
        rootLayout.addComponent(buttonsLayout);
        rootLayout.setExpandRatio(buttonsLayout, 2.0f);
        buttonsLayout.addComponent(fix);
        buttonsLayout.addComponent(editInOW);
        fix.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String resource = (String) listValues.getValue();
                String codeList = map.get(resource);
                getUI().addWindow(new QuickFixCodesFromCodeLists(resource, codeList));
            }
        });
    }
    
}
