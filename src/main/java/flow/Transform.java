/* Copyright 2014 hbz, Fabian Steeg. Licensed under the Eclipse Public License 1.0 */

package flow;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.converter.JsonToElasticsearchBulk;
import org.culturegraph.mf.stream.reader.TarReader;
import org.culturegraph.mf.stream.sink.ObjectWriter;
import org.culturegraph.mf.stream.source.DirReader;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.util.FileCompression;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

	/**
	 * Extract ID from given XML, emit ID and full XML as literals.
	 * 
	 * @author Fabian Steeg (fsteeg)
	 */
	private static class IdExtractor extends
			DefaultObjectPipe<Reader, StreamReceiver> {

		private XPathExpression expression;

		/**
		 * @param xPath The xPath expression to select the record ID
		 */
		public IdExtractor(String xPath) {
			try {
				expression = XPathFactory.newInstance().newXPath().compile(xPath);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void process(Reader reader) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try (Scanner scanner = new Scanner(reader)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(new StringReader(line)));
					String hbzId = expression.evaluate(doc);
					getReceiver().startRecord(hbzId);
					getReceiver().literal("hbzId", hbzId);
					getReceiver().literal("mabXml", line);
					getReceiver().endRecord();
				}
			} catch (IOException | XPathExpressionException
					| ParserConfigurationException | SAXException e) {
				e.printStackTrace();
			}
		}
	}

}
