package controllers;

import java.io.File;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;

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
	private static final String MABXML_ELASTICSEARCH = "mabxml-elasticsearch";
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
		
		play.Logger.info("Calling elasticsearch:: index: {}, type: {}, id: {}", ES_INDEX, ES_TYPE, id);
		
		Client client = NodeBuilder.nodeBuilder().node().client();

		GetResponse response = client.prepareGet(ES_INDEX, ES_TYPE, id)
		        .execute()
		        .actionGet();
		Object object = response.getSource().get(GET_KEY);
		
		response().setContentType("text/xml");
		play.Logger.info(object.toString());
		
		return Promise.promise(() -> ok(object.toString()));
	}
}
	