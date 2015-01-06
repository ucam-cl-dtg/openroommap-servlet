/*******************************************************************************
 * Copyright 2014 Digital Technology Group, Computer Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package ojw28.orm.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public abstract class ServletRequestHandler {
		
	private DOMImplementation mBuilderImpl;
	private TransformerFactory mTransformerFactory;
	private String mUrlExt;
	
	public ServletRequestHandler(String iUrlExt) throws ParserConfigurationException, TransformerConfigurationException
	{	
		mUrlExt = iUrlExt;
		
		DocumentBuilderFactory lFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder lBuilder = lFactory.newDocumentBuilder();
		mBuilderImpl = lBuilder.getDOMImplementation();

		mTransformerFactory = TransformerFactory.newInstance();
	}

	protected void writeXmlResponse(Document iDocument, HttpServletResponse response) throws IOException, TransformerException
	{
		DOMSource domSource = new DOMSource(iDocument);
		StreamResult streamResult = new StreamResult(response.getWriter());
		Transformer lTransformer = mTransformerFactory.newTransformer();
		lTransformer.transform(domSource, streamResult); 
	}  	
	
	protected Document createDocument(String iName)
	{
		return mBuilderImpl.createDocument(null, iName, null);
	}

	protected String getCrsid(HttpServletRequest request)
	{
		String lUser = request.getHeader("X-AAPrincipal");
		if(lUser == null)
		{
			return "no_auth";
		}
		else
		{
			return lUser;
		}
	}
	
	public String getUrlExt()
	{
		return mUrlExt;
	}
	
	public abstract void handleRequest(HttpServletRequest request, HttpServletResponse response);
	

}