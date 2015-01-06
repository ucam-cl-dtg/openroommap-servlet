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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * A 2.5D representation of a building. Each Map is made up of Rooms, which in turn are made
 * up of FloorPoly objects. Each FloorPoly object is a planar 2.5D polygon (i.e. a 2D polygon in
 * (x,y) with a defined height (z).
 * @author ojw28
 */
public class Map25D {

	private static Random mRand = new Random();
	
	//Floor polygons indexed by the uids.
	private ArrayList<FloorPoly> mFloorPolys = new ArrayList<FloorPoly>();
	private Hashtable<Integer, FloorPoly> mIndexedPolys = new Hashtable<Integer, FloorPoly>();
	private ArrayList<Room> mRooms = new ArrayList<Room>();
	private Hashtable<Integer, Room> mIndexedRooms = new Hashtable<Integer, Room>();
		
	private Bounds3D mBounds = new Bounds3D();
	private float mTotalArea = 0;
	
	/**
	 * Constructor.
	 */
	public Map25D()
	{
	}
	
	public void addRoom(Room lRoom)
	{
		mTotalArea += lRoom.getArea();
		mBounds.expand(lRoom.getBounds());

		mRooms.add(lRoom);
		mIndexedRooms.put(lRoom.getUid(), lRoom);

		for(FloorPoly lPoly : lRoom.getFloorPolys())
		{
			mFloorPolys.add(lPoly);
			mIndexedPolys.put(lPoly.getUid(), lPoly);
		}
	}

	/**
	 * Gets the FloorPoly with a specified uid.
	 * @param iUid The uid of the required polygon
	 * @return The floor polygon with the specified uid
	 */
	public FloorPoly getFloorPoly(int iUid)
	{
		if(mIndexedPolys.containsKey(iUid))
		{
			return mIndexedPolys.get(iUid);			
		}
		return null;
	}
		
	public Room getRoom(int iUid)
	{
		return mIndexedRooms.get(iUid);
	}
	
	public ArrayList<FloorPoly> getFloorPolys()
	{
		return mFloorPolys;
	}

	public Room getRoom(String mName)
	{
		return mIndexedRooms.get(mName);
	}
	
	public ArrayList<Room> getRooms()
	{
		return mRooms;
	}

	/**
	 * The map's bounds as an array.
	 * @return The bounds (xMin,yMin,zMin,xMax,yMax,zMax)
	 */
	public float[] getBounds()
	{
		return mBounds.getBounds();
	}

	/**
	 * Gets the total floor area of the map.
	 * @return The total floor area of the map
	 */
	public float getTotalArea()
	{
		return mTotalArea;
	}

	/**
	 * Return a random position in the map, drawn from a uniform
	 * distribution over the entire map surface.
	 * @return A random position, or null if the map has a total area equal to 0
	 */
	public PointInMap getRandomPosition()
	{
		float lAreaIndex = mRand.nextFloat() * mTotalArea;
		float lAreaAcc = 0;
		for(FloorPoly lPoly : mFloorPolys)
		{
			lAreaAcc += lPoly.getArea();
			if(lAreaIndex < lAreaAcc)
			{
				float[] lPointInRoom = lPoly.getRandomPosition2D();
				return new PointInMap(lPointInRoom, lPoly);
			}
		}
		//The map has a surface area of 0
		return null;
	}
	
	public void readFromXml(Element iElement)
	{
		NodeList lRoomNodes = iElement.getElementsByTagName("Room");
		for(int li = 0; li < lRoomNodes.getLength(); li++)
		{
			Room lRoom = Room.readFromXml((Element) lRoomNodes.item(li));
			lRoom.setParentMap(this);
			
			mTotalArea += lRoom.getArea();
			mBounds.expand(lRoom.getBounds());

			mRooms.add(lRoom);
			mIndexedRooms.put(lRoom.getUid(), lRoom);

			for(FloorPoly lPoly : lRoom.getFloorPolys())
			{
				mFloorPolys.add(lPoly);
				mIndexedPolys.put(lPoly.getUid(), lPoly);
			}
		}
		compileMap();
	}
	
	public Element writeToXml(Document iDoc)
	{
		Element lMapElement = iDoc.createElement("Map25D");
		lMapElement.setAttribute("version", "1.0");
		
		for(Room lRoom : mRooms)
		{
			Element lRoomXml = lRoom.writeToXml(iDoc);
			lMapElement.appendChild(lRoomXml);
		}
		return lMapElement;
	}
	
	public void saveRoomsToFile(String iFile) throws IOException
	{
		PrintWriter lOut = new PrintWriter(new FileWriter(iFile));

		for(Room lCurrent : mRooms)
		{
			lOut.println("ROOM");
			lCurrent.saveRoomsToFile(lOut);
			lOut.println("END_ROOM");
		}

		lOut.flush();
		lOut.close();
	}
	
	public void compileMap()
	{
		for(FloorPoly lPoly : mFloorPolys)
		{
			lPoly.compileConnections();
		}		
	}

	public void loadRoomsFromFile(String iFile) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(iFile));
		String lCurrentLine = in.readLine();

		while(lCurrentLine != null)
		{			
			Room lRoom = Room.loadRoomFromFile(this, in);
			lRoom.setParentMap(this);
			
			mTotalArea += lRoom.getArea();
			mBounds.expand(lRoom.getBounds());

			mRooms.add(lRoom);
			mIndexedRooms.put(lRoom.getUid(), lRoom);

			for(FloorPoly lPoly : lRoom.getFloorPolys())
			{
				mFloorPolys.add(lPoly);
				mIndexedPolys.put(lPoly.getUid(), lPoly);
			}

			lCurrentLine = in.readLine();
			lCurrentLine = in.readLine();
		}

		in.close();
		
		compileMap();
	}

	/**
	 * A method for checking the validity of the connectors in a map.
	 * For each connector A->B in room A, the method checks that:
	 *  * Room B exists
	 *  * There is a corresponding connector B->A in room B.
	 * @return True iff the map is consistent
	 */
	/*public boolean checkConnectorConsistency()
	{
		boolean mConsistent = true;

		for(int li = 0; li < mFloorPolys.size(); li++)
		{
			FloorPoly lA = mFloorPolys.get(li);
			Wall[] lAWalls = lA.getWalls();
			for(int lj = 0; lj < lAWalls.length; lj++)
			{
				if(lAWalls[lj] instanceof Transition)
				{
					Transition lAToB = (Transition) lAWalls[lj];
					FloorPoly lB = lAToB.getTarget();
					
					//Try and find a matching connector
					boolean lFoundMatch = false;
					System.out.println(lB.getUid());
					Wall[] lBWalls = lB.getWalls();
					for(int lk = 0; lk < lBWalls.length; lk++)
					{
						if(lBWalls[lk] instanceof Transition)
						{
							Transition lConnector = (Transition) lBWalls[lk];
							if(lConnector.getTarget() == lA)
							{
								if((lConnector.getX1() == lAToB.getX1() && lConnector.getX2() == lAToB.getX2() &&
										lConnector.getY1() == lAToB.getY1() && lConnector.getY2() == lAToB.getY2()) ||
										(lConnector.getX1() == lAToB.getX2() && lConnector.getX2() == lAToB.getX1() &&
												lConnector.getY1() == lAToB.getY2() && lConnector.getY2() == lAToB.getY1()))
								{
									lFoundMatch = true;
									break;
								}
							}
						}
					}
					if(!lFoundMatch)
					{
						System.out.println(
								"Poly "+lA+
								"\thas an unmatched transition to " + lAToB.getTarget());
						mConsistent =  false;							
					}
				}

			}
		}
		if(mConsistent)
		{
			System.out.println("Connectors are consistent");
		}
		return mConsistent;
	}*/


}
