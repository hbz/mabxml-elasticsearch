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
 * Gets tar bz2 archives of MAB XML clobs and index their records into an
 * Elasticsearch instance. Acts as online test and can also be executed on
 * command line.
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
	private static final String CLUSTER = "quaoar";
	private static final String HOSTNAME = "193.30.112.171";

	@SuppressWarnings("javadoc")
	public static void main(String... args) {
		// hbz catalog transformation
		FileOpener openFile = new FileOpener();
		DirReader dirReader = new DirReader();
		dirReader.setFilenamePattern(
				args.length == 4 ? ".*tar." + args[1] : ".*tar." + SUFFIX);
		ElasticsearchIndexer elasticsearchIndexer =
				getElasticsearchIndexer(args.length == 4 ? args[2] : CLUSTER,
						args.length == 4 ? args[3] : HOSTNAME);
		dirReader.setReceiver(new ObjectLogger<String>("Directory reader: "))
				.setReceiver(openFile).setReceiver(new TarReader())
				.setReceiver(getIdExtractor()).setReceiver(new JsonEncoder())
				.setReceiver(elasticsearchIndexer);
		dirReader.process(args.length == 4 ? args[0] : DIR);
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
			final String cluster, final String hostname) {
		ElasticsearchIndexer esIndexer = new ElasticsearchIndexer();
		esIndexer.setClustername(cluster);
		esIndexer.setHostname(hostname);
		esIndexer.setIndexname("hbz01");
		esIndexer.setIdKey("hbzId");
		esIndexer.setIndextype("mabxml");
		esIndexer.onSetReceiver();
		return esIndexer;
	}

}
