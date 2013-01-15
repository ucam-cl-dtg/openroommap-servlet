package ojw28.orm.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;

public class UserHandler extends ServletRequestHandler {

    private static Logger mLogger = Logger.getLogger("ojw28.orm.servlet.UserHandler");
	
	public UserHandler() throws TransformerConfigurationException, ParserConfigurationException
	{
		super("/whoami");
		mLogger.info("Handler successfully initialised");
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		try
		{
			Document lXml = createDocument("WhoAmIResponse");
			lXml.getDocumentElement().setAttribute("crsid", super.getCrsid(request));
			writeXmlResponse(lXml, response);
		}
		catch(Exception lE)
		{                
			mLogger.log(Level.SEVERE, "Exception caught while handling request :\t"+ request.getPathInfo(), lE);
		}
	}  
	
}
