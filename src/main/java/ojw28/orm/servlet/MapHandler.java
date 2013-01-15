package ojw28.orm.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import ojw28.orm.utils.Map25D;
import ojw28.orm.utils.MapLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class MapHandler extends ServletRequestHandler {

	private static Logger mLogger = Logger
			.getLogger("ojw28.orm.servlet.MapHandler");
	private Document mDocument;

	public MapHandler() throws ParserConfigurationException,
			TransformerConfigurationException, SQLException {
		super("/getmap");
		buildMapDocument();
		mLogger.info("Handler successfully initialised");
	}

	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			writeXmlResponse(mDocument, response);
		} catch (Exception lE) {
			mLogger.log(Level.SEVERE,
					"Exception caught while handling request :\t"
							+ request.getPathInfo(), lE);
		}
	}

	public void buildMapDocument() throws SQLException {
		Document lDocument = createDocument("MapRequestResponse");

		Connection lConnection = ojw28.orm.utils.DbConnectionPool
				.getSingleton().getConnection();
		try {
			MapLoader lLoader = new MapLoader();

			Map25D lGroundMap = lLoader.loadSubmap(lConnection, "Ground");
			Element lGroundXml = lGroundMap.writeToXml(lDocument);
			lGroundXml.setAttribute("name", "Ground");
			lGroundXml.setAttribute("level", "0");

			Map25D lFirstMap = lLoader.loadSubmap(lConnection, "First");
			Element lFirstXml = lFirstMap.writeToXml(lDocument);
			lFirstXml.setAttribute("name", "First");
			lFirstXml.setAttribute("level", "1");

			Map25D lSecondMap = lLoader.loadSubmap(lConnection, "Second");
			Element lSecondXml = lSecondMap.writeToXml(lDocument);
			lSecondXml.setAttribute("name", "Second");
			lSecondXml.setAttribute("level", "2");

			Element lMapElement = lDocument.createElement("Map");
			lDocument.getDocumentElement().appendChild(lMapElement);
			lMapElement.appendChild(lGroundXml);
			lMapElement.appendChild(lFirstXml);
			lMapElement.appendChild(lSecondXml);

			Element lOccupancyElement = lDocument.createElement("Occupancy");
			lDocument.getDocumentElement().appendChild(lOccupancyElement);

			PreparedStatement lOccupantsStatement = lConnection
					.prepareStatement("SELECT * FROM occupants_table");
			try {
				ResultSet lUpdates = lOccupantsStatement.executeQuery();
				while (lUpdates.next()) {
					String lCrsid = lUpdates.getString("crsid");
					int lRoom = lUpdates.getInt("roomid");

					Element lItemElement = lDocument.createElement("Mapping");
					lItemElement.setAttribute("crsid", "" + lCrsid);
					lItemElement.setAttribute("roomid", "" + lRoom);
					lOccupancyElement.appendChild(lItemElement);
				}
				lUpdates.close();
			} finally {
				lOccupantsStatement.close();
			}
		} finally {
			lConnection.close();
		}
		mDocument = lDocument;
	}

}
