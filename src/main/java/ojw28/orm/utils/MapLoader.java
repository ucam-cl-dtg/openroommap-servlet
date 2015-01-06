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
package ojw28.orm.utils;

import java.sql.*;
import java.util.*;

public class MapLoader {
	
	public Map25D loadMap(Connection iConnection) throws SQLException
	{
		return loadMap(iConnection, null);
	}

	public Map25D loadSubmap(Connection iConnection, String iSubmap) throws SQLException
	{		
		Statement st = iConnection.createStatement();
		try
		{			
			ResultSet res = null;
			res = st.executeQuery("SELECT (submapid) FROM submap_table WHERE name = '"+iSubmap+"'");
			res.next();
			int lSubmapId = res.getInt(1);
			st.close();
			Map25D lMap =  loadMap(iConnection, lSubmapId);			
			return lMap;
		}
		catch (SQLException lE)
		{
			throw lE;
		}
		finally
		{
			st.close();
		}
	}
	
	private Map25D loadMap(Connection iDbConnection, Integer iSubmap) throws SQLException
	{				
		Map25D lMap = new Map25D();
		Hashtable<Integer,ArrayList<FloorPoly>> lRoomPolys = new Hashtable<Integer,ArrayList<FloorPoly>>();

		Statement st = iDbConnection.createStatement();
		try
		{
			ResultSet res = null;
			if(iSubmap != null)
			{
				res = st.executeQuery("SELECT * FROM submappoly_table INNER JOIN roompoly_table ON submappoly_table.polyid = roompoly_table.polyid WHERE submapid = "+iSubmap);
			}
			else
			{
				res = st.executeQuery("SELECT * FROM roompoly_table");	
			}

			while (res.next()) {
				int lPolyId = res.getInt("polyid");
				int lRoomId = res.getInt("roomid");
				if(!lRoomPolys.containsKey(lRoomId))
				{
					lRoomPolys.put(lRoomId, new ArrayList<FloorPoly>());
				}

				FloorPoly lPoly = loadPoly(iDbConnection, lPolyId);
				lRoomPolys.get(lRoomId).add(lPoly);
			}	
			res.close();

			Enumeration<Integer> lKeys = lRoomPolys.keys();
			while(lKeys.hasMoreElements())
			{
				int lRoomId = lKeys.nextElement();
				ArrayList<FloorPoly> lChildren = lRoomPolys.get(lRoomId);
				FloorPoly[] lChildrenArray = new FloorPoly[lChildren.size()];
				lChildren.toArray(lChildrenArray);
				lMap.addRoom(loadRoom(iDbConnection, lMap, lRoomId, lChildrenArray));
			}

			lMap.compileMap();
		}
		finally
		{
			st.close();
		}

		return lMap;
	}
	
	private Room loadRoom(Connection conn, Map25D iParentMap, int iRoomId, FloorPoly[] iChildren) throws SQLException
	{
		Statement st = conn.createStatement();
		try
		{
			ResultSet res = st.executeQuery("SELECT * FROM room_table WHERE roomid = "+iRoomId);
			res.next();
			String lRoomName = res.getString("name");
			int lAccessLevel = res.getInt("accesslevel");

			Room lRoom = new Room(iRoomId, lRoomName, lAccessLevel,iChildren);
			lRoom.setParentMap(iParentMap);
			return lRoom;
		}
		finally
		{
			st.close();
		}
	}
	
	private FloorPoly loadPoly(Connection conn, int iPolyId) throws SQLException
	{
		Statement st = conn.createStatement();
		try
		{
			ResultSet res = st.executeQuery("SELECT COUNT(*) FROM floorpoly_table WHERE polyid = "+iPolyId);
			res.next();
			int lVertexCount = res.getInt(1);
			float[] lVertices = new float[lVertexCount * 3];
			Integer[] lConnections = new Integer[lVertexCount];
			res.close();

			res = st.executeQuery("SELECT * FROM floorpoly_table WHERE polyid = "+iPolyId+" ORDER BY vertexnum");
			for(int li = 0; li < lVertices.length; li+=3)
			{
				res.next();
				lVertices[li] = res.getFloat("x");
				lVertices[li + 1] = res.getFloat("y");
				lVertices[li + 2] = res.getFloat("z");
				int lEdgeTarget = res.getInt("edgetarget");
				boolean lIsConnection = !res.wasNull();
				if(lIsConnection)
				{
					lConnections[li/3] = lEdgeTarget;
				}
			}
			res.close();

			FloorPoly lPoly = new FloorPoly(iPolyId, lVertices, lConnections);
			return lPoly;
		}
		finally
		{
			st.close();
		}
	}
	
	@SuppressWarnings("unused")
	private float[] loadFurnitureVertices(Connection conn, int iFurnitureId) throws SQLException
	{
		Statement st = conn.createStatement();
		try
		{
			ResultSet res = st.executeQuery("SELECT COUNT(*) FROM furniturepoly_table WHERE furnitureid = "+iFurnitureId+" ORDER BY vertexnum");
			res.next();
			int lVertexCount = res.getInt(1);
			float[] lVertices = new float[lVertexCount * 3];

			res = st.executeQuery("SELECT * FROM furniturepoly_table WHERE furnitureid = "+iFurnitureId+" ORDER BY vertexnum");
			for(int li = 0; li < lVertices.length; li+=3)
			{
				res.next();
				lVertices[li] = res.getFloat("x");
				lVertices[li + 1] = res.getFloat("y");
				lVertices[li + 2] = res.getFloat("z");
			}

			res.close();

			return lVertices;
		}
		finally
		{
			st.close();
		}
	}
}
