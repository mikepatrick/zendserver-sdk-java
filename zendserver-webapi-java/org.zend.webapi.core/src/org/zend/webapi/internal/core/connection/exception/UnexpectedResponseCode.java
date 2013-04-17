/*******************************************************************************
 * Copyright (c) Feb 28, 2011 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/
package org.zend.webapi.internal.core.connection.exception;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Node;
import org.zend.webapi.core.WebApiException;
import org.zend.webapi.core.connection.response.ResponseCode;

/**
 * Unexpected response code
 * 
 * @author Roy, 2011
 * 
 */
public class UnexpectedResponseCode extends WebApiException {

	private static final long serialVersionUID = -2095471259882902506L;

	final private int httpCode;
	final private String message;
	private String errorCode;


	public UnexpectedResponseCode(int httpCode, Representation handle) {
		this.httpCode = httpCode;
		if (MediaType.TEXT_HTML.equals(handle.getMediaType())) {
			if (httpCode == 500) {
				this.errorCode = "internalServerError";
				this.message = ResponseCode.byErrorCode(errorCode)
						.getDescription();
			} else {
				this.errorCode = "pageNotFound";
				this.message = ResponseCode.byErrorCode(errorCode)
						.getDescription();
			}
		} else {
			final DomRepresentation domRepresentation = new DomRepresentation(
					handle);
			Node node = domRepresentation
					.getNode("/zendServerAPIResponse/errorData/errorMessage");
			this.message = node == null ? null : node.getTextContent().trim();
			node = domRepresentation.getNode("/zendServerAPIResponse/errorData/errorCode");
			this.errorCode = node == null ? null : node.getTextContent().trim();
		}
	}

	@Override
	public String getMessage() {
		return message != null ? message : getResponseCode().getDescription();
	}

	@Override
	public ResponseCode getResponseCode() {
		if (httpCode == 200) {
			return ResponseCode.OK;
		}
		if (httpCode == 202) {
			return ResponseCode.ACCEPTED;
		}
		return ResponseCode.byErrorCode(errorCode);
	}
}
