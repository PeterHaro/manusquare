package interfacetest;

import org.eclipse.rdf4j.query.TupleQuery;

public class ConnectApp {
	
	public static void main(String[] args) {
		
		
		IConnection connection = new GraphDBConnection();
		
		String query = null;
		
		
		
		TupleQuery results = connection.connect(query);
		
	}

}
