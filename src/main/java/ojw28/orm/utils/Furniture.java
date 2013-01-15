package ojw28.orm.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Furniture extends PlanarPoly {
	
	private String mName;
	private String mDescription = "This is a furniture description describing something about the furniture (duh)";
	
	/**
	 * Constructor.
	 * @param iVertices The vertices which define the polygon, as a packed array (x0,y0,z0,x1,y1,z1 . . . xN,yN,zN)
	 */
	public Furniture(String iName, float[] iVertices)
	{
		super(iVertices);
		mName = iName;
	}

	public void writeToXml(Document iDoc, Element iParent)
	{
		Element lFurnitureElement = iDoc.createElement("Poly");
		lFurnitureElement.setAttribute("name", ""+mName);		
		lFurnitureElement.setAttribute("description", ""+mDescription);			
		
		for(int li = 0; li < mVertices.length; li+=3)
		{
			Element lVertex = iDoc.createElement("Vertex");
			lVertex.setAttribute("x", ""+mVertices[li]);
			lVertex.setAttribute("y", ""+mVertices[li+1]);
			lFurnitureElement.appendChild(lVertex);
		}
		iParent.appendChild(lFurnitureElement);
	}
	
}
