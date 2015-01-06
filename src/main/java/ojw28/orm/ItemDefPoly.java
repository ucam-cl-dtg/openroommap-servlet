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
package ojw28.orm;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ItemDefPoly {
	
	//The polygon's vertices as a packed array (x1,y1,x2,y2...xN,yN)
	private float[] mVertices;
	
	private int mFillColour;
	private float mFillAlpha;
	private int mEdgeColour;
	private float mEdgeAlpha;
	
	public ItemDefPoly(float[] iVertices, int iFillColour, float iFillAlpha, int iEdgeColour, float iEdgeAlpha)
	{
		mVertices = iVertices;
		mFillColour = iFillColour;
		mFillAlpha = iFillAlpha;
		mEdgeColour = iEdgeColour;
		mEdgeAlpha = iEdgeAlpha;
	}
	
	public ItemDefPoly()
	{
		
	}
	
	public int getFillColour()
	{
		return mFillColour;
	}
	
	public int getEdgeColour()
	{
		return mEdgeColour;
	}
	
	public float getFillAlpha()
	{
		return mFillAlpha;
	}
	
	public float getEdgeAlpha()
	{
		return mEdgeAlpha;
	}
	
	public float[] getVertices()
	{
		return mVertices;
	}
	
	public void writeToXml(Document iDoc, Element iParent)
	{
		Element lFurnitureElement = iDoc.createElement("Poly");
		lFurnitureElement.setAttribute("fill_colour", "0x"+Integer.toHexString(mFillColour));	
		lFurnitureElement.setAttribute("fill_alpha", ""+mFillAlpha);		
		lFurnitureElement.setAttribute("edge_colour", "0x"+Integer.toHexString(mEdgeColour));		
		lFurnitureElement.setAttribute("edge_alpha", ""+mEdgeAlpha);
		for(int li = 0; li < mVertices.length; li+=2)
		{
			Element lVertex = iDoc.createElement("Vertex");
			lVertex.setAttribute("x", ""+mVertices[li]);
			lVertex.setAttribute("y", ""+mVertices[li+1]);
			lFurnitureElement.appendChild(lVertex);
		}
		iParent.appendChild(lFurnitureElement);
	}

	public void readFromXml(Element iElement)
	{
		mFillColour = Integer.parseInt(iElement.getAttribute("fill_colour").substring(2),16);
		mFillAlpha = Float.parseFloat(iElement.getAttribute("fill_alpha"));
		mEdgeColour = Integer.parseInt(iElement.getAttribute("edge_colour").substring(2),16);
		mEdgeAlpha = Float.parseFloat(iElement.getAttribute("edge_alpha"));
		
		NodeList lVertexNodes = iElement.getElementsByTagName("Vertex");
		mVertices = new float[lVertexNodes.getLength()*2];
		for(int li = 0; li < lVertexNodes.getLength(); li++)
		{
			Element lVertexElement = (Element) lVertexNodes.item(li);
			mVertices[li*2] = Float.parseFloat(lVertexElement.getAttribute("x"));
			mVertices[li*2 + 1] = Float.parseFloat(lVertexElement.getAttribute("y"));
		}
	}
}
