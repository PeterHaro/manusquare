package utilities;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;


public class SparqlConnection {
	
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
