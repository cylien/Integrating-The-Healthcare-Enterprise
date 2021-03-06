package edu.tcu.gaduo.ihe.iti.xds_transaction.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import edu.tcu.gaduo.ihe.utility.AxiomUtil;
import edu.tcu.gaduo.ihe.utility.PnRCommon;
import edu.tcu.gaduo.ihe.utility._interface.IAxiomUtil;

import edu.tcu.gaduo.ihe.constants.DocumentRelationshipsConstants;
import edu.tcu.gaduo.ihe.constants.EbXML;
import edu.tcu.gaduo.ihe.constants.Namespace;
import edu.tcu.gaduo.ihe.iti.xds_transaction.dao.DocumentRelationships;
import edu.tcu.gaduo.ihe.iti.xds_transaction.dao.XDSDocumentEntry;
import edu.tcu.gaduo.ihe.iti.xds_transaction.dao.XDSEntry;
import edu.tcu.gaduo.ihe.iti.xds_transaction.dao.XDSFolder;
import edu.tcu.gaduo.ihe.iti.xds_transaction.dao.XDSSubmissionSet;
import edu.tcu.gaduo.webservice._interface.ISoap;

public class MetadataGenerator {
	/* <ExtrinsicObject> map <Document> */
	private HashMap<String, String> docMap;
	private ArrayList<String> DocFolderAssoc;
	private ArrayList<String> SubmissionSetAssoc;
	private ArrayList<String> DocumentList;
	private OMElement ProvideAndRegisterDocumentSetRequest = null;
	private OMElement RegistryObjectList;
	private OMElement ExistingDocumentEntry = null;
	private OMElement ExistingFolder = null;
	private OMElement UniqueId = null;
/*--------*/
private OMElement documentUniqueId = null;
private OMElement folderUniqueId = null;
private OMElement submissionSetUniqueId = null;
/*--------*/
	private XDSSubmissionSet SubmissionSet;
	private int Operations;
	private IAxiomUtil axiom;
	public static Logger logger = Logger.getLogger(MetadataGenerator.class);

	public MetadataGenerator() {
		this.axiom = new AxiomUtil();
	}

