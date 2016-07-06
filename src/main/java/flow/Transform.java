/* Copyright 2014  hbz, Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0 */

package flow;

import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.pipe.ObjectLogger;
import org.culturegraph.mf.stream.source.DirReader;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.stream.source.TarReader;

import pipe.ElasticsearchIndexer;
import pipe.IdExtractor;

/**
 * Index MAB XML clobs into an Elasticsearch instance.
 *
 * @author Fabian Steeg (fsteeg)
 * @author Pascal Christoph (dr0i)
 *
 */
public final class Transform {

	private static final String X_PATH =
			"/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='001']/subfield[@code='a']";

	private static final String DIR = "/files/open_data/open/DE-605/mabxml/";
	private static final String SUFFIX = "gz";
	private static final String CLUSTER = "quaoar1";
	private static final String HOSTNAME = "193.30.112.170";
	private static final String INDEX = "hbz01-test";

	@SuppressWarnings("javadoc")
	public static void main(String... args) {
		String dir = args.length > 0 ? args[0] : DIR;
		String suffix = args.length > 1 ? args[1] : SUFFIX;
		String cluster = args.length > 2 ? args[2] : CLUSTER;
		String hostname = args.length > 3 ? args[3] : HOSTNAME;
		String index = args.length > 4 ? args[4] : INDEX;
		FileOpener openFile = new FileOpener();
		DirReader dirReader = new DirReader();
		dirReader.setFilenamePattern(".*tar." + suffix);
		ElasticsearchIndexer elasticsearchIndexer =
				getElasticsearchIndexer(cluster, hostname, index);
		dirReader//
				.setReceiver(new ObjectLogger<String>("Directory reader: "))//
				.setReceiver(openFile)//
				.setReceiver(new TarReader())//
				.setReceiver(getIdExtractor())//
				.setReceiver(new JsonEncoder())//
				.setReceiver(elasticsearchIndexer);
		dirReader.process(dir);
		elasticsearchIndexer.closeStream();
		dirReader.closeStream();
	}

	private static IdExtractor getIdExtractor() {
		IdExtractor idExtractor = new IdExtractor();
		idExtractor.setXPathToId(X_PATH);
		idExtractor.setIdFieldName("hbzId");
		idExtractor.setFullXmlFieldName("mabXml");
		return idExtractor;
	}

	private static ElasticsearchIndexer getElasticsearchIndexer(
			final String cluster, final String hostName, final String index) {
		ElasticsearchIndexer esIndexer = new ElasticsearchIndexer();
		esIndexer.setClusterName(cluster);
		esIndexer.setHostName(hostName);
		esIndexer.setIndexName(index);
		esIndexer.setIdKey("hbzId");
		esIndexer.setIndexType("mabxml");
		esIndexer.onSetReceiver();
		return esIndexer;
	}

}
