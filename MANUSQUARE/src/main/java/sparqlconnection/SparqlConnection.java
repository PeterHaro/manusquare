package sparqlconnection;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;


public class SparqlConnection {
	
	static String SPARQL_ENDPOINT = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0"; 
	static String AUTHORISATION_TOKEN = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	
	public static Repository initRepository () {
		Repository repository;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", AUTHORISATION_TOKEN);
		headers.put("accept", "application/JSON");
		repository = new SPARQLRepository(SPARQL_ENDPOINT);
		repository.initialize();
		((SPARQLRepository) repository).setAdditionalHttpHeaders(headers);
		
		return repository;
	}

	
	public static TupleQuery connect (Repository repository, boolean testing, String strQuery) {
		
		
		
		TupleQuery tupleQuery;
		
		//open connection to GraphDB and run SPARQL query
				try (RepositoryConnection conn = repository.getConnection()) {
				 tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, strQuery);

					//if querying the local KB, we need to set setIncludeInferred to false, otherwise inference will include irrelevant results.
					//when querying the Semantic Infrastructure the non-inference is set in the http parameters.
					if (testing) {
						//do not include inferred statements from the KB
						tupleQuery.setIncludeInferred(false);
					}
					
					return tupleQuery;
				}


	}

}
