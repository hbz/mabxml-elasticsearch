/* Copyright 2014 hbz, Fabian Steeg. Licensed under the Eclipse Public License 1.0 */

package pipe;

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
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Extracts ID from given XML, emit ID and full XML as literals.
 *
 * @author Fabian Steeg (fsteeg)
 * @author Pascal Christoph (dr0i)
 */
@In(Reader.class)
@Out(StreamReceiver.class)
public class IdExtractor extends DefaultObjectPipe<Reader, StreamReceiver> {

	private static String parseXml(final String line) {
		try {
			final DocumentBuilder builder = IdExtractor.factory.newDocumentBuilder();
			final Document doc =
					builder.parse(new InputSource(new StringReader(line)));
			return IdExtractor.expression.evaluate(doc);
		} catch (XPathExpressionException | ParserConfigurationException
				| SAXException | IOException e) {
			IdExtractor.LOG.warn("Could not parse: " + line);
			e.printStackTrace();
		}
		return null;
	}

	private static final Logger LOG = LoggerFactory.getLogger(IdExtractor.class);
	private static XPathExpression expression;
	private static DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();
	private static String idFieldName;

	private static String fullXmlFieldName;

	@Override
	public void process(final Reader reader) {
		try (Scanner scanner = new Scanner(reader)) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final String id = IdExtractor.parseXml(line);
				if (id != null) {
					getReceiver().startRecord(id);
					getReceiver().literal(IdExtractor.idFieldName, id);
					getReceiver().literal(IdExtractor.fullXmlFieldName, line);
					getReceiver().endRecord();
				}
			}
		}
	}

	/**
	 * Sets the name of the ID field which is emitted as key.
	 *
	 * @param fullXmlFieldName the name of the ID field which is emitted as key
	 */
	public void setFullXmlFieldName(final String fullXmlFieldName) {
		IdExtractor.fullXmlFieldName = fullXmlFieldName;
	}

	/**
	 * Sets the name of the ID field which is emitted as key.
	 *
	 * @param idFieldName the name of the ID field which is emitted as key
	 */
	public void setIdFieldName(final String idFieldName) {
		IdExtractor.idFieldName = idFieldName;
	}

	/**
	 * Sets the xPath to identify the ID in the record.
	 *
	 * @param xPath The xPath expression to select the record ID
	 */
	public void setXPathToId(final String xPath) {
		try {
			IdExtractor.expression =
					XPathFactory.newInstance().newXPath().compile(xPath);
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}
}