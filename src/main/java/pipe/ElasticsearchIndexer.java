package pipe;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Index JSON into elasticsearch.
 *
 * @author Pascal Christoph (dr0i)
 * @author Fabian Steeg (fsteeg)
 */
@In(String.class)
@Out(Void.class)
public class ElasticsearchIndexer extends
		DefaultObjectPipe<String, ObjectReceiver<Void>> {
	private static void setIndexRefreshInterval(final Client client,
			final Object setting) {
		client.admin().indices()
				.prepareUpdateSettings(ElasticsearchIndexer.indexname)
				.setSettings(ImmutableMap.of("index.refresh_interval", setting))
				.execute().actionGet();
	}

	private static final Logger LOG = LoggerFactory
			.getLogger(ElasticsearchIndexer.class);
	private String hostname;
	private String clustername;

	private static String indexname;
	private final ObjectMapper mapper = new ObjectMapper();
	private String idKey;

	private IndexRequestBuilder indexRequest;
	private static Builder CLIENT_SETTINGS;
	private static InetSocketTransportAddress NODE;
	private static String indextype;
	private TransportClient tc;

	private Client client;

	private void index(final Map<String, Object> json, final String id) {
		int retries = 40;
		while (retries > 0) {
			try {
				this.indexRequest.setId(id).setSource(json).execute();
				break; // stop retry-while
			} catch (final NoNodeAvailableException e) {
				retries--;
				try {
					Thread.sleep(10000);
				} catch (final InterruptedException x) {
					x.printStackTrace();
				}
				System.err.printf("Retry indexing record %s: %s (%s more retries)\n",
						id, e.getMessage(), retries);
			}
		}
	}

	@Override
	protected void onCloseStream() {
		ElasticsearchIndexer.setIndexRefreshInterval(this.client, "1");
	}

	@Override
	public void onSetReceiver() {
		ElasticsearchIndexer.CLIENT_SETTINGS =
				ImmutableSettings.settingsBuilder().put("cluster.name",
						this.clustername);
		ElasticsearchIndexer.NODE =
				new InetSocketTransportAddress(this.hostname, 9300);
		this.tc =
				new TransportClient(ElasticsearchIndexer.CLIENT_SETTINGS
						.put("client.transport.sniff", false)
						.put("client.transport.ping_timeout", 20, TimeUnit.SECONDS).build());
		this.client = this.tc.addTransportAddress(ElasticsearchIndexer.NODE);
		ElasticsearchIndexer.setIndexRefreshInterval(this.client, "-1");
		this.indexRequest =
				this.client.prepareIndex(ElasticsearchIndexer.indexname,
						ElasticsearchIndexer.indextype);
	}

	@Override
	public void process(final String obj) {
		if (this.hostname == null || this.clustername == null
				|| ElasticsearchIndexer.indexname == null) {
			ElasticsearchIndexer.LOG
					.error("Pass 3 params: <hostname> <clustername> <indexname>");
			return;
		}

		try {
			final Map<String, Object> json = this.mapper.readValue(obj, Map.class);
			final String id = (String) json.get(this.idKey);
			index(json, id);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the elasticsearch cluster name.
	 *
	 * @param clustername the name of the cluster
	 */
	public void setClustername(final String clustername) {
		this.clustername = clustername;
	}

	/**
	 * Sets the elasticsearch hostname
	 *
	 * @param hostname may be an IP or a domain name
	 */
	public void setHostname(final String hostname) {
		this.hostname = hostname;
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
	 * @param indexname the name of the index
	 */
	public void setIndexname(final String indexname) {
		ElasticsearchIndexer.indexname = indexname;

	}

	/**
	 * Sets the name of the elasticsearch index type .
	 *
	 * @param indextype the name of the type of the index
	 */
	public void setIndextype(final String indextype) {
		ElasticsearchIndexer.indextype = indextype;

	}

}
