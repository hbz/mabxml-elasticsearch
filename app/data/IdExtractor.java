/* Copyright 2014 hbz, Fabian Steeg. Licensed under the Eclipse Public License 1.0 */

package data;

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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import play.Logger;

/**
 * Extracts ID from given XML, emit ID and full XML as literals.
 *
 * @author Fabian Steeg (fsteeg)
 * @author Pascal Christoph (dr0i)
 */
@In(Reader.class)
@Out(StreamReceiver.class)
public class IdExtractor extends DefaultObjectPipe<Reader, StreamReceiver> {

	private static String[] parseXml(final String line) {
		try {
			final DocumentBuilder builder = IdExtractor.factory.newDocumentBuilder();
			final Document doc =
					builder.parse(new InputSource(new StringReader(line)));
			String[] ids = new String[2];
			ids[0] = IdExtractor.expressionId.evaluate(doc);
			ids[1] = IdExtractor.expressionSysId.evaluate(doc);
			return ids;
		} catch (XPathExpressionException | ParserConfigurationException
				| SAXException | IOException e) {
			Logger.warn("Could not parse: " + line);
			e.printStackTrace();
		}
		return null;
	}

	private static XPathExpression expressionId;
	private static XPathExpression expressionSysId;
	private static DocumentBuilderFactory factory =
			DocumentBuilderFactory.newInstance();
	private static String idFieldName;
	private static String idSysFieldName;
	private static String fullXmlFieldName;

	@Override
	public void process(final Reader reader) {
		try (Scanner scanner = new Scanner(reader)) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				String ids[] = IdExtractor.parseXml(line);
				if (ids != null) {
					final String id = ids[0];
					final String idSys = ids[1];
					Logger.info("Id: " + id + ", Sys:" + idSys);
					if (id != null && idSys != null) {
						getReceiver().startRecord(id);
						getReceiver().literal(IdExtractor.idFieldName, id);
						getReceiver().literal(IdExtractor.idSysFieldName, idSys);
						getReceiver().literal(IdExtractor.fullXmlFieldName, line);
						getReceiver().endRecord();
					} else {
						Logger.warn("No ID found in " + line);
					}
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
	 * Sets the name of the alpeh internal sys number ID field which is emitted as
	 * key.
	 * 
	 * @param idSysFieldName the name of the aleph internal SYS ID field which is
	 *          emitted as key
	 */
	public void setIdSysFieldName(String idSysFieldName) {
		IdExtractor.idSysFieldName = idSysFieldName;
	}

	/**
	 * Sets the xPath to identify the ID in the record.
	 *
	 * @param xPath The xPath expression to select the record ID
	 */
	public void setXPathToId(final String xPath) {
		try {
			IdExtractor.expressionId =
					XPathFactory.newInstance().newXPath().compile(xPath);
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the xPath to identify the aleph internal sys ID in the record.
	 *
	 * @param xPath The xPath expression to select the record's aleph internal sys
	 *          ID
	 */
	public void setXPathToSysId(String xPath) {
		try {
			IdExtractor.expressionSysId =
					XPathFactory.newInstance().newXPath().compile(xPath);
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
