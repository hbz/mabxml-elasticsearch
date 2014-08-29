/* Copyright 2014 hbz, Fabian Steeg. Licensed under the Eclipse Public License 1.0 */

package flow;

import java.util.concurrent.TimeUnit;

import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.pipe.ObjectLogger;
import org.culturegraph.mf.stream.reader.TarReader;
import org.culturegraph.mf.stream.source.DirReader;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.util.FileCompression;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import pipe.ElasticsearchIndexer;
import pipe.IdExtractor;

/**
 * Transform MAB-XML to JSON and index in Elasticsearch.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public class Transform {

	private static final String IN = "src/main/resources/input/";
	private static final String PATTERN = ".*\\.tar.bz2";
	private static final FileCompression COMPRESSION = FileCompression.BZIP2;
	private static final String INDEX = "hbz01-test";
	private static final Builder CLIENT_SETTINGS = ImmutableSettings
			.settingsBuilder().put("cluster.name", "lobid-wan");
	private static final InetSocketTransportAddress NODE_1 =
			new InetSocketTransportAddress("193.30.112.171", 9300);
	private static final InetSocketTransportAddress NODE_2 =
			new InetSocketTransportAddress("193.30.112.172", 9300);

	private static final String X_PATH =
			"/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='001']/subfield[@code='a']";

	/** @param args Not used */
	public static void main(String... args) {
		DirReader readDir = new DirReader();
		readDir.setRecursive(false);
		readDir.setFilenamePattern(PATTERN);
		FileOpener openFile = new FileOpener();
		openFile.setCompression(COMPRESSION);
		//@formatter:off
		try (TransportClient tc = new TransportClient(CLIENT_SETTINGS
				.put("client.transport.sniff", false)
				.put("client.transport.ping_timeout", 20, TimeUnit.SECONDS).build());
				Client client = tc.addTransportAddress(NODE_1).addTransportAddress(NODE_2)) {
			setIndexRefreshInterval(client, "-1");
			readDir
					.setReceiver(new ObjectLogger<String>("Directory reader: "))
					.setReceiver(openFile)
					.setReceiver(new TarReader())
					.setReceiver(new IdExtractor(X_PATH))
					.setReceiver(new JsonEncoder())
					.setReceiver(new ElasticsearchIndexer("hbzId", client.prepareIndex(INDEX, "mabxml")));
			//@formatter:on
			process(readDir);
			setIndexRefreshInterval(client, "1");
		}
	}

	static void process(DirReader dirReader) {
		dirReader.process(IN);
		dirReader.closeStream();
	}

	private static void setIndexRefreshInterval(Client client, String setting) {
		client.admin().indices().prepareUpdateSettings(INDEX)
				.setSettings(ImmutableMap.of("index.refresh_interval", setting))
				.execute().actionGet();
	}
}
