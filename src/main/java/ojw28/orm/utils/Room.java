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

import java.io.*;
import org.w3c.dom.*;

/**
 * A room in a 2.5D map, consisting of 1 or more floor polygons.
 * @author ojw28
 */
public class Room {
	
	private Map25D mParentMap;

	private String mName;
	private int mAccessLevel;
	private int mUid;
	
	private FloorPoly[] mFloorPolys;
	private Bounds3D mBounds = new Bounds3D();	
	
	private float mArea;
	private float[] mCentroid = new float[3];
	
	/**
	 * Constructor.
	 * @param iName The name of the room
	 * @param iUid The room's unique identifier
	 * @param iAccessLevel The access level required to enter the room
	 */
	public Room(int iUid, String iName, int iAccessLevel, FloorPoly[] iFloorPolys)
	{
		mUid = iUid;
		mName = iName;
		mAccessLevel = iAccessLevel;
		mFloorPolys = iFloorPolys;
		
		mCentroid[0] = 0;
		mCentroid[1] = 0;
		mCentroid[2] = 0;
		for(FloorPoly lPoly : iFloorPolys)
		{
			lPoly.setParentRoom(this);
			
			mBounds.expand(lPoly);
			mArea += lPoly.getArea();
			mCentroid[0] += lPoly.getCentroid()[0] * lPoly.getArea();
			mCentroid[1] += lPoly.getCentroid()[1] * lPoly.getArea();
			mCentroid[2] += lPoly.getCentroid()[2] * lPoly.getArea();
		}
		mCentroid[0] /= mArea;
		mCentroid[1] /= mArea;
		mCentroid[2] /= mArea;
	}

	public void setParentMap(Map25D iParent)
	{
		mParentMap = iParent;
	}
	
	public Element writeToXml(Document iDoc)
	{
		Element lRoomElement = iDoc.createElement("Room");
		lRoomElement.setAttribute("name", mName);
		lRoomElement.setAttribute("accesslevel", ""+mAccessLevel);
		lRoomElement.setAttribute("uid", ""+mUid);
			
		for(FloorPoly lPoly : mFloorPolys)
		{
			Element lPolyXml = lPoly.writeToXml(iDoc);
			lRoomElement.appendChild(lPolyXml);
		}
		
		return lRoomElement;
	}

	public static Room readFromXml(Element iElement)
	{
		String lRoomName = iElement.getAttribute("name");
		int lAccessLevel = Integer.parseInt(iElement.getAttribute("accesslevel"));
		int lUid = Integer.parseInt(iElement.getAttribute("uid"));

		NodeList lPolyNodes = iElement.getElementsByTagName("Poly");
		FloorPoly[] lPolys = new FloorPoly[lPolyNodes.getLength()];
		for(int li = 0; li < lPolyNodes.getLength(); li++)
		{
			lPolys[li] = FloorPoly.readFromXml((Element) lPolyNodes.item(li));
		}

		Room lRoom = new Room(lUid,lRoomName,lAccessLevel,lPolys);
		return lRoom;
	}
	
	/**
	 * Get the room's name
	 * @return The room's name
	 */
	public String getName()
	{
		return mName;
	}
	
	/**
	 * Get the unique identifier.
	 * @return The room's unique identifier
	 */
	public int getUid()
	{
		return mUid;
	}
	
	/**
	 * Get the centroid of the room.
	 * @return The room's centroid
	 */
	public float[] getCentroid()
	{
		return mCentroid;
	}
	
	/**
	 * Get the bounds of the room.
	 * @return The room's bounds
	 */
	public Bounds3D getBounds()
	{
		return mBounds;
	}
	
	/**
	 * Get the total floor area of the room.
	 * @return The floor area
	 */
	public float getArea()
	{
		return mArea;
	}
	
	/**
	 * Get the access level required to enter the room.
	 * @return The required access level
	 */
	public int getAccessLevel()
	{
		return mAccessLevel;
	}
	
	/**
	 * Get the floor polygons which define the room.
	 * @return The polygons
	 */
	public FloorPoly[] getFloorPolys()
	{
		return mFloorPolys;
	}

	public String toString()
	{
		return mName;
	}
	
	/**
	 * Write the room description out to file.
	 * @param iOutputStream The file output stream
	 * @throws IOException If an IOException occurs
	 */
	public void saveRoomsToFile(PrintWriter iOutputStream) throws IOException
	{
		iOutputStream.println("  Uid\t"+mUid);
		iOutputStream.println("  Room name\t"+mName);
		iOutputStream.println("  Access level\t"+mAccessLevel);
		iOutputStream.println("  Poly count\t"+mFloorPolys.length);
		for(FloorPoly lCurrent : mFloorPolys)
		{
			iOutputStream.println("  POLY");
			lCurrent.writePolyToFile(iOutputStream);
			iOutputStream.println("  END_POLY");
		}
	}
	
	public Map25D getParentMap()
	{
		return mParentMap;
	}
	
	/**
	 * Read a room description from a file.
	 * @param iMap The map to which the room will belong
	 * @param iReader The file stream from which to read the description
	 * @return The constructed room
	 * @throws IOException If an IOException occurs
	 */
	public static Room loadRoomFromFile(Map25D iMap, BufferedReader iReader) throws IOException
	{
		int lUid = Integer.parseInt(iReader.readLine().trim().split("\t")[1]);
		String lRoomName = iReader.readLine().trim().split("\t")[1];
		int lAccessLevel = Integer.parseInt(iReader.readLine().trim().split("\t")[1]);
		int lPolyCount = Integer.parseInt(iReader.readLine().trim().split("\t")[1]);
				
		FloorPoly[] lPolys = new FloorPoly[lPolyCount];
		for(int li = 0; li < lPolyCount; li++)
		{
			iReader.readLine();
			
			lPolys[li] = FloorPoly.loadPolyFromFile(iMap, iReader);
			
			iReader.readLine();
		}

		Room lRoom = new Room(lUid,lRoomName,lAccessLevel,lPolys);
		
		return lRoom;
	}
	
}
