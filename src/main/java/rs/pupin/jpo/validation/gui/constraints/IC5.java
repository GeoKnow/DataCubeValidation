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
public class IC5 extends IntegrityConstraintComponent {

    public IC5(Repository repository, String graph) {
        super(repository, graph);
    }

    @Override
    public ICQuery generateICQuery() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("prefix qb: <http://purl.org/linked-data/cube#> \n");
        strBuilder.append("prefix skos: <http://www.w3.org/2004/02/skos/core#> \n");
        strBuilder.append("select distinct ?dim \n");
        strBuilder.append("from <").append(graph).append("> where { \n");
        strBuilder.append("  { { ?dim a qb:DimensionProperty . } UNION { [] qb:dimension ?dim . } } \n");
        strBuilder.append("  ?dim rdfs:range skos:Concept . \n");
        strBuilder.append("  FILTER NOT EXISTS { ?dim qb:codeList [] } \n");
        strBuilder.append("}");
        return new ICQuerySimple(repository, strBuilder.toString());
    }

    @Override
    public void generateGUI() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
