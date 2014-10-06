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
public class IC3 extends IntegrityConstraintComponent {

    public IC3(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("select ?dsd \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  ?dsd a qb:DataStructureDefinition . \n");
        strBuilder.append("  FILTER NOT EXISTS { \n");
        strBuilder.append("    ?dsd qb:component ?cs . \n");
        strBuilder.append("    ?cs qb:componentProperty ?prop . \n");
        strBuilder.append("    ?prop a qb:MeasureProperty . \n");
        strBuilder.append("  } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
