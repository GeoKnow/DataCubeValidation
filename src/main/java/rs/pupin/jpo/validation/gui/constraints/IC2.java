/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation.gui.constraints;

import org.openrdf.repository.Repository;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
