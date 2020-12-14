package interfacetest;

import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public class GraphDBConnection implements IConnection {
	
	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "BP_2";

	@Override
	public TupleQuery connect(String query) {
		
		Repository repository = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
		HTTPRepository repo = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
		System.out.println(repo.getRepositoryURL());
		System.out.println(repo.getPreferredRDFFormat());
		repository.initialize();
		System.out.println(repository.isInitialized());
		
		return null;
	}



}
