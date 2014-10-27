package rs.pupin.jpo.validation;

public class ValidationFixUtils {
	
	private static StringBuilder createBuilder(){
		StringBuilder builder = new StringBuilder();
		builder.append("PREFIX qb: <http://purl.org/linked-data/cube#> \n");
		builder.append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n");
		builder.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
		builder.append("PREFIX dct: <http://purl.org/dc/terms/> \n");
		builder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		return builder;
	}
	
	public static String ic03_getDimensions(String graph, String dsd){
		StringBuilder query = createBuilder();
		query.append("SELECT ?dim \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  <").append(dsd).append("> qb:component ?cs . \n");
		query.append("  ?cs qb:componentProperty ?dim . \n");
		query.append("  ?dim a qb:DimensionProperty . \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic03_getRequiredAttributes(String graph, String dsd){
		StringBuilder query = createBuilder();
		query.append("SELECT DISTINCT ?attr \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  <").append(dsd).append("> qb:component ?cs . \n");
		query.append("  ?cs qb:componentRequired \"true\"^^xsd:boolean . \n");
		query.append("  { { ?cs qb:attribute ?attr. } UNION { \n");
		query.append("    ?cs qb:componentProperty ?attr . \n");
		query.append("    ?attr a qb:AttributeProperty . \n");
		query.append("  }} \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic03_turnToMeasure(String graph, String dim){
		StringBuilder query = createBuilder();
		query.append("MODIFY GRAPH <").append(graph).append("> \n");
		query.append("DELETE { \n");
		query.append("  <").append(dim).append("> a ?type . \n");
		query.append("} \n");
		query.append("INSERT { \n");
		query.append("  <").append(dim).append("> a qb:MeasureProperty . \n");
		query.append("} \n");
		query.append("WHERE { \n");
		query.append("  <").append(dim).append("> a ?type . \n");
		query.append("  FILTER (?type IN (qb:DimensionProperty, qb:AttributeProperty)) \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic03_turnToMeasure2(String graph, String dim){
		StringBuilder query = createBuilder();
		query.append("MODIFY GRAPH <").append(graph).append("> \n");
		query.append("DELETE { \n");
		query.append("  ?s ?prop <").append(dim).append("> . \n");
		query.append("} \n");
		query.append("INSERT { \n");
		query.append("  ?s qb:measure <").append(dim).append("> . \n");
		query.append("} \n");
		query.append("WHERE { \n");
		query.append("  ?s ?prop <").append(dim).append("> . \n");
		query.append("  FILTER (?prop IN (qb:dimension, qb:attribute)) \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic04_getMathingRange(String graph, String dim){
		StringBuilder query = createBuilder();
		query.append("SELECT DISTINCT ?type \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?obs qb:dataSet ?ds . \n");
		query.append("  ?ds qb:structure ?dsd . \n");
		query.append("  ?dsd qb:component ?cs . \n");
		query.append("  ?cs ?prop <").append(dim).append("> . ");
		query.append("  FILTER (?prop IN (qb:dimension, qb:componentProperty)) \n");
		query.append("  ?obs <").append(dim).append("> ?val . \n");
		query.append("  ?val a ?type . \n");
		query.append("  FILTER NOT EXISTS { \n");
		query.append("    ?obs2 qb:dataSet ?ds2 . \n");
		query.append("    ?ds2 qb:structure ?dsd2 . \n");
		query.append("    ?dsd2 qb:component ?cs2 . \n");
		query.append("    ?cs2 ?prop2 <").append(dim).append("> . ");
		query.append("    FILTER (?prop2 IN (qb:dimension, qb:componentProperty)) \n");
		query.append("    ?obs2 <").append(dim).append("> ?val2 . \n");
		query.append("    FILTER NOT EXISTS { ?val2 a ?type . } \n");
		query.append("  } \n");
		query.append("}");
		// can this be optimized?
		return query.toString();
	}
	
	public static String ic04_insertRange(String graph, String dim, String range){
		StringBuilder query = createBuilder();
		query.append("INSERT INTO GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(dim).append("> rdfs:range <").append(range).append("> . \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic05_getMathingCodeLists(String graph, String dim){
		StringBuilder query = createBuilder();
		query.append("SELECT DISTINCT ?list \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?list a skos:ConceptScheme . \n");
		query.append("  FILTER NOT EXISTS { \n");
		query.append("    ?obs qb:dataSet ?ds . \n");
		query.append("    ?ds qb:structure ?dsd . \n");
		query.append("    ?dsd qb:component ?cs . \n");
		query.append("    ?cs ?prop <").append(dim).append("> . ");
		query.append("    FILTER (?prop IN (qb:dimension, qb:componentProperty)) \n");
		query.append("    ?obs <").append(dim).append("> ?val . \n");
		query.append("    FILTER NOT EXISTS { \n");
		query.append("      ?val skos:inScheme ?list . \n");
		query.append("    } \n");
		query.append("  } \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic05_getOtherCodeLists(String graph, String dim){
		StringBuilder query = createBuilder();
		query.append("SELECT DISTINCT ?list \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?list a skos:ConceptScheme . \n");
		query.append("  FILTER EXISTS { \n");
		query.append("    ?obs qb:dataSet ?ds . \n");
		query.append("    ?ds qb:structure ?dsd . \n");
		query.append("    ?dsd qb:component ?cs . \n");
		query.append("    ?cs ?prop <").append(dim).append("> . ");
		query.append("    FILTER (?prop IN (qb:dimension, qb:componentProperty)) \n");
		query.append("    ?obs <").append(dim).append("> ?val . \n");
		query.append("    FILTER NOT EXISTS { \n");
		query.append("      ?val skos:inScheme ?list . \n");
		query.append("    } \n");
		query.append("  } \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic05_insertCodeList(String graph, String dim, String codeList){
		StringBuilder query = createBuilder();
		query.append("INSERT INTO GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(dim).append("> qb:codeList <").append(codeList).append("> . \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic06_removeComponentRequired(String graph, String dsd, String componentProperty){
		StringBuilder query = createBuilder();
		query.append("DELETE FROM GRAPH <").append(graph).append("> { \n");
		query.append("  ?componentSpec qb:componentRequired \"false\"^^xsd:boolean . \n");
		query.append("} \n");
		query.append("WHERE { GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(dsd).append("> qb:component ?componentSpec . \n");
		query.append("  ?componentSpec ?prop <").append(componentProperty).append("> . \n");
		query.append("  FILTER (?prop IN (qb:measure, qb:dimension, qb:attribute, qb:componentProperty)) ");
		query.append("} } ");
		return query.toString();
	}
	
	public static String ic06_changeToAttribute(String graph, String componentProperty){
		StringBuilder query = createBuilder();
		query.append("MODIFY GRAPH <").append(graph).append("> \n");
		query.append("DELETE { \n");
		query.append("  <").append(componentProperty).append("> a ?type . \n");
		query.append("} \n");
		query.append("INSERT { \n");
		query.append("  <").append(componentProperty).append("> a qb:AttributeProperty . \n");
		query.append("} \n");
		query.append("WHERE { \n");
		query.append("  <").append(componentProperty).append("> a ?type . \n");
		query.append("  FILTER (?type IN (qb:MeasureProperty, qb:DimensionProperty)) \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic06_changeToAttribute2(String graph, String componentProperty){
		StringBuilder query = createBuilder();
		query.append("MODIFY GRAPH <").append(graph).append("> \n");
		query.append("DELETE { \n");
		query.append("  ?cs ?prop <").append(componentProperty).append("> . \n");
		query.append("} \n");
		query.append("INSERT { \n");
		query.append("  ?cs qb:attribute <").append(componentProperty).append("> . \n");
		query.append("} \n");
		query.append("WHERE { \n");
		query.append("  ?cs ?prop <").append(componentProperty).append("> . \n");
		query.append("  FILTER (?prop IN (qb:measure, qb:dimension)) \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic07_getMatchingDSDs(String graph, String key){
		StringBuilder query = createBuilder();
		query.append("SELECT DISTINCT ?dsd \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?dsd a qb:DataStructureDefinition . \n");
		query.append("  FILTER NOT EXISTS { \n");
		query.append("    <").append(key).append("> qb:componentProperty ?prop . \n");
		query.append("    FILTER NOT EXISTS { \n");
		query.append("      ?dsd qb:component ?cs . \n");
		query.append("      ?cs ?p2 ?prop . \n");
		query.append("      FILTER (?p2 IN (qb:componentProperty, qb:dimension)) . \n");
		query.append("    } \n");
		query.append("  } \n");
		query.append("}");
		return query.toString();
	}
	
	// TODO: consider including getOtherDSDs???
	
	public static String ic07_insertConnection(String graph, String dsd, String key){
		StringBuilder query = createBuilder();
		query.append("INSERT INTO GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(dsd).append("> qb:sliceKey <").append(key).append("> . \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic08_getMatchingDSDs(String graph, String key){
		return ic07_getMatchingDSDs(graph, key);
	}
	
	public static String ic08_removeExistingConnection(String graph, String key){
		StringBuilder query = createBuilder();
		query.append("DELETE FROM GRAPH <").append(graph).append("> { \n");
		query.append("  ?dsd qb:sliceKey <").append(key).append("> . \n");
		query.append("} \n");
		query.append("WHERE { \n");
		query.append("  ?dsd qb:sliceKey <").append(key).append("> . \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic08_insertConnection(String graph, String dsd, String key){
		return ic07_insertConnection(graph, dsd, key);	
	}
	
	public static String ic08_removeComponentFromKey(String graph, String comp, String key){
		StringBuilder query = createBuilder();
		query.append("DELETE FROM GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(key).append("> qb:componentProperty <").append(comp).append("> . \n");
		query.append("} \n");
		return query.toString();
	}
	
	public static String ic09_getMatchingKeys(String graph, String slice){
		StringBuilder query = createBuilder();
		query.append("SELECT DISTINCT ?key \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?key a qb:SliceKey . \n");
		query.append("  FILTER NOT EXISTS { \n");
		query.append("    <").append(slice).append("> ?dim ?val . \n");
		query.append("    ?dim a qb:DimensionProperty . \n");
		query.append("    FILTER NOT EXISTS { \n");
		query.append("      ?key qb:componentProperty ?dim . \n");
		query.append("    } \n");
		query.append("  } \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic09_getOtherKeys(String graph, String slice){
		StringBuilder query = createBuilder();
		query.append("SELECT DISTINCT ?key \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?key a qb:SliceKey . \n");
		query.append("  FILTER EXISTS { \n");
		query.append("    <").append(slice).append("> ?dim ?val . \n");
		query.append("    ?dim a qb:DimensionProperty . \n");
		query.append("    FILTER NOT EXISTS { \n");
		query.append("      ?key qb:componentProperty ?dim . \n");
		query.append("    } \n");
		query.append("  } \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic09_removeSliceKeys(String graph, String slice){
		StringBuilder query = createBuilder();
		query.append("DELETE FROM GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(slice).append("> qb:sliceStructure ?key . \n");
		query.append("} \n");
		query.append("WHERE { \n");
		query.append("  <").append(slice).append("> qb:sliceStructure ?key . \n");
		query.append("} ");
		return query.toString();
	}
	
	public static String ic09_insertSliceKey(String graph, String slice, String key){
		StringBuilder query = createBuilder();
		query.append("INSERT INTO GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(slice).append("> qb:sliceStructure <").append(key).append("> . \n");
		query.append("} \n");
		return query.toString();
	}
	
	public static String ic10_getCodeLists(String graph, String dim){
		return ic11_getCodeLists(graph, dim);
	}
	
	public static String ic10_getDuplicates(String graph, String slice, String [] dims, String [] vals){
		StringBuilder query = createBuilder();
		query.append("SELECT ?slice2 \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?ds qb:slice <").append(slice).append("> . \n");
		query.append("  <").append(slice).append("> qb:sliceStructure ?key . \n");
		query.append("  ?ds qb:slice ?slice2 . \n");
		query.append("  ?slice2 qb:sliceStructure ?key . \n");
		for (int i=0; i<dims.length; i++){
			query.append("  ?slice2 <").append(dims[i]).append("> ").append(vals[i]).append(" \n");
		}
		query.append("  FILTER (?slice2 != <").append(slice).append(">) \n");
		query.append("  FILTER NOT EXISTS { \n");
		query.append("    <").append(slice).append("> ?dim ?val1 . \n");
		query.append("    ?dim a qb:DimensionProperty . \n");
		query.append("    ?key qb:componentProperty ?dim . \n");
		query.append("    ?slice2 ?dim ?val2 . \n");
		query.append("    FILTER (?val1 != ?val2) \n");
		query.append("  } \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic10_insertDims(String graph, String slice, String [] dims, String [] vals){
		StringBuilder query = createBuilder();
		query.append("INSERT INTO GRAPH <").append(graph).append("> { \n");
		for (int i=0; i<dims.length; i++){
			query.append("  <").append(slice).append("> <").append(dims[i]).append("> ").append(vals[i]).append(" . \n");
		}
		query.append("} ");
		return query.toString();
	}
	
	public static String ic11_getCodeLists(String graph, String dim){
		StringBuilder query = createBuilder();
		query.append("SELECT ?code \n");
		query.append("FROM <").append(graph).append("> \n");
		query.append("WHERE { \n");
//		query.append("  <").append(dim).append("> rdfs:range skos:Concept . \n");
		query.append("  <").append(dim).append("> qb:codeList ?list . \n");
		query.append("  ?code skos:inScheme ?list . \n");
//		query.append("  ?code a skos:Concept . \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic11_getDuplicates(String graph, String obs, String [] dims, String [] vals){
		StringBuilder query = createBuilder();
		query.append("SELECT ?obs2 \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  <").append(obs).append("> qb:dataSet ?dataSet . \n");
		query.append("  ?obs2 qb:dataSet ?dataSet . \n");
		for (int i=0; i<dims.length; i++){
			query.append("  ?obs2 <").append(dims[i]).append("> ").append(vals[i]).append(" \n");
		}
		query.append("  FILTER (?obs2 != <").append(obs).append(">) \n");
		query.append("  FILTER NOT EXISTS { \n");
		query.append("    ?dataSet qb:structure ?dsd . \n");
		query.append("    ?dsd qb:component ?cs . \n");
		query.append("    ?cs qb:componentProperty ?dim . \n");
		query.append("    ?dim a qb:DimensionProperty . \n");
		query.append("    <").append(obs).append("> ?dim ?val1 . \n");
		query.append("    ?obs2 ?dim ?val2 . \n");
		query.append("    FILTER (?val1 != ?val2) \n");
		query.append("  } \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic11_insertDims(String graph, String obs, String [] dims, String [] vals){
		StringBuilder query = createBuilder();
		query.append("INSERT INTO GRAPH <").append(graph).append("> { \n");
		for (int i=0; i<dims.length; i++){
			query.append("  <").append(obs).append("> <").append(dims[i]).append("> ").append(vals[i]).append(" . \n");
		}
		query.append("} ");
		return query.toString();
	}
	
	public static String ic13_removeComponentRequiredTrue(String graph, String obs, String attr){
		StringBuilder query = createBuilder();
		query.append("DELETE FROM GRAPH <").append(graph).append("> { \n");
		query.append("  ?cs qb:componentRequired \"true\"^^xsd:boolean . \n");
		query.append("} \n");
		query.append("WHERE { GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(obs).append("> qb:dataSet ?ds . \n");
		query.append("  ?ds qb:structure ?dsd . \n");
		query.append("  ?dsd qb:component ?cs . \n");
		query.append("  ?cs ?prop <").append(attr).append("> . \n");
		query.append("  FILTER (?prop IN (qb:measure, qb:dimension, qb:attribute, qb:componentProperty)) ");
		query.append("} } ");
		return query.toString();
	}
	
	public static String ic16_removeExcessMeasures(String graph, String obs, String measure){
		StringBuilder query = createBuilder();
		query.append("DELETE FROM GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(obs).append("> ?measure [] . \n");
		query.append("} \n");
		query.append("WHERE { GRAPH <").append(graph).append("> { \n");
		query.append("  <").append(obs).append("> qb:measureType ?mtype . \n");
		query.append("  <").append(obs).append("> ?measure [] . \n");
		query.append("  ?measure a qb:MeasureProperty . \n");
		query.append("  FILTER (?measure != ?mtype) \n");
		query.append("}");
		return query.toString();
	}
	
	public static String ic19_getCodesFromCodeList(String graph, String codeList){
		StringBuilder query = createBuilder();
		query.append("SELECT ?code \n");
		query.append("FROM <").append(graph).append("> WHERE { \n");
		query.append("  ?code a skos:Concept . \n");
		query.append("  ?code skos:inScheme <").append(codeList).append("> . \n");
		query.append("}");
		return query.toString();
	}

}
