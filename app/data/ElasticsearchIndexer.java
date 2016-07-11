package data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

/**
 * Index JSON into elasticsearch.
 *
 * @author Pascal Christoph (dr0i)
 * @author Fabian Steeg (fsteeg)
 */
@In(String.class)
@Out(Void.class)
public class ElasticsearchIndexer
		extends DefaultObjectPipe<String, ObjectReceiver<Void>> {

	private static final Logger LOG =
			LoggerFactory.getLogger(ElasticsearchIndexer.class);
	private final ObjectMapper mapper = new ObjectMapper();

	private String hostName;
	private String clusterName;
	private String indexName;
	private String idKey;
	private String indexType;

	private TransportClient tc;
	private Client client;
	private BulkRequestBuilder bulkRequest;
	private int pendingIndexRequests;

	private void setIndexRefreshInterval(final Client client,
			final Object setting) {
		client.admin().indices().prepareUpdateSettings(indexName)
				.setSettings(ImmutableMap.of("index.refresh_interval", setting))
				.execute().actionGet();
	}

	private static String config() {
		String res = null;
		try {
			final InputStream config = new FileInputStream("conf/index-config.json");
			try (InputStreamReader reader = new InputStreamReader(config, "UTF-8")) {
				res = Streams.copyToString(reader);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return res;
	}

	private void index(final Map<String, Object> json, final String id) {
		if (id.isEmpty()) {
			return;
		}
		bulkRequest
				.add(client.prepareIndex(indexName, indexType, id).setSource(json));
		pendingIndexRequests++;
		if (pendingIndexRequests == 1000) {
			executeBulk();
			bulkRequest = client.prepareBulk();
			pendingIndexRequests = 0;
		}
	}

	private void executeBulk() {
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		if (bulkResponse.hasFailures()) {
			bulkResponse.forEach(item -> {
				if (item.isFailed()) {
					LOG.error("Indexing {} failed: {}", item.getId(),
							item.getFailureMessage());
				}
			});
		}
	}

	@Override
	protected void onCloseStream() {
		if (pendingIndexRequests > 0) {
			executeBulk();
		}
		setIndexRefreshInterval(client, "1s");
		client.close();
	}

	@Override
	public void onSetReceiver() {
		Settings.Builder clientSettings = Settings.settingsBuilder()//
				.put("cluster.name", clusterName)//
				.put("client.transport.sniff", false)//
				.put("client.transport.ping_timeout", 20, TimeUnit.SECONDS);
		tc = TransportClient.builder().settings(clientSettings).build();
		client = tc.addTransportAddress(
				new InetSocketTransportAddress(new InetSocketAddress(hostName, 9300)));
		final IndicesAdminClient admin = client.admin().indices();
		if (!admin.prepareExists(indexName).execute().actionGet().isExists()) {
			admin.prepareCreate(indexName).setSource(config()).execute().actionGet();
		}
		setIndexRefreshInterval(client, "-1");
		bulkRequest = client.prepareBulk();
		pendingIndexRequests = 0;
	}

	@Override
	public void process(final String obj) {
		if (hostName == null || clusterName == null || indexName == null
				|| indexType == null || idKey == null) {
			ElasticsearchIndexer.LOG.error(
					"Set params: <host name> <cluster name> <index name> <index type> <id key>");
			return;
		}
		try {
			final Map<String, Object> json = mapper.readValue(obj, Map.class);
			final String id = (String) json.get(idKey);
			index(json, id);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the elasticsearch cluster name.
	 *
	 * @param clusterName the name of the cluster
	 */
	public void setClusterName(final String clusterName) {
		this.clusterName = clusterName;
	}

	/**
	 * Sets the elasticsearch host name
	 *
	 * @param hostName may be an IP or a domain name
	 */
	public void setHostName(final String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Sets the key of the JSON value to be used as the ID for the record
	 *
	 * @param idKey the key
	 */
	public void setIdKey(final String idKey) {
		this.idKey = idKey;
	}

	/**
	 * Sets the elasticsearch index name.
	 *
	 * @param indexName the name of the index
	 */
	public void setIndexName(final String indexName) {
		this.indexName = indexName;

	}

	/**
	 * Sets the name of the elasticsearch index type .
	 *
	 * @param indexType the name of the type of the index
	 */
	public void setIndexType(final String indexType) {
		this.indexType = indexType;
	}

}