	@SuppressWarnings({ "unchecked" })
	public OMElement execution(OMElement source) {
		DocFolderAssoc = new ArrayList<String>();
		ExistingDocumentEntry = source.getFirstChildWithName(new QName("ExistingDocumentEntry"));
		ExistingFolder = source.getFirstChildWithName(new QName("ExistingFolder"));

		Operations = Integer.valueOf(source.getFirstChildWithName(new QName("Operations")).getText());
/*------------*/
//setUniqeId();
/*------------*/		
		/* ProvideAndRegisterDocumentSetRequest */
		ProvideAndRegisterDocumentSetRequest = axiom.createOMElement(EbXML.ProvideAndRegisterDocumentSetRequest, Namespace.IHE);
		/* SubmitObjectsRequest */
		OMElement SubmitObjectsRequest = axiom.createOMElement(EbXML.SubmitObjectsRequest, Namespace.LCM3);
		ProvideAndRegisterDocumentSetRequest.addChild(SubmitObjectsRequest);
		/* SubmitObjectsRequest */
		RegistryObjectList = axiom.createOMElement(EbXML.RegistryObjectList, Namespace.RIM3);
		SubmitObjectsRequest.addChild(RegistryObjectList);

		OMElement element;
		SubmissionSetAssoc = new ArrayList<String>();
		DocumentList = new ArrayList<String>();
		docMap = new HashMap<String, String>();
		/* For each Folder */
		QName qname;
		qname = new QName("Folder");
		Iterator<OMElement> folderList = source.getChildrenWithName(qname);
		/* It's has one or more folde */
		if (folderList.hasNext()) {
			while (folderList.hasNext()) {
				logger.info("--Creating Folder--");
				OMElement folder = folderList.next();
				/* RegistryPackage */
				XDSFolder RegistryPackage = new XDSFolder(source, folder);
/*--------*/
//folderUniqueId.addChild(RegistryPackage.getUniqueId());
/*--------*/
				RegistryObjectList.addChild(RegistryPackage.getRoot());
				RegistryObjectList.addChild(RegistryPackage
						.getClassifsication());
				qname = new QName("Document");
				Iterator<OMElement> docList = folder.getChildrenWithName(qname);
				/* For each Folder's doc */
				while (docList.hasNext()) {
					logger.info("--For each Folder's doc--");
					OMElement doc = docList.next();
					buildExtrinsicObject(source, doc);
				}
				/* Association */
				logger.info("--Folder and Doc Association--");
				String sourceObject = RegistryPackage.getId();
				buildFolderDocAssociation(sourceObject,
						DocumentRelationshipsConstants.HAS_MEMBER);
			}
		}
		{/* It's has a document */
			qname = new QName("Documents");
			OMElement doclist = source.getFirstChildWithName(qname);
			if (doclist != null) {
				try {
					logger.info("--Just has Documents--");
					Iterator<OMElement> iterator = doclist.getChildElements();
					while (iterator.hasNext()) {
						element = iterator.next();
						/*Build Extrinsic Objec*/
						String entryUUID = buildExtrinsicObject(source, element);
						String sourceObject = null, targetObject = null;
						DocumentRelationships association = null;
						OMElement assoc;
						/* Association */
						switch (Operations) {
						case 11971: // Add New Document to Existing Folder
							/* Existing Folder */
							if (ExistingFolder != null) {
								logger.info("--Add New Document to Existing Folder--");
								logger.info("Existing Folder : "
										+ ExistingFolder);
								sourceObject = ExistingFolder.getText();
								buildFolderDocAssociation(
										sourceObject,
										DocumentRelationshipsConstants.HAS_MEMBER);
							}
							break;
						case 11974: // Replace Existing Document
							/* Existing Document */
							if (ExistingDocumentEntry != null) {
								logger.info("--Submit Replace for Existing Document--");
								logger.info("Existing DocumentEntry : "
										+ ExistingDocumentEntry);
								targetObject = ExistingDocumentEntry.getText();
								association = new DocumentRelationships(
										entryUUID, targetObject,
										DocumentRelationshipsConstants.RPLC);
								assoc = association.getRoot();
								RegistryObjectList.addChild(assoc);
							}
							break;
						case 11975: // Submit Transformation for Existing
									// Document
							/* Existing Document */
							if (ExistingDocumentEntry != null) {
								logger.info("--Submit Transformation for Existing Document--");
								logger.info("Existing DocumentEntry : "
										+ ExistingDocumentEntry);
								targetObject = ExistingDocumentEntry.getText();
								association = new DocumentRelationships(
										entryUUID, targetObject,
										DocumentRelationshipsConstants.XFRM);
								assoc = association.getRoot();
								RegistryObjectList.addChild(assoc);
							}
							break;
						case 11977:
							/* Existing Document */
							if (ExistingDocumentEntry != null) {
								logger.info("--Submit Addendum for Existing Document--");
								logger.info("Existing DocumentEntry : "
										+ ExistingDocumentEntry);
								targetObject = ExistingDocumentEntry.getText();
								association = new DocumentRelationships(
										entryUUID, targetObject,
										DocumentRelationshipsConstants.APND);
								assoc = association.getRoot();
								RegistryObjectList.addChild(assoc);
							}
							break;
						case 11976:
							/* Existing Document */
							if (ExistingDocumentEntry != null) {
								logger.info("--Submit Addendum for Existing Document--");
								logger.info("Existing DocumentEntry : "
										+ ExistingDocumentEntry);
								targetObject = ExistingDocumentEntry.getText();
								association = new DocumentRelationships(
										entryUUID,
										targetObject,
										DocumentRelationshipsConstants.XFRM_RPLC);
								assoc = association.getRoot();
								RegistryObjectList.addChild(assoc);
							}
							break;
						}
					}
				} catch (java.lang.NullPointerException e) {
					e.printStackTrace();
				}
			}
			if (Operations == 11973) {
				// Add Existing Document to Existing Folder using XDS.b
				if (ExistingFolder != null && ExistingDocumentEntry != null) {
					logger.info("--Add Existing Document to Existing Folder using XDS.b--");
					logger.info("Existing DocumentEntry : "
							+ ExistingDocumentEntry);
					logger.info("Existing Folder : " + ExistingFolder);
					DocumentRelationships association = new DocumentRelationships(
							ExistingFolder.getText(),
							ExistingDocumentEntry.getText(),
							DocumentRelationshipsConstants.HAS_MEMBER);
					String entryUUID = association.getId();
					RegistryObjectList.addChild(association.getRoot());
					SubmissionSetAssoc.add(entryUUID);
				}
			}
		}

		// http://wiki.ihe.net/index.php?title=XDStar_Documentation#Association_Metadata
		/* SubmissionSet */
		/* RegistryPackage */
		SubmissionSet = new XDSSubmissionSet(source);
/*--------*/
//submissionSetUniqueId.addChild(RegistryPackage.getUniqueId());
/*--------*/
		RegistryObjectList.addChild(SubmissionSet.getRoot());
		RegistryObjectList.addChild(SubmissionSet.getClassifsication());

		/* SubmissionSet with Folder or DocFolderAssoc */
		{
			logger.info("--Association SubmissionSet with Folder or DocFolderAssoc--");
			Iterator<String> iterator = SubmissionSetAssoc.iterator();
			while (iterator.hasNext()) {
				String targetObject = iterator.next();
				String existingUUID = "";
				if (ExistingFolder != null) {
					existingUUID = ExistingFolder.getText();
				}
				if (!existingUUID.equals(targetObject)) {
					DocumentRelationships association = new DocumentRelationships(
							SubmissionSet.getId(), targetObject,
							DocumentRelationshipsConstants.HAS_MEMBER);
					RegistryObjectList.addChild(association.getRoot());
				}
			}
		}

		/* SubmissionSet with Document */
		{
			logger.info("--Association SubmissionSet with Document--");
			Iterator<String> iterator = DocumentList.iterator();
			while (iterator.hasNext()) {
				String targetObject = iterator.next();
				DocumentRelationships association = new DocumentRelationships(
						SubmissionSet.getId(), targetObject, "Original",
						DocumentRelationshipsConstants.HAS_MEMBER);
				RegistryObjectList.addChild(association.getRoot());
			}
		}

		ObejctRef();
		DocumentList();
		ProvideAndRegisterDocumentSetRequest.build();
		return ProvideAndRegisterDocumentSetRequest;
	}

