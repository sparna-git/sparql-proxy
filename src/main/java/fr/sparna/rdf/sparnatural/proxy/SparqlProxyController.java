package fr.sparna.rdf.sparnatural.proxy;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SparqlProxyController {

	
	@RequestMapping(
			value = {"sparql"},
			params={"query", "endpoint"}
	)
	public void sparql(
			@RequestParam(value="query", required=true) String query,
			@RequestParam(value="endpoint", required=true) String endpoint,
			@RequestParam(value="method", required=false) String method,
			@RequestParam(value="format", required=false) String format,
			@RequestHeader(value="Accept", required=false) String accept,
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException {
		
		SPARQLRepository sparqlRepository = new SPARQLRepository(endpoint);
		RepositoryConnection connection = sparqlRepository.getConnection();
		
		SPARQLParser parser = new SPARQLParser();
		ParsedQuery pq = parser.parseQuery(query, RDF.NAMESPACE);		
		
		// always set CORS
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		if(pq instanceof ParsedTupleQuery) {
			TupleQuery q = connection.prepareTupleQuery(query);
			
			TupleQueryResultHandler handler;
			if(format != null) {
				switch (format) {
				case "json" : {
					response.setContentType("application/sparql-results+json");
					handler = new SPARQLResultsJSONWriter(response.getOutputStream());
				}
				default : {
					response.setContentType("application/sparql-results+xml");
					handler = new SPARQLResultsXMLWriter(response.getOutputStream());
				}
				}
			} else {
				switch (accept) {
				case "application/json" :
				case "application/sparql-results+json" : {
					response.setContentType("application/sparql-results+json");
					handler = new SPARQLResultsJSONWriter(response.getOutputStream());
				}
				case "text/csv" : {
					response.setContentType("text/csv");
					handler = new SPARQLResultsCSVWriter(response.getOutputStream());
				}
				case "text/tab-separated-values" : {
					response.setContentType("text/tab-separated-values");
					handler = new SPARQLResultsTSVWriter(response.getOutputStream());
				}
				default : {
					response.setContentType("application/sparql-results+xml");
					handler = new SPARQLResultsXMLWriter(response.getOutputStream());
				}
				}	
			}
		
			
			q.evaluate(handler);
		} else if(pq instanceof ParsedGraphQuery) {
			GraphQuery q = connection.prepareGraphQuery(query);
			
			RDFWriter handler;
			switch (accept) {
			case "text/turtle" : {
				response.setContentType("text/turtle");
				handler = new TurtleWriter(response.getOutputStream());
			}
			default : {
				response.setContentType("application/rdf+xml");
				handler = new RDFXMLWriter(response.getOutputStream());
			}
			}	
			
			q.evaluate(handler);
			
		} else if(pq instanceof ParsedBooleanQuery) {
			BooleanQuery q = connection.prepareBooleanQuery(query);
			boolean b = q.evaluate();
			response.getOutputStream().write(Boolean.toString(b).getBytes());
		}
	}
	
	
}
