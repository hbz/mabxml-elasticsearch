/* Copyright 2014 hbz, Fabian Steeg. Licensed under the Eclipse Public License 1.0 */

package flow;

import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.converter.JsonToElasticsearchBulk;
import org.culturegraph.mf.stream.reader.TarReader;
import org.culturegraph.mf.stream.sink.ObjectWriter;
import org.culturegraph.mf.stream.source.DirReader;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.util.FileCompression;

import pipe.IdExtractor;

/**
 * Transform MAB-XML to JSON for Elasticsearch bulk import.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public class Transform {

	private static final String IN = "src/main/resources/input/";
	private static final String OUT =
			"src/main/resources/output/es-bulk.out.json";
	private static final String X_PATH =
			"/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='001']/subfield[@code='a']";

	/** @param args Not used */
	public static void main(String... args) {
		DirReader readDir = new DirReader();
		readDir.setRecursive(false);
		readDir.setFilenamePattern(".*\\.tar.bz2");
		FileOpener openFile = new FileOpener();
		openFile.setCompression(FileCompression.BZIP2);
		//@formatter:off
		readDir
				.setReceiver(openFile)
				.setReceiver(new TarReader())
				.setReceiver(new IdExtractor(X_PATH))
				.setReceiver(new JsonEncoder())
				.setReceiver(new JsonToElasticsearchBulk("hbzId", "mabxml", "hbz01"))
				.setReceiver(new ObjectWriter<>(OUT));
		//@formatter:on
		process(readDir);
	}

	static void process(DirReader dirReader) {
		dirReader.process(IN);
		dirReader.closeStream();
	}

}