	private void buildFolderDocAssociation(String sourceObject,
			String Assoication) {
		/* Association */
		SubmissionSetAssoc.add(sourceObject);
		/* Folder with it's doc */
		Iterator<String> iterator = DocFolderAssoc.iterator();
		while (iterator.hasNext()) {
			logger.info("--Association Folder with it's Doc--");
			String targetObject = iterator.next();
			DocumentRelationships association = new DocumentRelationships(
					sourceObject, targetObject, Assoication);
			String entryUUID = association.getId();
			RegistryObjectList.addChild(association.getRoot());
			SubmissionSetAssoc.add(entryUUID);
		}
	}

	private String buildExtrinsicObject(OMElement source, OMElement doc) {
		QName qname = new QName("SourcePatientId");
		String sourcePatientId = source.getFirstChildWithName(qname).getText();
		String file_dir = extractDocumentUrl(doc, sourcePatientId);
		XDSDocumentEntry ExtrinsicObject = new XDSDocumentEntry(file_dir, source, doc);
/*--------*/
//documentUniqueId.addChild(ExtrinsicObject.getUniqueId());
/*--------*/
		String entryUUID = ExtrinsicObject.getId();
		if (!file_dir.contains("http")) {
			docMap.put(entryUUID, ExtrinsicObject.getContent());
		}
		RegistryObjectList.addChild(ExtrinsicObject.getRoot());
		if (DocFolderAssoc != null) {
			DocFolderAssoc.add(entryUUID);
		}
		if (DocumentList != null) {
			DocumentList.add(entryUUID);
		}

		File file = new File(file_dir);
		if (file.delete()) {
			logger.debug("file is delete");
		}

		return entryUUID;
	}

