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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import ojw28.orm.utils.DbConnectionPool;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PlacedItemHandler extends ServletRequestHandler {

    private static Logger mLogger = Logger.getLogger("ojw28.orm.servlet.PlacedItemHandler");
    
	private DocumentBuilder mBuilder;
	private DOMImplementation mBuilderImpl;
	
	public PlacedItemHandler() throws ParserConfigurationException, TransformerConfigurationException
	{
		super("/items");
		
		DocumentBuilderFactory mFactory = DocumentBuilderFactory.newInstance();
		mBuilder = mFactory.newDocumentBuilder();
		mBuilderImpl = mBuilder.getDOMImplementation();
		
		mLogger.info("Handler successfully initialised");
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		String lRequest = request.getPathInfo();
		try
		{
			if(lRequest.startsWith("/items/fetchupdatesfloor"))
			{
				fetchUpdateFloorHandler(request, response);
			}
			else if(lRequest.equals("/items/doupdate"))
			{
				updateItemHandler(request, response);
			}
			else if(lRequest.equals("/items/doadd"))
			{
				addNewItemHandler(request, response);
			}
			else if(lRequest.startsWith("/items/doremove"))
			{
				removeItemHandler(request, response);
			}
			else if(lRequest.startsWith("/items/getitemsfloor"))
			{
				itemRequestFloorHandler(request, response);
			}
			else
			{
				mLogger.warning("Unknown request not handled :\t" + lRequest);
			}
		}
		catch(Exception lE)
		{                
			mLogger.log(Level.SEVERE, "Exception caught while handling request :\t"+ request.getPathInfo(), lE);
		}
	}

	private void addNewItemHandler(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, TransformerException
	{
		int lItemId = Integer.parseInt(request.getParameter("item_def_id"));
		int lFloor = Integer.parseInt(request.getParameter("floor"));
		float lX = Float.parseFloat(request.getParameter("x"));
		float lY = Float.parseFloat(request.getParameter("y"));
		int lTheta = Integer.parseInt(request.getParameter("theta"));
		boolean lFlipped = Boolean.parseBoolean(request.getParameter("flipped"));
		String lLabel = request.getParameter("label");

		int lAssignedUid = writeNewItemToDb(lItemId,getCrsid(request),lX,lY,lTheta,lFloor,lFlipped,lLabel);

		Document lUpdateXml = createDocument("ItemAdded");
		Element lRootElement = lUpdateXml.getDocumentElement();
		lRootElement.setAttribute("uid", ""+lAssignedUid);
		writeXmlResponse(lUpdateXml, response);
	}

	public int writeNewItemToDb(int iItemDefId, String iCrsid, float iX, float iY, int iTheta, int iFloor, boolean iFlipped, String iLabel) throws SQLException
	{
		Connection lConnection = DbConnectionPool.getSingleton().getConnection();
		try
		{
			int lAssignedUid = 0;
			PreparedStatement lGetPolyId = lConnection.prepareStatement("SELECT nextval('item_id_seq')");
			try
			{
				ResultSet lRes = lGetPolyId.executeQuery();
				lRes.next();
				lAssignedUid = lRes.getInt(1);
				lRes.close();

				PreparedStatement lInsertItem = lConnection.prepareStatement("INSERT INTO " +
				"placed_item_table(item_id,item_def_id) VALUES(?,?)");
				try
				{
					lInsertItem.setInt(1,lAssignedUid);
					lInsertItem.setInt(2, iItemDefId);
					lInsertItem.execute();

					writeUpdateToDb(lConnection,iCrsid,lAssignedUid,iX,iY,iTheta,iFloor,iFlipped,false,iLabel);
					
					lConnection.commit();
										
					return lAssignedUid;
				}
				finally
				{
					lInsertItem.close();
				}
			}
			finally
			{
				lGetPolyId.close();
			}
		}
		catch(SQLException lE)
		{
			lConnection.rollback();
			throw(lE);
		}
		finally
		{
			lConnection.close();
		}
	}
	
	private void updateItemHandler(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			int lUid = Integer.parseInt(request.getParameter("uid"));
			float lX = Float.parseFloat(request.getParameter("x"));
			float lY = Float.parseFloat(request.getParameter("y"));
			int lTheta = Integer.parseInt(request.getParameter("theta"));
			int lFloor = Integer.parseInt(request.getParameter("floor"));
			boolean lFlipped = Boolean.parseBoolean(request.getParameter("flipped"));
			String lLabel = request.getParameter("label");

			Connection lConnection = DbConnectionPool.getSingleton().getConnection();
			try
			{
				writeUpdateToDb(lConnection,getCrsid(request),lUid,lX,lY,lTheta,lFloor,lFlipped,false, lLabel);
				lConnection.commit();
			}
			catch(SQLException lE)
			{
				lConnection.rollback();
				throw(lE);
			}
			finally
			{
				lConnection.close();
			}
		}
		catch(Exception lE)
		{
			mLogger.log(Level.SEVERE, "Exception caught while handling request :\t"+ request.getPathInfo(), lE);
		}
	}
	
	private void removeItemHandler(HttpServletRequest request, HttpServletResponse response) throws SQLException
	{
		int lUid = Integer.parseInt(request.getParameter("uid"));

		Connection lConnection = DbConnectionPool.getSingleton().getConnection();
		try
		{
			writeRemoveToDb(lConnection,getCrsid(request),lUid);
			lConnection.commit();
		}
		catch(SQLException lE)
		{
			lConnection.rollback();
			throw(lE);
		}
		finally
		{
			lConnection.close();
		}
	}

	private void fetchUpdateFloorHandler(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, TransformerException
	{
		int lLastUpdate = Integer.parseInt(request.getParameter("lastupdate"));
		int lFloor = Integer.parseInt(request.getParameter("floor"));

		Document lUpdateDoc = getUpdatesFloor(lLastUpdate, lFloor);
		writeXmlResponse(lUpdateDoc, response);
	}

	public Document getUpdatesFloor(int iLastUpdateId, int iFloor) throws SQLException
	{
		Document lUpdateXml = mBuilderImpl.createDocument(null, "ItemUpdate", null);
		Element lRootElement = lUpdateXml.getDocumentElement();
		
		int lUpdateId = iLastUpdateId;
		
		Connection lConnection = DbConnectionPool.getSingleton().getConnection();
		try
		{
			PreparedStatement lFetchUpdates = lConnection.prepareStatement(
					"SELECT * FROM placed_item_table INNER JOIN placed_item_update_table "+
					"ON placed_item_table.last_update = placed_item_update_table.update_id " +
					"WHERE last_update > ? AND floor_id = ?");
			try
			{
				lFetchUpdates.setInt(1, iLastUpdateId);
				lFetchUpdates.setInt(2, iFloor);
				ResultSet lUpdates = lFetchUpdates.executeQuery();
				while(lUpdates.next())
				{
					int lUid = lUpdates.getInt("item_id");
					int lItemDefId = lUpdates.getInt("item_def_id");
					float lX = lUpdates.getFloat("x");
					float lY = lUpdates.getFloat("y");
					int lTheta = lUpdates.getInt("theta");
					int lFloor = lUpdates.getInt("floor_id");
					boolean lFlipped = lUpdates.getBoolean("flipped");
					boolean lDeleted = lUpdates.getBoolean("deleted");
					String lLabel = lUpdates.getString("label");

					lUpdateId = Math.max(lUpdateId, lUpdates.getInt("last_update"));

					writePlacedItemXml(lUpdateXml, lRootElement, lUid, lItemDefId, lX, lY, lTheta, lFloor, lFlipped, lDeleted, lLabel);
				}
				lUpdates.close();
			}
			finally
			{
				lFetchUpdates.close();
			}
		}
		finally
		{
			lConnection.close();
		}
		
		lRootElement.setAttribute("updatetoken", ""+lUpdateId);
		
		return lUpdateXml;
	}
	
	public Document getUpdates(int iLastUpdateId) throws SQLException
	{
		Document lUpdateXml = mBuilderImpl.createDocument(null, "ItemUpdate", null);
		Element lRootElement = lUpdateXml.getDocumentElement();
		
		int lUpdateId = iLastUpdateId;
		
		Connection lConnection = DbConnectionPool.getSingleton().getConnection();
		try
		{
			PreparedStatement lFetchUpdates = lConnection.prepareStatement(
					"SELECT * FROM placed_item_table INNER JOIN placed_item_update_table "+
					"ON placed_item_table.last_update = placed_item_update_table.update_id " +
					"WHERE last_update > ?");
			try
			{
				lFetchUpdates.setInt(1, iLastUpdateId);
				ResultSet lUpdates = lFetchUpdates.executeQuery();
				while(lUpdates.next())
				{
					int lUid = lUpdates.getInt("item_id");
					int lItemDefId = lUpdates.getInt("item_def_id");
					float lX = lUpdates.getFloat("x");
					float lY = lUpdates.getFloat("y");
					int lTheta = lUpdates.getInt("theta");
					int lFloor = lUpdates.getInt("floor_id");
					boolean lFlipped = lUpdates.getBoolean("flipped");
					boolean lDeleted = lUpdates.getBoolean("deleted");
					String lLabel = lUpdates.getString("label");

					lUpdateId = Math.max(lUpdateId, lUpdates.getInt("last_update"));

					writePlacedItemXml(lUpdateXml, lRootElement, lUid, lItemDefId, lX, lY, lTheta, lFloor, lFlipped, lDeleted, lLabel);
				}
				lUpdates.close();
			}
			finally
			{
				lFetchUpdates.close();
			}
		}
		finally
		{
			lConnection.close();
		}
		
		lRootElement.setAttribute("updatetoken", ""+lUpdateId);
		
		return lUpdateXml;
	}

	private void itemRequestFloorHandler(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, TransformerException
	{
		int lFloor = Integer.parseInt(request.getParameter("floor"));

		Document lUpdateXml = mBuilderImpl.createDocument(null, "Items", null);
		Element lRootElement = lUpdateXml.getDocumentElement();

		int lUpdateId = 0;

		Connection lConnection = DbConnectionPool.getSingleton().getConnection();
		try
		{
			PreparedStatement lFetchUpdates = lConnection.prepareStatement(
					"SELECT * FROM placed_item_table INNER JOIN placed_item_update_table "+
					"ON placed_item_table.last_update = placed_item_update_table.update_id " +
					"WHERE NOT deleted AND floor_id = ?");
			lFetchUpdates.setInt(1,lFloor);
			try
			{
				ResultSet lUpdates = lFetchUpdates.executeQuery();
				while(lUpdates.next())
				{
					int lUid = lUpdates.getInt("item_id");
					int lItemDefId = lUpdates.getInt("item_def_id");
					float lX = lUpdates.getFloat("x");
					float lY = lUpdates.getFloat("y");
					int lTheta = lUpdates.getInt("theta");
					boolean lFlipped = lUpdates.getBoolean("flipped");
					boolean lDeleted = lUpdates.getBoolean("deleted");
					String lLabel = lUpdates.getString("label");

					lUpdateId = Math.max(lUpdateId, lUpdates.getInt("last_update"));

					writePlacedItemXml(lUpdateXml, lRootElement, lUid, lItemDefId, lX, lY, lTheta, lFloor, lFlipped, lDeleted, lLabel);
				}
				lUpdates.close();
			}
			finally
			{
				lFetchUpdates.close();
			}
			
		}
		finally
		{
			lConnection.close();
		}

		lRootElement.setAttribute("updatetoken", ""+lUpdateId);

		writeXmlResponse(lUpdateXml, response);
	}

	private void writePlacedItemXml(Document iDoc, Element iParent, int iUid, int iItemDefId, float iX, float iY, int iTheta, int iFloor, boolean iFlipped, boolean iDeleted, String iLabel)
	{
		Element lItemElement = iDoc.createElement("Item");
		lItemElement.setAttribute("uid", ""+iUid);
		lItemElement.setAttribute("x", ""+iX);
		lItemElement.setAttribute("y", ""+iY);
		lItemElement.setAttribute("theta", ""+iTheta);
		lItemElement.setAttribute("floor", ""+iFloor);
		lItemElement.setAttribute("flipped", ""+iFlipped);
		lItemElement.setAttribute("deleted", ""+iDeleted);
		lItemElement.setAttribute("item_def_id",""+iItemDefId);
		lItemElement.setAttribute("label", iLabel);
		iParent.appendChild(lItemElement);	
	}	
	
	private void writeUpdateToDb(Connection iConnection, String iCrsid, int iUid, float iX, float iY, int iTheta, int iFloor, boolean iFlipped, boolean iDeleted, String iLabel) throws SQLException
	{
		PreparedStatement lGetUpdateId = iConnection.prepareStatement("SELECT nextval('item_update_seq')");
		try
		{
			ResultSet lRes = lGetUpdateId.executeQuery();
			lRes.next();
			int lLastUpdateToken = lRes.getInt(1);
			lRes.close();

			PreparedStatement lInsertUpdate = iConnection.prepareStatement("INSERT INTO " +
					"placed_item_update_table(update_id,crsid,item_id,x,y,theta,floor_id,flipped,deleted,label) VALUES(?,?,?,?,?,?,?,?,?,?)");
			try
			{
				lInsertUpdate.setInt(1, lLastUpdateToken);
				lInsertUpdate.setString(2, iCrsid);
				lInsertUpdate.setInt(3,iUid);
				lInsertUpdate.setFloat(4, iX);
				lInsertUpdate.setFloat(5, iY);
				lInsertUpdate.setInt(6, iTheta);
				lInsertUpdate.setInt(7, iFloor);
				lInsertUpdate.setBoolean(8, iFlipped);
				lInsertUpdate.setBoolean(9, iDeleted);
				lInsertUpdate.setString(10, iLabel);
				lInsertUpdate.execute();

				PreparedStatement lSetUpdate = iConnection.prepareStatement("UPDATE placed_item_table SET last_update = ? WHERE item_id = ?");
				try
				{
					lSetUpdate.setInt(1, lLastUpdateToken);
					lSetUpdate.setInt(2, iUid);
					lSetUpdate.execute();
				}
				finally
				{
					lSetUpdate.close();
				}
			}
			finally
			{
				lInsertUpdate.close();
			}
		}
		finally
		{
			lGetUpdateId.close();
		}
	}

	private void writeRemoveToDb(Connection iConnection, String iCrsid, int iUid) throws SQLException
	{			
		PreparedStatement lFetchLastState = iConnection.prepareStatement(
				"SELECT * FROM placed_item_table INNER JOIN placed_item_update_table "+
				"ON placed_item_table.last_update = placed_item_update_table.update_id " +
				"WHERE placed_item_table.item_id = ?");
		try
		{
			lFetchLastState.setInt(1, iUid);
			ResultSet lLastState = lFetchLastState.executeQuery();
			lLastState.next();
			{
				float lX = lLastState.getFloat("x");
				float lY = lLastState.getFloat("y");
				int lTheta = lLastState.getInt("theta");
				int lFloor = lLastState.getInt("floor_id");
				boolean lFlipped = lLastState.getBoolean("flipped");
				String lLabel = lLastState.getString("label");

				writeUpdateToDb(iConnection, iCrsid, iUid, lX, lY, lTheta, lFloor, lFlipped, true, lLabel);
			}
			lLastState.close();
		}
		finally
		{
			lFetchLastState.close();
		}
	}

}
