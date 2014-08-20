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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Extract ID from given XML, emit ID and full XML as literals.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public class IdExtractor extends DefaultObjectPipe<Reader, StreamReceiver> {

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