package org.bfreuden.docxgen;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DocumentWriter implements Closeable {

    // the main document
    private static final String DOCUMENT_XML = "word/document.xml";
    // contains paragraph and run ids
    private static final String SETTINGS_XML = "word/settings.xml";
    // contains image ids
    private static final String DOCUMENT_XML_RELS = "word/_rels/document.xml.rels";
    // declare image types
    private static final String CONTENT_TYPE_XML = "[Content_Types].xml";

    private final File template;
    private final Random random;
    private ZipOutputStream zos;

    private Document documentXmlDom;
    private Document documentXmlRelsDom;
    private Document contentTypesXmlDom;
    private Document settingsXmlDom;
    private int nextImageId = 1;
    private int nextRelId = 1;
    private Node relationshipsNode;
    private Node typesNode;
    private String imageTemplateFragment;
    private HashSet<Integer> paragraphIds;
    private String paragraphRootId;
    private Element paragraphIdsDom;
    private Element insertPicturesBefore;

    public DocumentWriter(File template) {
        this.template = template;
        this.random = new Random();

    }

    public void partiallyRewrite(File target) throws IOException {
        this.zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target)));
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(this.template)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                if (entry.getName().equals(DOCUMENT_XML)) {
                    this.documentXmlDom = parseXML(new CloseShieldInputStream(zis));
                } else if (entry.getName().equals(DOCUMENT_XML_RELS)) {
                    this.documentXmlRelsDom = parseXML(new CloseShieldInputStream(zis));
                } else if (entry.getName().equals(CONTENT_TYPE_XML)) {
                    this.contentTypesXmlDom = parseXML(new CloseShieldInputStream(zis));
                } else if (entry.getName().equals(SETTINGS_XML)) {
                    this.settingsXmlDom = parseXML(new CloseShieldInputStream(zis));
                } else {
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    copy(zis, new CloseShieldOutputStream(zos));
                    zos.closeEntry();
                }
            }
        }
        prepareImageAppending();
    }

    public void appendImage(String filename, BufferedImage image, int targetMaxDimensionInMillimeter, float compressionQuality) throws IOException {
        if (zos == null)
            throw new IllegalStateException("doc not provided");
        String overrideImagePartName = getOverrideImagePartName();
        writeImageFileToZip(image, compressionQuality, overrideImagePartName);
        nextImageId++;
        addImageContentType(overrideImagePartName);
        String relId = addImageRelationship(overrideImagePartName);
        addImageToDocument(relId, image.getWidth(), image.getHeight(), targetMaxDimensionInMillimeter, relId);
    }

    public void finalizeDocument() throws IOException {
        if (zos == null)
            throw new IllegalStateException("doc not provided");
        zos.putNextEntry(new ZipEntry(DOCUMENT_XML));
        writeXML(documentXmlDom, zos);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry(DOCUMENT_XML_RELS));
        writeXML(documentXmlRelsDom, zos);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry(CONTENT_TYPE_XML));
        writeXML(contentTypesXmlDom, zos);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry(SETTINGS_XML));
        writeXML(settingsXmlDom, zos);
        zos.closeEntry();
    }

    @Override
    public void close() throws IOException {
        if (zos != null)
            zos.close();
    }

    private void prepareImageAppending() {
        try {
            findInsertionTarget();
            prepareParagraphRunIds();
            prepareImageIds();
            prepareImageRelId();
            prepareXmlFragments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void findInsertionTarget() throws XPathExpressionException {
        String targetEmoji = "\uD83D\uDCF7"; // 📷
        Map<String, String> ns = Map.of("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
        XPathExpression rootidXPath = newXPath("//w:p[w:r[w:t[contains(text(), '" + targetEmoji + "')]]]", ns);
        Element target = (Element) rootidXPath.evaluate(documentXmlDom, XPathConstants.NODE);
        if (target == null)
            throw new IllegalStateException("unable to find picture insertion target");
        this.insertPicturesBefore = (Element) target.getNextSibling();
        target.getParentNode().removeChild(target);
    }

    private void prepareXmlFragments() throws IOException {
        InputStream is = DocumentWriter.class.getClassLoader().getResourceAsStream("image-run-template.xml");
        this.imageTemplateFragment = toString(new CloseShieldInputStream(is), "UTF-8");
    }

    private void prepareImageIds() throws XPathExpressionException {
        Map<String, String> ns = Map.of("n", "http://schemas.openxmlformats.org/package/2006/content-types");
        // XPathExpression mediaXPath = xPath.compile("//Override[starts-with(@PartName,'/word/media')]/@PartName");
        XPathExpression mediaXPath = newXPath("//n:Override/@PartName", ns);
        NodeList nodeList = (NodeList) mediaXPath.evaluate(contentTypesXmlDom, XPathConstants.NODESET);
        Pattern imageEntryRegex = Pattern.compile("/word/media/image(\\d+).jpeg");
        // find images
        for (int i=0 ; i<nodeList.getLength() ; i++) {
            Node node = nodeList.item(i);
            Attr attr = (Attr)node;
            String value = attr.getValue();
            Matcher matcher = imageEntryRegex.matcher(value);
            if (matcher.matches()) {
                int imageId = Integer.parseInt(matcher.group(1));
                if (imageId >= this.nextImageId) {
                    this.nextImageId = imageId + 1;
                }
            }
        }
        XPathExpression typesXPath = newXPath("/n:Types", ns);
        this.typesNode = (Node) typesXPath.evaluate(contentTypesXmlDom, XPathConstants.NODE);
    }

    private void prepareImageRelId() throws XPathExpressionException {
        Map<String, String> ns = Map.of("n", "http://schemas.openxmlformats.org/package/2006/relationships");
        XPathExpression relationshipXPath = newXPath("//n:Relationship/@Id", ns);
        NodeList nodeList = (NodeList) relationshipXPath.evaluate(documentXmlRelsDom, XPathConstants.NODESET);
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
        this.relationshipsNode = (Node) relationshipsXPath.evaluate(documentXmlRelsDom, XPathConstants.NODE);
    }

    private void prepareParagraphRunIds() throws XPathExpressionException {
//        Map<String, String> ns = Map.of("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
//        XPathExpression rootidXPath = newXPath("/w:settings/w:rsids/w:rsidRoot/@w:val", ns);
//        Attr root = (Attr) rootidXPath.evaluate(settingsXmlDom, XPathConstants.NODE);
//        this.paragraphRootId = root.getValue();
//
//        XPathExpression rsidsXPath = newXPath("/w:settings/w:rsids", ns);
//        this.paragraphIdsDom =(Element) rsidsXPath.evaluate(settingsXmlDom, XPathConstants.NODE);
//
//        XPathExpression idsXPath = newXPath("/w:settings/w:rsids/w:rsid/@w:val", ns);
//        NodeList nodeList = (NodeList) idsXPath.evaluate(settingsXmlDom, XPathConstants.NODESET);
//        this.paragraphIds = new HashSet<>();
//        for (int i=0 ; i<nodeList.getLength() ; i++) {
//            Node node = nodeList.item(i);
//            Attr attr = (Attr)node;
//            String value = attr.getValue();
//            int paragraphId = Integer.parseInt(value, 16);
//            this.paragraphIds.add(paragraphId);
//        }
    }

    private String getNewParagraphId() {
        int next;
        while (paragraphIds.contains((next=random.nextInt())));
        return Integer.toString(next, 16).toUpperCase();
    }

    static XPathExpression newXPath(String expression) throws XPathExpressionException {
        return newXPath(expression, null);
    }

    static XPathExpression newXPath(String expression, Map<String, String> ns) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                return ns.get(prefix);
            }

            public Iterator<String> getPrefixes(String val) {
                return null;
            }

            public String getPrefix(String uri) {
                return null;
            }
        });
        return xPath.compile(expression);
    }

    private String getOverrideImagePartName() {
        return "word/media/image" + nextImageId + ".jpeg";
    }

    private Document parseXML(InputStream is) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private Document parseXML(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void writeXML(Document document, OutputStream os) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String toString(InputStream in, String charset) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, charset);
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[1024];
        int nb;
        while ((nb = reader.read(buf)) != -1) {
            builder.append(buf, 0, nb);
        }
        return builder.toString();
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int nb;
        while ((nb = in.read(buf)) != -1) {
            out.write(buf, 0, nb);
        }
    }

    private void addImageToDocument(String filemane, int width, int height, int targetMaxDimensionInMillimeter, String relId) {
        try {

            HashMap<String, String> replacements = new HashMap<>();
            // https://stackoverflow.com/questions/8082980/inserting-image-into-docx-using-openxml-and-setting-the-size
            // emus per mm : 36000
            int widthEmus, heightEmus;
            if (width > height) {
                widthEmus = targetMaxDimensionInMillimeter * 36000;
                heightEmus = Math.round(1.0f * widthEmus * height / width);
            } else {
                heightEmus = targetMaxDimensionInMillimeter * 36000;
                widthEmus = Math.round(1.0f * heightEmus * width / height);
            }
            replacements.put("width.emu", Integer.toString(widthEmus));
            replacements.put("height.emu", Integer.toString(heightEmus));
            replacements.put("width.px", Integer.toString(width));
            replacements.put("height.px", Integer.toString(height));
            replacements.put("filename", filemane);
            replacements.put("relId", relId);
            String imageFragment = imageTemplateFragment;
            for (Map.Entry<String, String> entry: replacements.entrySet())
                imageFragment = imageFragment.replace("[[" + entry.getKey() + "]]", entry.getValue());
            Document document = parseXML(imageFragment);
            XPathExpression drawingXPath = newXPath("/w:document/w:r", Map.of("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main"));
            Node imageRun = (Node) drawingXPath.evaluate(document, XPathConstants.NODE);
            Node paragraph = documentXmlDom.createElement("w:p");

            Node jc = documentXmlDom.createElement("w:jc");
            Attr valAtt = documentXmlDom.createAttribute("w:val");
            valAtt.setValue("center");
            Node pPr = documentXmlDom.createElement("w:pPr");
            pPr.appendChild(jc);
            ((Element)jc).setAttributeNode(valAtt);
            paragraph.appendChild(pPr);

            /*
                  <w:pPr>
        <w:jc w:val="center"/>
                </w:pPr>

             */
            Node importedImageRun = documentXmlDom.importNode(imageRun, true);
            paragraph.appendChild(importedImageRun);
            insertPicturesBefore.getParentNode().insertBefore(paragraph, insertPicturesBefore);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private void addImageContentType(String overrideImagePartName) {
        /*
            <Override PartName="/word/media/image2.jpeg" ContentType="image/jpeg" />
         */
        Element override = contentTypesXmlDom.createElementNS("http://schemas.openxmlformats.org/package/2006/content-types", "Override");
        override.setAttribute("PartName", overrideImagePartName);
        override.setAttribute("ContentType", "image/jpeg");
        typesNode.appendChild(override);
    }

    private String addImageRelationship(String overrideImagePartName) {
        /*
            <Relationship Id="rId3"
             Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
             Target="media/image2.jpeg" />
         */
        Element rel = documentXmlRelsDom.createElementNS("http://schemas.openxmlformats.org/package/2006/relationships", "Relationship");
        String relId = "rId" + nextRelId;
        rel.setAttribute("Id", relId);
        rel.setAttribute("Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
        rel.setAttribute("Target", overrideImagePartName.replace("word/", ""));
        relationshipsNode.appendChild(rel);
        nextRelId++;

        return relId;
    }

    private void writeImageFileToZip(BufferedImage image, float compressionQuality, String overrideImagePartName) throws IOException {
        zos.putNextEntry(new ZipEntry(overrideImagePartName));
        ImageWriter.writeJPG(image, new CloseShieldOutputStream(zos), compressionQuality);
        zos.closeEntry();
    }


}
