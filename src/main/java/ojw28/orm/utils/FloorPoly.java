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
 * A segment of a room, represented in 2.5D. Each edge is either a Wall or a Transition
 * to a neighbouring FloorPoly, which can be in the same Room, or in a different neighbouring
 * room.
 * @author ojw28
 */
public class FloorPoly extends PlanarPoly {
	
	private Room mParentRoom;

	private int mUid;
	
	private Integer[] mConnectionIds;
	private FloorPoly[] mConnections;
	
	private Furniture[] mFurniture;
		
	/**
	 * Constructor.
	 * @param iUid The floor polygon's unique identifier
	 * @param iVertices The vertices which define the polygon, as a packed array (x0,y0,z0,x1,y1,z1 . . . xN,yN,zN)
	 * @param iConnections The connections to other floor polygons, as an array (c1,c2. . .cN). If there is no connection
	 * @param iFurniture Furniture which overlaps the floor polygon
	 * on the edge (xI,yI,zI)->(x[I+1],y[I+1],z[I+1]) then cI should be set to null
	 */
	public FloorPoly(int iUid, float[] iVertices, Integer[] iConnections, Furniture[] iFurniture)
	{
		super(iVertices);
		mUid = iUid;
		mConnectionIds = iConnections;
		mFurniture = iFurniture;
	}
	
	/**
	 * Constructor.
	 * @param iUid The floor polygon's unique identifier
	 * @param iVertices The vertices which define the polygon, as a packed array (x0,y0,z0,x1,y1,z1 . . . xN,yN,zN)
	 * @param iConnections The connections to other floor polygons, as an array (c1,c2. . .cN). If there is no connection
	 * on the edge (xI,yI,zI)->(x[I+1],y[I+1],z[I+1]) then cI should be set to null
	 */
	public FloorPoly(int iUid, float[] iVertices, Integer[] iConnections)
	{
		super(iVertices);
		mUid = iUid;
		mConnectionIds = iConnections;
	}
	
	/**
	 * Sets the room to which this floorpoly belongs.
	 * @param iRoom The parent room
	 */
	void setParentRoom(Room iRoom)
	{
		mParentRoom = iRoom;
	}
	
	/**
	 * Compiles the connections to neighbouring floor polygons.
	 */
	public void compileConnections()
	{
		mConnections = new FloorPoly[mConnectionIds.length];
		for(int li = 0; li < mConnections.length; li++)
		{
			if(mConnectionIds[li] != null)
			{
				FloorPoly lTarget = mParentRoom.getParentMap().getFloorPoly(mConnectionIds[li]);
				if(lTarget == null)
				{
					//Missing the target (i.e. the target is not part of the map). Treat as a wall.
				}
				mConnections[li] = lTarget;
			}
		}
	}
	
	/**
	 * Computes the position along P1->P2 at which an intersection with a wall W1->W2 occurs.
	 * The return value is the value in the range [0,1], where 0 indicates that there is
	 * an intersection at P1 and 1 indicates an intersection at P2. A return value of -1
	 * indicates that there is no intersection.
	 * @param iWallSegment The index of the vertex W1
	 * @param iP1 The start position P1
	 * @param iP2 The end position P2
	 * @return The intersection position in the range [0,1], or -1 if no intersection is found
	 */
	public float getIntersectionPosition(int iWallSegment, float[] iP1, float[] iP2)
	{	
		float mMinX = Math.min(mVertices[iWallSegment * 3], mVertices[((iWallSegment + 1) * 3) % mVertices.length]);
		float mMinY = Math.min(mVertices[iWallSegment * 3 + 1], mVertices[((iWallSegment + 1) * 3) % mVertices.length + 1]);
		float mMaxX = Math.max(mVertices[iWallSegment * 3], mVertices[((iWallSegment + 1) * 3) % mVertices.length]);
		float mMaxY = Math.max(mVertices[iWallSegment * 3 + 1], mVertices[((iWallSegment + 1) * 3) % mVertices.length + 1]);
		float mDiffX = mVertices[((iWallSegment + 1) * 3) % mVertices.length] - mVertices[iWallSegment * 3];
		float mDiffY = mVertices[((iWallSegment + 1) * 3) % mVertices.length + 1] - mVertices[iWallSegment * 3 + 1];
		
		if((iP1[0] < mMinX && iP2[0] < mMinX) ||
			(iP1[0] > mMaxX && iP2[0] > mMaxX) ||
			(iP1[1] < mMinY && iP2[1] < mMinY) ||
			(iP1[1] > mMaxY && iP2[1] > mMaxY))
		{
			//No intersection
			return -1;
		}
		
		float lDiffX = iP2[0]-iP1[0];
		float lDiffY = iP2[1]-iP1[1];
		float lDenominator = lDiffY*mDiffX - lDiffX*mDiffY;
		
		if(lDenominator == 0)
		{
			//Lines are parallel
			return -1;
		}
		
		float lDiffX2 = mVertices[iWallSegment * 3]-iP1[0];
		float lDiffY2 = mVertices[iWallSegment * 3 + 1]-iP1[1];
		float lNumeratorA = lDiffX*lDiffY2 - lDiffY*lDiffX2;
		float lNumeratorB = mDiffX*lDiffY2 - mDiffY*lDiffX2; 
		
		if(lDenominator < 0)
		{
			if(lNumeratorA > 0 || lNumeratorB > 0 || lNumeratorA < lDenominator || lNumeratorB < lDenominator)
			{
				//Segments do not overlap
				return -1;
			}
		}
		else if(lNumeratorA < 0 || lNumeratorB < 0 || lNumeratorA > lDenominator || lNumeratorB > lDenominator)
		{
			//Segments do not overlap
			return -1;
		}

		return lNumeratorB/lDenominator;
		
		//This is the actual point of intersection!
		//float lUb = lNumeratorB/lDenominator;
		//return new float[] {iP1[0] + lUb*(iP2[0] - iP1[0]),iP1[1] + lUb*(iP2[1] - iP1[1])};
	}
	
