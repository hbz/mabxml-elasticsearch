/* Copyright 2014 hbz, Fabian Steeg. Licensed under the Eclipse Public License 1.0 */

package pipe;

import java.io.IOException;
import java.util.Map;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.NoNodeAvailableException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Index JSON in Elasticsearch.
 * 
 * @author Fabian Steeg (fsteeg)
 */
@In(String.class)
@Out(Void.class)
public class ElasticsearchIndexer extends
		DefaultObjectPipe<String, ObjectReceiver<Void>> {

	private ObjectMapper mapper = new ObjectMapper();
	private String idKey;
	private IndexRequestBuilder indexRequest;

	/**
	 * @param idKey The key of the JSON value to be used as the ID for the record
	 * @param indexRequest The index request with a specified index name and type
	 */
	public ElasticsearchIndexer(String idKey, IndexRequestBuilder indexRequest) {
		this.idKey = idKey;
		this.indexRequest = indexRequest;
	}

	@Override
	public void process(String obj) {
		try {
			Map<String, Object> json = mapper.readValue(obj, Map.class);
			String id = (String) json.get(idKey);
			index(json, id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void index(Map<String, Object> json, String id) {
		int retries = 40;
		while (retries > 0) {
			try {
				indexRequest.setId(id).setSource(json).execute();
				break; // stop retry-while
			} catch (NoNodeAvailableException e) {
				retries--;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException x) {
					x.printStackTrace();
				}
				System.err.printf("Retry indexing record %s: %s (%s more retries)\n",
						id, e.getMessage(), retries);
			}
		}
	}
}
