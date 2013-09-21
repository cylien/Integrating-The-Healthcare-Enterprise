package com.gaduo.webservice._interface;

import javax.activation.DataHandler;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;

public interface ISoap {
	public MessageContext send(OMElement data);
	public boolean isMTOM_XOP();
	public String addAttachment(DataHandler handler) ;
}