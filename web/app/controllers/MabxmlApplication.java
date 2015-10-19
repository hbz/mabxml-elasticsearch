package controllers;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import play.libs.F.Promise;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

/**
 * 
 * Contains the methods of the play application to search, get source, and
 * display context
 *
 */
public class MabxmlApplication extends Controller {

	public static final Config CONFIG = ConfigFactory.parseFile(
			new File("conf/application.conf")).resolve();
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
	 * @return The source of a document as JSON
	 */
	public static Promise<Result> get(String id) {
		String url =
				String.format("%s/%s/%s/%s/" + _SOURCE, ES_SERVER, ES_INDEX, ES_TYPE, id);
		response().setContentType("text/xml");
		play.Logger.info("Calling URL: " + url);
		return WS.url(url).execute().map(x -> ok(x.asJson().get(GET_KEY).asText()));
	}
}
	