	private String extractDocumentUrl(OMElement element, String SourcePatientId) {
		OMElement title = element.getFirstChildWithName(new QName("Title"));
		OMElement description = element.getFirstChildWithName(new QName("Description"));
		OMElement content = element.getFirstChildWithName(new QName("Content"));

		if (content == null || description == null || title == null) {
			try {
				throw new AxisFault("Either Image or FileName is null");
			} catch (AxisFault e) {
				e.printStackTrace();
			}
		}
		String name = title.getText();
		File file = null;
		if (!name.contains("http")) {// attchment not url
			try {
				String encodeBase64 = content.getText();
				byte[] input = Base64.decodeBase64(encodeBase64.getBytes());

				String[] dirs = name.split("/");
				new File(System.getProperty("java.io.tmpdir")).mkdirs();
				file = File.createTempFile("temp", dirs[dirs.length - 1]);
				FileOutputStream OutputStream = new FileOutputStream(file);
				OutputStream.write(input);
				OutputStream.close();
			} catch (IOException e) {
				logger.error(e.toString());
				logger.error("Tomcat temp Directory was miss.");
				e.printStackTrace();
			}
		} else {
			return name;
		}
		return file.getAbsolutePath();
	}

	// -----------------------------------------------------------------//

	private void DocumentList() {
		logger.info("--Create DocumentList--");
		/* Document List */
		Set<String> keySet = docMap.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			/* Document */
			try {
				String entryUUID = iterator.next();
				String base64 = docMap.get(entryUUID);
				OMFactory factory = OMAbstractFactory.getOMFactory();
				OMElement Document = axiom.createOMElement(EbXML.Document,
						Namespace.IHE);
				Document.addAttribute("id", entryUUID, null);
				ISoap soap = ProvideAndRegisterDocumentSet.soap;
				boolean swa = (soap != null) ? soap.isSWA() : false;
				if (!swa) {
					Document.setText(base64);
				} else {
					OMNamespace xop = factory.createOMNamespace(
							"http://www.w3.org/2004/08/xop/include", "xop");
					OMElement inclue = factory.createOMElement("Include", xop);
					inclue.addAttribute("href", base64, null);
					Document.addChild(inclue);
				}
				/* Document */
				ProvideAndRegisterDocumentSetRequest.addChild(Document);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void ObejctRef() {
		logger.info("--Create ObjectRef--");
		synchronized (MetadataGenerator.class) {
			Iterator<String> iterator = PnRCommon.ObjectRef.iterator();
			while (iterator.hasNext()) {
				String obejctRef = iterator.next();
				XDSEntry entry = new XDSEntry();
				OMElement element = entry.addObjectRef(obejctRef);
				RegistryObjectList.addChild(element);
			}
		}
	}

	public void setUniqeId() {
		UniqueId = axiom.createOMElement("UniqueId", null, "");
		submissionSetUniqueId = axiom.createOMElement("SubmissionSet", null, "");
		UniqueId.addChild(submissionSetUniqueId);
		documentUniqueId = axiom.createOMElement("Document", null, "");
		UniqueId.addChild(documentUniqueId);
		folderUniqueId = axiom.createOMElement("Folder", null, "");
		UniqueId.addChild(folderUniqueId);
	}

	public OMElement getUniqeId() {
		return UniqueId;
	}

	public XDSSubmissionSet getSubmissionSet() {
		return SubmissionSet;
	}
	
}
