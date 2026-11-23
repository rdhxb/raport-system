package rdhxb.financeservice.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import rdhxb.financeservice.dto.SprawozdanieDto.Kwota;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class XmlUtils {

    private XmlUtils() {
    }

    public static boolean exists(Document doc, String localName) {
        return doc.getElementsByTagNameNS("*", localName).getLength() > 0;
    }

    // Text of the first element with the given local name, in any namespace; null if missing
    public static String getText(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }

    // Attribute of the first element with the given local name; null if element or attribute is missing
    public static String getAttribute(Document doc, String localName, String attribute) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        String value = ((Element) nodes.item(0)).getAttribute(attribute);
        return value.isEmpty() ? null : value;
    }

    // First descendant of parent with the given local name, in any namespace; null if missing
    public static Element findElement(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    // Direct child of parent with the given local name; null if missing
    public static Element getChild(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE && localName.equals(child.getLocalName())) {
                return (Element) child;
            }
        }
        return null;
    }

    // KwotaA = current year, KwotaB = previous year
    public static Kwota getKwota(Element pozycja) {
        if (pozycja == null) {
            return null;
        }
        return new Kwota(
                toBigDecimal(getChild(pozycja, "KwotaA")),
                toBigDecimal(getChild(pozycja, "KwotaB"))
        );
    }

    public static BigDecimal toBigDecimal(Element element) {
        return element == null ? null : new BigDecimal(element.getTextContent().trim());
    }

    // xs:boolean allows both "1"/"0" and "true"/"false"
    public static boolean isTrue(String value) {
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    public static LocalDate toDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}
