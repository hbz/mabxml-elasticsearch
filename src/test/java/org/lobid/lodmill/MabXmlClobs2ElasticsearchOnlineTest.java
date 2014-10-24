/* Copyright 2014  hbz, Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0 */
package org.lobid.lodmill;

import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.pipe.ElasticsearchIndexer;
import org.culturegraph.mf.stream.pipe.IdExtractor;
import org.culturegraph.mf.stream.pipe.ObjectLogger;
import org.culturegraph.mf.stream.source.DirReader;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.util.FileCompression;
import org.junit.Test;

/**
 * Gets tar bz2 archives of MAB XML clobs and index their records into an
 * elasticsearch instance. Acts as online test and can also be executed on
 * command line.
 * 
 * @author Pascal Christoph (dr0i)
 * 
 */
@SuppressWarnings("javadoc")
public final class MabXmlClobs2ElasticsearchOnlineTest {

	private static final String X_PATH =
			"/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='001']/subfield[@code='a']";
	private static final FileCompression COMPRESSION = FileCompression.BZIP2;

	public static void main(String... args) {
		MabXmlClobs2ElasticsearchOnlineTest mabXmlClobs2Elasticsearch =
				new MabXmlClobs2ElasticsearchOnlineTest();
		mabXmlClobs2Elasticsearch.testFlow();
	}

	@SuppressWarnings("static-method")
	@Test
	public void testFlow() {
		// hbz catalog transformation
		FileOpener openFile = new FileOpener();
		openFile.setCompression(COMPRESSION);
		DirReader dirReader = new DirReader();
		ElasticsearchIndexer elasticsearchIndexer = getElasticsearchIndexer();
		dirReader.setReceiver(new ObjectLogger<String>("Directory reader: "))
				.setReceiver(openFile).setReceiver(new TarReader())
				.setReceiver(getIdExtractor()).setReceiver(new JsonEncoder())
				.setReceiver(elasticsearchIndexer);
		dirReader
				.process("/files/open_data/closed/hbzvk/index.hbz-nrw.de/alephxml/clobs/updates_test");
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

	private static ElasticsearchIndexer getElasticsearchIndexer() {
		ElasticsearchIndexer esIndexer = new ElasticsearchIndexer();
		esIndexer.setClustername("quaoar");
		esIndexer.setHostname("193.30.112.171");
		esIndexer.setIndexname("hbz01");
		esIndexer.setIdKey("hbzId");
		esIndexer.onSetReceiver();
		return esIndexer;
	}

}
