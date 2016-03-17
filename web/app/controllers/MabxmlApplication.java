package controllers;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

/**
 * 
 * Contains the methods of the play application to search, get source, and
 * display context
 *
 */
public class MabxmlApplication extends Controller {

	public static final Config CONFIG =
			ConfigFactory.parseFile(new File("conf/application.conf")).resolve();
	private static final String _SOURCE = "_source";
	private static final String MABXML_ELASTICSEARCH = "mabxml-elasticsearch";
	private static final String SERVER = CONFIG.getString("es.server");
	private static final String PORT = ":9200";
	private static final String ES_SERVER = "http://" + SERVER + PORT;
	private static final String ES_INDEX = CONFIG.getString("es.index");
	private static final String ES_TYPE = "mabxml";
	private static final String GET_KEY = "mabXml";

	public static Result index() {
		return ok(index.render(MABXML_ELASTICSEARCH));
	}

	/**
	 * @param id The id of a document in the Elasticsearch index
	 * @return The source of a document as XML, if it was found
	 */
	public static Promise<Result> get(String id) {
		String url = String.format("%s/%s/%s/%s/" + _SOURCE, ES_SERVER, ES_INDEX,
				ES_TYPE, id);
		play.Logger.info("Calling URL: " + url);
		return WS.url(url).execute().map(toResult(id));
	}

	private static Function<WSResponse, Result> toResult(String id) {
		return response -> response.getStatus() == Http.Status.OK
				? ok(response.asJson().get(GET_KEY).asText())
						.as("text/xml; charset: utf-8")
				: status(response.getStatus(), String.format("GET %s: %s\n%s", id,
						response.getStatusText(), response.getBody()));
	}
}