	/**
	 * The connections to other floor polygons as an array (c1,c2, . . . cN). cI is the floor polygon
	 * which is connected to the edge (xI,yI,zI)->(x[I+1],y[I+1],z[I+1]), or null if the edge is a wall.
	 * @param The index I of the first vertex of the wall.
	 * @return The connections
	 */
	public FloorPoly getConnection(int iWallIndex)
	{
		return mConnections[iWallIndex];
	}	
			
	/**
	 * The room to which this floor polygon belongs.
	 * @return The parent room
	 */
	public Room getContainingRoom()
	{
		return mParentRoom;
	}
	
	public Furniture[] getFurniture()
	{
		return mFurniture;
	}
	
	/**
	 * Get the unique polygon identifier.
	 * @return The polygon uid
	 */
	public int getUid()
	{
		return mUid;
	}

	/**
	 * Returns a string of the form ROOM_NAME : POLY_UID
	 */
	public String toString()
	{
		return mParentRoom.getName()+" : "+mUid;
	}
		
	public Element writeToXml(Document iDoc)
	{
		Element lRoomElement = iDoc.createElement("Poly");
		lRoomElement.setAttribute("uid", ""+mUid);			
		
		for(int li = 0; li < mVertices.length; li+=3)
		{
			Element lVertex = iDoc.createElement("Vertex");
			lVertex.setAttribute("x", ""+mVertices[li]);
			lVertex.setAttribute("y", ""+mVertices[li+1]);
			lVertex.setAttribute("z", ""+mVertices[li+2]);
			if(mConnectionIds[li/3] != null)
			{
				lVertex.setAttribute("edgetype", "connector");
				lVertex.setAttribute("target", ""+mConnectionIds[li/3]);
			}
			else
			{
				lVertex.setAttribute("edgetype", "wall");				
			}
			
			lRoomElement.appendChild(lVertex);
		}
		
		return lRoomElement;
	}
	
	public static FloorPoly readFromXml(Element iElement)
	{
		int lUid = Integer.parseInt(iElement.getAttribute("uid"));

		NodeList lVertexNodes = iElement.getElementsByTagName("Vertex");
		float[] lVertices = new float[lVertexNodes.getLength() * 3];
		Integer[] lConnections = new Integer[lVertexNodes.getLength()];
		
		for(int li = 0; li < lVertexNodes.getLength(); li++)
		{
			Element lVertexXml = (Element) lVertexNodes.item(li);
			lVertices[li * 3] = Float.parseFloat(lVertexXml.getAttribute("x"));
			lVertices[li * 3 + 1] = Float.parseFloat(lVertexXml.getAttribute("y"));
			lVertices[li * 3 + 2] = Float.parseFloat(lVertexXml.getAttribute("z"));
			
			if(lVertexXml.getAttribute("edgetype").equals("connector"))
			{
				lConnections[li] = Integer.parseInt(lVertexXml.getAttribute("target"));
			}
		}
		
		makeClockwise(lVertices,lConnections);

		FloorPoly lFloorPoly = new FloorPoly(lUid, lVertices, lConnections);
		return lFloorPoly;
	}
	
