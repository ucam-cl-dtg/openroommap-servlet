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

public class ItemDef {

	private ItemDefPoly[] mPolys;
	
	private float mHeight;
	private boolean mFlipable;
	private boolean mMovable;
	private int mDefId;
	private String mName;
	private String mDescription;
	private String mImageFile;
	private String mCategory;
	private String mFieldLabel;
	private int mOrdering;
	
	public ItemDef(int iDefId, String iName, int iOrdering, String iCategory, String iImageFile, String iDescription, String iFieldLabel, boolean iFlipable, boolean iMovable, float iHeight, ItemDefPoly[] iPolys)
	{
		mDefId = iDefId;
		mName = iName;
		mFieldLabel = iFieldLabel;
		mCategory = iCategory;
		mImageFile = iImageFile;
		mHeight = iHeight;
		mDescription = iDescription;
		mFlipable = iFlipable;
		mMovable = iMovable;
		mPolys = iPolys;
		mOrdering = iOrdering;
	}
	
	public ItemDef()
	{
		
	}
	
	public ItemDefPoly[] getPolys()
	{
		return mPolys;
	}
	
	public int getItemDefId()
	{
		return mDefId;
	}
	
	public String getName()
	{
		return mName;
	}

	public String getCategory()
	{
		return mCategory;
	}
	
	public float getHeight()
	{
		return mHeight;
	}
	
	public int getOrdering()
	{
		return mOrdering;
	}
	
	public boolean isFlipable()
	{
		return mFlipable;
	}

	public boolean isMovable()
	{
		return mMovable;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public String getImageFile()
	{
		return mImageFile;
	}
	
	public String getFieldLabel()
	{
		return mFieldLabel;
	}

	public void writeToXml(Document iDoc, Element iParent)
	{
		Element lFurnitureElement = iDoc.createElement("Poly");
		lFurnitureElement.setAttribute("item_def_id",""+mDefId);
		lFurnitureElement.setAttribute("name", ""+mName);	
		lFurnitureElement.setAttribute("ordering", ""+mOrdering);	
		lFurnitureElement.setAttribute("category", ""+mCategory);	
		lFurnitureElement.setAttribute("image_file", ""+mImageFile);		
		lFurnitureElement.setAttribute("description", ""+mDescription);		
		lFurnitureElement.setAttribute("height", ""+mHeight);		
		lFurnitureElement.setAttribute("flipable", ""+mFlipable);
		lFurnitureElement.setAttribute("movable", ""+mMovable);
		lFurnitureElement.setAttribute("field_label", ""+mFieldLabel);
		
		for(int li = 0; li < mPolys.length; li++)
		{
			mPolys[li].writeToXml(iDoc, lFurnitureElement);
		}
		iParent.appendChild(lFurnitureElement);
	}
	
	public void readFromXml(Element iElement)
	{
		mDefId = Integer.parseInt(iElement.getAttribute("id"));
		mName = iElement.getAttribute("name");
		mCategory = iElement.getAttribute("category");
		mOrdering = Integer.parseInt(iElement.getAttribute("ordering"));
		mImageFile = iElement.getAttribute("image_file");
		mDescription = iElement.getAttribute("description");
		mHeight = Float.parseFloat(iElement.getAttribute("height"));
		mFlipable = Boolean.parseBoolean(iElement.getAttribute("flipable"));
		mMovable = iElement.hasAttribute("movable") ? Boolean.parseBoolean(iElement.getAttribute("movable")) : true;
		mFieldLabel = iElement.getAttribute("field_label");
		
		NodeList lPolyNodes = iElement.getElementsByTagName("Poly");
		mPolys = new ItemDefPoly[lPolyNodes.getLength()];
		for(int li = 0; li < lPolyNodes.getLength(); li++)
		{
				mPolys[li] = new ItemDefPoly();
				mPolys[li].readFromXml((Element) lPolyNodes.item(li));
		}
	}
	
}
