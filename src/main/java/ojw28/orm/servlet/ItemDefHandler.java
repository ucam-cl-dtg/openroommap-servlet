package ojw28.orm.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import ojw28.orm.ItemDef;
import ojw28.orm.ItemDefPoly;
import ojw28.orm.utils.DbConnectionPool;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ItemDefHandler extends ServletRequestHandler 
{
    private static Logger mLogger = Logger.getLogger("ojw28.orm.servlet.ItemDefHandler");

	public ItemDefHandler() throws ParserConfigurationException, TransformerConfigurationException, InterruptedException
	{		
		super("/components");
		mLogger.info("Handler successfully initialised");
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		try
		{
			Document lDocument = createDocument("ComponentLibrary");
			Element lRoot = lDocument.getDocumentElement();
			buildItemDefinitionXml(lDocument, lRoot);
			writeXmlResponse(lDocument, response);
		}
		catch(Exception lE)
		{                
			mLogger.log(Level.SEVERE, "Exception caught while handling request :\t"+ request.getPathInfo(), lE);
		}
	}

	private void buildItemDefinitionXml(Document iDocument, Element iParent) throws SQLException
	{
		Connection lItemConn = DbConnectionPool.getSingleton().getConnection();
		try
		{
			PreparedStatement lPolyStatement = lItemConn.prepareStatement("SELECT * FROM item_polygon_table WHERE item_def_id=?");
			try
			{
				PreparedStatement lVertexStatement = lItemConn.prepareStatement("SELECT * FROM item_polygon_vertex_table WHERE poly_id=?");
				try
				{
					Statement lItemStatement = lItemConn.createStatement();
					try
					{
						ResultSet lItemRes = lItemStatement.executeQuery("SELECT * FROM item_definition_table ORDER BY ordering,name");

						while(lItemRes.next())
						{
							int lItemId = lItemRes.getInt("def_id");
							String lName = lItemRes.getString("name");
							int lOrdering = lItemRes.getInt("ordering");
							String lCategory = lItemRes.getString("category");
							String lImageFile = lItemRes.getString("image_file");
							String lDescription = lItemRes.getString("description");
							String lFieldLabel = lItemRes.getString("field_label");
							
							float lHeight = lItemRes.getFloat("height");
							boolean lFlipable = lItemRes.getBoolean("flipable");

							lPolyStatement.setInt(1, lItemId);
							ResultSet lPolyRes = lPolyStatement.executeQuery();
							ArrayList<ItemDefPoly> lTempPolys = new ArrayList<ItemDefPoly>();

							while(lPolyRes.next())
							{
								int lPolyId = lPolyRes.getInt("poly_id");
								int lFillColour = lPolyRes.getInt("fill_colour");
								float lFillAlpha = lPolyRes.getFloat("fill_alpha");
								int lEdgeColour = lPolyRes.getInt("edge_colour");
								float lEdgeAlpha = lPolyRes.getFloat("edge_alpha");

								lVertexStatement.setInt(1, lPolyId);
								ResultSet lVertexRes = lVertexStatement.executeQuery();
								ArrayList<Float> lTempVertices = new ArrayList<Float>();

								while(lVertexRes.next())
								{
									lTempVertices.add(lVertexRes.getFloat("x"));
									lTempVertices.add(lVertexRes.getFloat("y"));
								}

								float[] lVertexArray = new float[lTempVertices.size()];
								for(int li = 0; li < lVertexArray.length; li++)
								{
									lVertexArray[li] = lTempVertices.get(li);
								}

								lTempPolys.add(new ItemDefPoly(lVertexArray,lFillColour,lFillAlpha,lEdgeColour,lEdgeAlpha));
							}

							ItemDefPoly[] lPolyArray = new ItemDefPoly[lTempPolys.size()];
							lTempPolys.toArray(lPolyArray);


							ItemDef lDef = new ItemDef(lItemId,lName,lOrdering,lCategory,lImageFile,lDescription,lFieldLabel,lFlipable,lHeight,lPolyArray);
							lDef.writeToXml(iDocument, iParent);
						}
					}
					finally
					{
						lItemStatement.close();
					}
				}
				finally
				{
					lVertexStatement.close();
				}
			}
			finally
			{
				lPolyStatement.close();
			}
		}
		finally
		{
			lItemConn.close();
		}
	}
}
