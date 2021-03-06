package edu.tcu.gaduo.ihe;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import edu.tcu.gaduo.ihe.security.Certificate;
import edu.tcu.gaduo.ihe.utility.LoadTesDatatUtil;
import edu.tcu.gaduo.ihe.utility.AxiomUtil;

import edu.tcu.gaduo.ihe.iti.xds_transaction.service.ProvideAndRegisterDocumentSet;

/**
 * Unit test for simple App.
 */
public class SubmitNewFolderInclude5DocTest extends TestCase {
	public static Logger logger = Logger.getLogger(SubmitNewFolderInclude5DocTest.class);
	LoadTesDatatUtil load;

	public SubmitNewFolderInclude5DocTest(String testName) {
		super(testName);
	}

	protected void setUp() {
		load = new LoadTesDatatUtil();
	}

	public void test01() {
		OneSubmit();
	}

	private void OneSubmit() {
		Certificate cert = new Certificate();
		cert.setCertificate("openxds_2010/OpenXDS_2010_Keystore.p12", "password", 
				"openxds_2010/OpenXDS_2010_Truststore.jks", "password");
		OMElement source = load.loadTestDataToOMElement("template/submit_new_folder_include5doc.xml");
		ProvideAndRegisterDocumentSet pnr = new ProvideAndRegisterDocumentSet();
		OMElement response = pnr.MetadataGenerator(source);
		assertEquals(
				"<rs:RegistryResponse xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\" status=\"urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success\"/>",
				response.toString());
		logger.info(response);
	}
}
