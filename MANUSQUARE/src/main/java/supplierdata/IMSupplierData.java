package supplierdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.model.OWLOntology;

import edm.Certification;
import query.IMQuery;
import sparqlconnection.SparqlConnection;
import sparqlquery.IMSparqlQuery;
import sparqlresult.IMSparqlResult;
import supplier.IMSupplier;
import utilities.StringUtilities;

public class IMSupplierData {

	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "INNOVATIONMANAGEMENT";

	
	public static List<IMSupplier> createInnovationManagerData(IMQuery query, boolean testing, OWLOntology onto, String SPARQL_ENDPOINT, String AUTHORISATION_TOKEN) {
		
		String strQuery = IMSparqlQuery.createSparqlQuery_IM(query, onto);
		Set<IMSparqlResult> sparqlResults = new HashSet<IMSparqlResult>();

		Repository repository;

		if (!testing) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", AUTHORISATION_TOKEN);
			headers.put("accept", "application/JSON");
			
			repository = new SPARQLRepository(SPARQL_ENDPOINT);
			repository.initialize();
			((SPARQLRepository) repository).setAdditionalHttpHeaders(headers);
		
		} else {
			//connect to GraphDB
			repository = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
			HTTPRepository repo = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
			System.out.println(repo.getRepositoryURL());
			System.out.println(repo.getPreferredRDFFormat());
			repository.initialize();
			System.out.println(repository.isInitialized());
		}

		//connect to triplestore and retrieve sparql results
		TupleQuery tupleQuery = SparqlConnection.connect(repository, testing, strQuery);

			try (TupleQueryResult result = tupleQuery.evaluate()) {

				IMSparqlResult sparqlResult = null;

				while (result.hasNext()) {

					BindingSet solution = result.next();
					
					String certification = null;
					if (solution.getValue("certificationType") != null) {
					certification = StringUtilities.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", ""));
					}
					
					String innovationSector = StringUtilities.stripIRI(solution.getValue("innovationSectorType").stringValue().replaceAll("\\s+", ""));
					String skill = StringUtilities.stripIRI(solution.getValue("skillType").stringValue().replaceAll("\\s+", ""));
					String innovationType = StringUtilities.stripIRI(solution.getValue("innovationTypeType").stringValue().replaceAll("\\s+", ""));
					String innovationPhase = StringUtilities.stripIRI(solution.getValue("innovationPhaseType").stringValue().replaceAll("\\s+", ""));
					String supplierId = StringUtilities.stripIRI(solution.getValue("supplier").stringValue().replaceAll("\\s+", ""));
					String supplierName = StringUtilities.stripIRI(solution.getValue("supplierName").stringValue().replaceAll("\\s+", ""));
					
					sparqlResult = new IMSparqlResult.Builder(innovationSector, skill, innovationType, innovationPhase)
							.setSupplierId(supplierId)
							.setSupplierName(supplierName)
							.setCertification(certification)
							.build();

					sparqlResults.add(sparqlResult);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		

		//close connection to KB repository
		repository.shutDown();
		
		List<IMSupplier> suppliersList = consolidateSuppliers(sparqlResults);
		
		return suppliersList;
		
	}
		
		public static List<IMSupplier> consolidateSuppliers (Set<IMSparqlResult> sparqlResults) {
			
			List<IMSupplier> suppliersList = new ArrayList<IMSupplier>();
			
			//get unique supplier ids used for constructing the supplier structure below
			Set<String> supplierIds = new HashSet<String>();

			for (IMSparqlResult sr : sparqlResults) {
				supplierIds.add(sr.getSupplierId());
			}

			Certification certification = null;
			String innovationPhase = null;
			String innovationType = null;
			String skill = null;
			String sector = null;
			IMSupplier imSupplier = null;
			
			for (String id : supplierIds) {


				List<Certification> certifications = new ArrayList<Certification>();
				List<String> innovationPhases = new ArrayList<String>();
				List<String> innovationTypes = new ArrayList<String>();
				List<String> skills = new ArrayList<String>();
				List<String> sectors = new ArrayList<String>();

				for (IMSparqlResult sr : sparqlResults) {

					if (sr.getSupplierId().equals(id)) {
											
						imSupplier = new IMSupplier();

						//add certifications
						certification = new Certification(sr.getCertification());
						if (certification.getId() != null && !certifications.contains(certification)) {
							certifications.add(certification);
						}
						
						//add innovationPhases
						innovationPhase = sr.getInnovationPhase();
						if (innovationPhase != null && !innovationPhases.contains(innovationPhase)) {
							innovationPhases.add(innovationPhase);
						}
						
						//add innovationTypes
						innovationType = sr.getInnovationType();
						if (innovationType != null && !innovationTypes.contains(innovationType)) {
							innovationTypes.add(innovationType);
						}
						
						//add skills
						skill = sr.getSkill();
						if (skill != null && !skills.contains(skill)) {
							skills.add(skill);
						}
						
						//add sectors
						sector = sr.getSector();
						if (sector != null && !sectors.contains(sector)) {
							sectors.add(sector);
						}
						

						imSupplier = new IMSupplier(id, sr.getSupplierName(), certifications, skills, innovationPhases,
								innovationTypes, sectors);
						
					}
				}


				suppliersList.add(imSupplier);
			}
			
			return suppliersList;
			
		}


}