	/**
	 * Write the room description out to file.
	 * @param iOutputStream The file output stream
	 * @throws IOException If an IOException occurs
	 */
	public void writePolyToFile(PrintWriter iOutputStream) throws IOException
	{
		iOutputStream.println("    Uid\t"+mUid);
		iOutputStream.println("    Vertex count\t"+getVertices().length/3);
		for(int li = 0; li < mVertices.length; li+=3)
		{
			if(mConnections[li/3] != null)
			{
				iOutputStream.println("    "+mVertices[li]+"\t"+mVertices[li+1]+"\t"+mVertices[li+2]+"\tConnector\t"+mConnections[li/3].getUid());
			}
			else
			{
				iOutputStream.println("    "+mVertices[li]+"\t"+mVertices[li+1]+"\t"+mVertices[li+2]+"\tWall");				
			}
		}
	}

	/**
	 * Construct a floor polygon from a description contained withing a file.
	 * @param iMap The map to which this polygon will belong
	 * @param iParent The parent room to which this polygon will belong
	 * @param iReader The file stream from which to read the polygon
	 * @return The constructed polygon
	 * @throws IOException If an IOException occurs
	 */
	public static FloorPoly loadPolyFromFile(Map25D iMap, BufferedReader iReader) throws IOException
	{
		int lUid = Integer.parseInt(iReader.readLine().trim().split("\t")[1]);
		int lNoVertices = Integer.parseInt(iReader.readLine().trim().split("\t")[1]);

		float[] lVertices = new float[lNoVertices * 3];
		Integer[] lConnections = new Integer[lNoVertices];
		for(int li = 0; li < lVertices.length; li+=3)
		{
			String[] lSplit = iReader.readLine().trim().split("\t");
			
			lVertices[li] = java.lang.Float.parseFloat(lSplit[0]);
			lVertices[li + 1] = java.lang.Float.parseFloat(lSplit[1]);
			lVertices[li + 2] = java.lang.Float.parseFloat(lSplit[2]);
			
			if(lSplit[3].toLowerCase().equals("connector"))
			{
				int lTransitionTargetUid = Integer.parseInt(lSplit[4]);
				lConnections[li/3] = lTransitionTargetUid;
			}
		}
		
		makeClockwise(lVertices,lConnections);

		FloorPoly lFloorPoly = new FloorPoly(lUid, lVertices, lConnections);
		return lFloorPoly;
	}
	
	/**
	 * Makes sure that the vertices of a polygon are specified in clockwise order. If they
	 * are not then the order is reverse. The connectors are also updated so that they remain
	 * consistent with the reversed order.
	 * @param iVertices The vertices
	 * @param iConnectors The connectors
	 */
	private static void makeClockwise(float[] iVertices, Integer[] iConnectors)
	{
		if(!isClockwise(iVertices))
		{
			System.out.println("WARNING\tpolygon vertices not in clockwise order - repaired");
			
			float[] lTemp = new float[iVertices.length];
			for(int li = 0; li < iVertices.length; li+= 3)
			{
				lTemp[li] = iVertices[iVertices.length - li - 3];
				lTemp[li + 1] = iVertices[iVertices.length - li - 2];
				lTemp[li + 2] = iVertices[iVertices.length - li - 1];
			}
			
			int[] lTempConnections = new int[iConnectors.length];
			for(int li = 0; li < iConnectors.length - 1; li++)
			{
				lTempConnections[li] = iConnectors[iConnectors.length - li - 2];
			}
			lTempConnections[iConnectors.length - 1] = iConnectors[iConnectors.length - 1];
			
			for(int li = 0; li < iVertices.length; li+=3)
			{
				iVertices[li] = lTemp[li];
				iVertices[li + 1] = lTemp[li + 1];
				iVertices[li + 2] = lTemp[li + 2];
				
				iConnectors[li/3] = lTempConnections[li/3];
			}
		}
	}

	/**
	 * Internal method used after the poly is constructed.
	 * @return The surface area of the floor poly
	 */
	private static boolean isClockwise(float[] iVertices)
	{
		double lArea = 0;
		for(int li = 0; li < iVertices.length-3; li+=3)
		{
			lArea += (iVertices[li]*iVertices[li+4] - iVertices[li + 1]*iVertices[li+3]);
		}
		lArea += (iVertices[iVertices.length-3]*iVertices[1] - iVertices[iVertices.length-2]*iVertices[0]);
		return lArea < 0;
	}
	
	public int hashCode()
	{
		return mUid;
	}
	
}