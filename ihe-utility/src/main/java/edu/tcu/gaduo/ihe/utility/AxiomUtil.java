package edu.tcu.gaduo.ihe.utility;

import java.util.Iterator;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;


import edu.tcu.gaduo.ihe.constants.EbXML;
import edu.tcu.gaduo.ihe.constants.Namespace;
import edu.tcu.gaduo.ihe.utility._interface.IAxiomUtil;

public class AxiomUtil implements IAxiomUtil {

	@SuppressWarnings("unchecked")
	public Iterator<OMElement> getChildrenWithName(OMElement request,
			Namespace namespace, String tag) {
		Iterator<OMElement> element = null;
		QName qname = null;
		if (namespace != null) {
			qname = new QName(namespace.getNamespace(), tag,
					namespace.getPrefix());
			element = request.getChildrenWithName(qname);
			if (element == null) {
				qname = new QName(namespace.getNamespace(), tag);
				element = request.getChildrenWithName(qname);
			}
		}
		if (element == null) {
			qname = new QName(tag);
			element = request.getChildrenWithName(qname);
		}
		return element;
	}

	public OMElement getFirstChildWithName(OMElement request,
			Namespace namespace, String tag) {
		OMElement element = null;
		QName qname = null;
		if (namespace != null) {
			qname = new QName(namespace.getNamespace(), tag,
					namespace.getPrefix());
			element = request.getFirstChildWithName(qname);
			if (element == null) {
				qname = new QName(namespace.getNamespace(), tag);
				element = request.getFirstChildWithName(qname);
			}
		}
		if (element == null) {
			qname = new QName(tag);
			element = request.getFirstChildWithName(qname);
		}
		return element;
	}

	public OMElement createOMElement(EbXML ebxml, Namespace namespace) {
		OMElement element;
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace Namespace = null;
		if (namespace != null) {
			Namespace = factory.createOMNamespace(namespace.getNamespace(),
					namespace.getPrefix());
			element = factory.createOMElement(ebxml.getTag(), Namespace);
		} else {
			QName qname = new QName(ebxml.getTag());
			element = factory.createOMElement(qname);
		}
		return element;
	}

	public OMElement createOMElement(String tag, String namespace, String prefix) {
		OMElement element;
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace Namespace = null;
		if (namespace != null) {
			Namespace = factory.createOMNamespace(namespace, prefix);
			element = factory.createOMElement(tag, Namespace);
		} else {
			element = factory.createOMElement(tag, null);
		}
		return element;
	}

	public OMNamespace createNamespace(String namespace, String prefix) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace Namespace = factory.createOMNamespace(namespace, prefix);
		return Namespace;
	}
	
	  
    public OMElement createOMElement(String tag, String value) { 
        OMFactory factory = OMAbstractFactory.getOMFactory(); 
        OMElement element = factory.createOMElement(tag, null); 
        if (value != null) { 
            element.setText(value); 
        } 
        return element; 
    } 
    
	public String getValueOfType(String type, OMElement request) {
		String value;
		QName qname = new QName(type);
		OMElement e = request.getFirstChildWithName(qname);
		value = (e != null) ? e.getText().trim() : null;
		return value;
	}

}
