package org.bfreuden.docxgen;

import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bfreuden.docxgen.DocumentWriter.newXPath;

public class DocumentRels {

    static final String DOCUMENT_XML_RELS = "word/_rels/document.xml.rels";
    private Document document;
    private int nextRelId = 1;
    private Node relationshipsNode;

    public DocumentRels(Document document) {
        this.document = document;
    }

    public void init() throws XPathExpressionException {
        Map<String, String> ns = Map.of("n", "http://schemas.openxmlformats.org/package/2006/relationships");
        XPathExpression relationshipXPath = newXPath("//n:Relationship/@Id", ns);
        NodeList nodeList = (NodeList) relationshipXPath.evaluate(document, XPathConstants.NODESET);
        Pattern ridRegex = Pattern.compile("rId(\\d+)");
        for (int i=0 ; i<nodeList.getLength() ; i++) {
            Node node = nodeList.item(i);
            Attr attr = (Attr)node;
            String value = attr.getValue();
            Matcher matcher = ridRegex.matcher(value);
            if (!matcher.matches())
                throw new IllegalStateException("unable to match rel id");
            int relId = Integer.parseInt(matcher.group(1));
            if (relId >= this.nextRelId) {
                this.nextRelId = relId + 1;
            }
        }
        XPathExpression relationshipsXPath = newXPath("/n:Relationships", ns);
        this.relationshipsNode = (Node) relationshipsXPath.evaluate(document, XPathConstants.NODE);
    }


    private String addImageRelationship(String overrideImagePartName) {
        /*
            <Relationship Id="rId3"
             Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
             Target="media/image2.jpeg" />
         */
        Element rel = document.createElementNS("http://schemas.openxmlformats.org/package/2006/relationships", "Relationship");
        String relId = "rId" + nextRelId;
        rel.setAttribute("Id", relId);
        rel.setAttribute("Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
        rel.setAttribute("Target", overrideImagePartName.replace("/word/", ""));
        relationshipsNode.appendChild(rel);
        nextRelId++;

        return relId;
    }

}
