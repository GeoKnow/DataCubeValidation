/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import java.util.Iterator;
import java.util.List;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import rs.pupin.jpo.validation.gui.InfoStatusToIconMapper;
import rs.pupin.jpo.validation.ic.ICQuery;

/**
 *
 * @author vukm
 */
public class Summary extends IntegrityConstraintComponent {

    public Summary(Repository repository, String graph) {
        super(repository, graph);
        this.statusMapper = InfoStatusToIconMapper.getInstance();
    }

    @Override
    public String getName() {
        return "Summary";
    }

    @Override
    public ICQuery generateICQuery() {
        ICQuery ic = new ICQuery() {

            @Override
            protected List<BindingSet> evaluate() {
                return null;
            }

            @Override
            public Status getStatus() {
                return Status.NEW;
            }

            @Override
            public Iterator<BindingSet> getResults() {
                return null;
            }
        };
        return ic;
    }

    @Override
    public void generateGUI() {
        rootLayout.removeAllComponents();
        Label label = new Label("", ContentMode.HTML);
        List<String> obsList = getObservations();
        List<String> dsList = getDataSets();
        List<String> dsdList = getDataStructureDefinitions();
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Summary</h2>");
        sb.append("This page contains summary information about the working graph, i.e. ");
        sb.append("number of observations, data sets, DSDs, dimensions, etc. ");
        sb.append("Therefore, this page only detects if some resources are missing, for more information, e.g. ");
        sb.append(" missing links refer to other validation criteria.");
        sb.append("<p>Summary information: <ul><li>");
        if (obsList.isEmpty()) {
            sb.append("ERROR - the graph is missing observations");
        } else {
            sb.append("There are ").append(obsList.size()).append(" observations");
        }
        sb.append("</li><li>");
        if (dsList.isEmpty()) {
            sb.append("ERROR - the graph is missing data sets");
        } else {
            sb.append("There are ").append(dsList.size()).append(" data sets");
        }
        sb.append("</li><li>");
        if (dsdList.isEmpty()) {
            sb.append("ERROR - the graph is missing data structure definitions");
        } else {
            sb.append("There are ").append(dsdList.size()).append(" data structure definitions");
        }
        sb.append("</li></ul></p>");
        sb.append("<p>TODO: add info about dimensions, maybe include pointers on cubeviz possibilities</p>");
        label.setValue(sb.toString());
        rootLayout.addComponent(label);
    }
    
}
