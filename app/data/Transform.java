/* Copyright 2014  hbz, Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0 */

package data;

import org.metafacture.json.JsonEncoder;
import org.metafacture.monitoring.ObjectLogger;
import org.metafacture.files.DirReader;
import org.metafacture.io.FileOpener;
import org.metafacture.io.TarReader;

/**
 * Index MAB XML clobs into an Elasticsearch instance.
 *
 * @author Fabian Steeg (fsteeg)
 * @author Pascal Christoph (dr0i)
 *
 */
public final class Transform {

	private static final String X_PATH_001 =
			"/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='001']/subfield[@code='a']";
	private static final String X_PATH_SYS =
			"/OAI-PMH/ListRecords/record/metadata/record/controlfield[@tag='SYS']";

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
		idExtractor.setXPathToId(X_PATH_001);
		idExtractor.setXPathToSysId(X_PATH_SYS);
		idExtractor.setIdFieldName("hbzId");
		idExtractor.setIdSysFieldName("alephInternalSysnumber");
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
