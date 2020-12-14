package interfacetest;

import org.eclipse.rdf4j.query.TupleQuery;

public interface IConnection {
	
	TupleQuery connect(String query);

}
