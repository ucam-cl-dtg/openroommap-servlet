package ojw28.orm.utils;

import java.util.Random;


/**
 * A segment of a room, represented as a planar polygon. Each edge is either a Wall or a Transition
 * to a neighbouring FloorPoly, which can be in the same Room, or in a different neighbouring
 * room.
 * @author ojw28
 */
public class PlanarPoly {
	
	protected static Random mRand = new Random();
	
	protected float[] mVertices;
	
	private float mArea;
	private float[] mCentroid;
	private float[] mNormalVector;
	
	private Bounds3D mBounds;
	
	private boolean mIsFlat = false;
	
	/**
	 * Constructor.
	 * @param iVertices The vertices as a packed array (x0,y0,z0,x1,y1,z1 . . . xN,yN,zN)
	 */
	public PlanarPoly(float[] iVertices)
	{
		setupPoly(iVertices);
	}
	
	/**
	 * Defines the polygon.
	 * @param iVertices The vertices as a packed array (x0,y0,z0,x1,y1,z1 . . . xN,yN,zN)
	 */
	public void setupPoly(float[] iVertices)
	{		
		float[] lBounds = new float[6];
		lBounds[0] = Float.MAX_VALUE;
		lBounds[1] = Float.MAX_VALUE;
		lBounds[2] = Float.MAX_VALUE;
		lBounds[3] = -Float.MAX_VALUE;
		lBounds[4] = -Float.MAX_VALUE;
		lBounds[5] = -Float.MAX_VALUE;
				
		mVertices = iVertices;
			
		computeNormalVector();
		
		for(int li = 0; li < iVertices.length; li+=3)
		{
			lBounds[0] = Math.min((float) iVertices[li], lBounds[0]);
			lBounds[1] = Math.min((float) iVertices[li + 1], lBounds[1]);
			lBounds[2] = Math.min((float) iVertices[li + 2], lBounds[2]);
			lBounds[3] = Math.max((float) iVertices[li], lBounds[3]);
			lBounds[4] = Math.max((float) iVertices[li + 1], lBounds[4]);
			lBounds[5] = Math.max((float) iVertices[li + 2], lBounds[5]);
		}	
		
		mBounds = new Bounds3D(lBounds);
		
		mArea = computeArea();
		mCentroid = computeCentroid();
		
		if(mBounds.getMinZ() == mBounds.getMaxZ())
		{
			mIsFlat = true; //Enable flat optimisations
		}
	}
		
	/**
	 * Get the height of the floor polygon at a specified (x,y) position
	 * @param iPosition An (x,y) position that falls within the polygon in the (x,y) plane.
	 * @return The height of the polygon at (x,y)
	 */
	public float getHeight(float[] iPosition)
	{
		if(mIsFlat)
		{
			return mVertices[2];
		}
		else
		{
			float[] lTop = new float[] {mVertices[0] - iPosition[0],mVertices[1] - iPosition[1],mVertices[2]};
			float[] lBot = new float[] {0,0,1};
			float lU = (float) (SlocMath.dotProduct(mNormalVector, lTop)/SlocMath.dotProduct(mNormalVector, lBot));
			return lU;
		}
	}
	
	/**
	 * The centroid of the polygon.
	 * @return The centroid (x,y,z)
	 */
	public float[] getCentroid()
	{
		return mCentroid;
	}
	
	/**
	 * The vertices which define the polygon, as a packed array
	 * (x0,y0,z0,x1,y1,z1 . . .xN,yN,zN). Note that z0 = z1 = ... zN = getHeight().
	 * The z values are included so that the array can be used by rendering
	 * algorithms without having to copy the data.
	 * @return The packed vertex array
	 */
	public float[] getVertices()
	{
		return mVertices;
	}

	/**
	 * Get the area of the floor polygon.
	 * @return The surface area
	 */
	public float getArea()
	{
		return mArea;
	}
	
	/**
	 * Get a random point which falls inside the polygon.
	 * @return A random point (x,y,z) which lies on the surface of the polygon
	 */
	public float[] getRandomPosition2D()
	{
		float[] lPoint = new float[3];
		while(true)
		{
			lPoint[0] = mRand.nextFloat()*(mBounds.getMaxX() - mBounds.getMinX()) + mBounds.getMinX();
			lPoint[1] = mRand.nextFloat()*(mBounds.getMaxY() - mBounds.getMinY()) + mBounds.getMinY();
			lPoint[2] = getHeight(lPoint);
			if(isInside2D(lPoint))
			{
				return lPoint;
			}
		}
	}
		
	/**
	 * Gets the bounds of the polygon.
	 * @return The polygon bounds
	 */
	public Bounds3D getBounds()
	{
		return mBounds;
	}

	/**
	 * Tests whether the specified position (x,y) lies on the polygon's surface in the x,y plane
	 * @param iPosition The position (x,y). A position (x,y,z) can be used, where z is ignored.
	 * @return True iff (x,y) lies on the polygon's surface in the x,y plane
	 */
	public boolean isInside2D(float[] iPosition)
	{
		int lCrossings = 0;
		for(int li = 0; li < mVertices.length - 3; li+=3)
		{
			lCrossings += pointCrossingsForLine(iPosition,
					mVertices[li],mVertices[li + 1],
					mVertices[li + 3],mVertices[li + 4]);
		}
		lCrossings += pointCrossingsForLine(iPosition,
				mVertices[mVertices.length - 3],mVertices[mVertices.length - 2],
				mVertices[0],mVertices[1]);
		return lCrossings % 2 == 1;
	}

    /**
     * Calculates the number of times the line from (x0,y0) to (x1,y1)
     * crosses the ray extending to the right from (iPosition[0],iPosition[1]).
     * If the point lies on the line, then no crossings are recorded.
     * +1 is returned for a crossing where the Y coordinate is increasing
     * -1 is returned for a crossing where the Y coordinate is decreasing
     */
    private int pointCrossingsForLine(float[] iPosition,
                                            double x0, double y0,
                                            double x1, double y1)
    {
        if (iPosition[1] <  y0 && iPosition[1] <  y1) return 0;
        if (iPosition[1] >= y0 && iPosition[1] >= y1) return 0;
        if (iPosition[0] >= x0 && iPosition[0] >= x1) return 0;
        if (iPosition[0] <  x0 && iPosition[0] <  x1) return 1;
        double xintercept = x0 + (iPosition[1] - y0) * (x1 - x0) / (y1 - y0);
        if (iPosition[0] >= xintercept) return 0;
        return 1;
    }
    
	/**
	 * Internal method used to compute the area of the polygon after it is constructed.
	 * @return The surface area of the polygon
	 */
	private float computeArea()
	{
		float[] lAccumulation = new float[3];
		
		float lArea = 0;
		for(int li = 0; li < mVertices.length-3; li+=3)
		{
			float[] lVi = new float[] {mVertices[li],mVertices[li+1],mVertices[li+2]};
			float[] lVj = new float[] {mVertices[li+3],mVertices[li+4],mVertices[li+5]};
			float[] lCp = SlocMath.crossProd(lVi, lVj);
			lAccumulation[0] += lCp[0];
			lAccumulation[1] += lCp[1];
			lAccumulation[2] += lCp[2];
		}
		float[] lVi = new float[] {mVertices[mVertices.length-3],mVertices[mVertices.length-2],mVertices[mVertices.length-1]};
		float[] lVj = new float[] {mVertices[0],mVertices[1],mVertices[2]};
		float[] lCp = SlocMath.crossProd(lVi, lVj);
		lAccumulation[0] += lCp[0];
		lAccumulation[1] += lCp[1];
		lAccumulation[2] += lCp[2];
		
		lArea = (float) SlocMath.dotProduct(mNormalVector, lAccumulation);
				
		return Math.abs(lArea / 2);
	}

	/**
	 * Internal method used to compute the centroid of the polygon after it is constructed.
	 * @return The centroid of the polygon
	 */
	private float[] computeCentroid()
	{
		float lAcc = 0;
		float[] lCentroidAcc = new float[3];
		for(int li = 0; li < mVertices.length-3; li+=3)
		{
			float lArea = computeTriangleArea(li);
			if(lArea != 0)
			{
				lAcc += lArea;
				float[] lCentroid = computeTriangleCentroid(li);
				lCentroidAcc[0] += lArea*lCentroid[0];
				lCentroidAcc[1] += lArea*lCentroid[1];
				lCentroidAcc[2] += lArea*lCentroid[2];
			}
		}
		lCentroidAcc[0] /= lAcc;
		lCentroidAcc[1] /= lAcc;
		lCentroidAcc[2] /= lAcc;
		return lCentroidAcc;
	}
	
	/**
	 * Computes the area of the triangle [v_0,v_iIndex,v_(iIndex+1)]
	 * @param iIndex The index of the second vertex
	 * @return The area of the triangle
	 */
	private float computeTriangleArea(int iIndex)
	{
		float[] lFirstEdge = new float[3];
		lFirstEdge[0] = mVertices[iIndex] - mVertices[0];
		lFirstEdge[1] = mVertices[iIndex+1] - mVertices[1];
		lFirstEdge[2] = mVertices[iIndex+2] - mVertices[2];
		float[] lLastEdge = new float[3];
		lLastEdge[0] = mVertices[iIndex+3] - mVertices[0];
		lLastEdge[1] = mVertices[iIndex+4] - mVertices[1];
		lLastEdge[2] = mVertices[iIndex+5] - mVertices[2];
		
		float[] lCrossProd = SlocMath.crossProd(lFirstEdge, lLastEdge);
		return SlocMath.length(lCrossProd)/2f;
	}
	
	/**
	 * Computes the centroid of the triangle [v_0,v_iIndex,v_(iIndex+1)]
	 * @param iIndex The index of the second vertex
	 * @return The centroid of the triangle
	 */
	private float[] computeTriangleCentroid(int iIndex)
	{
		float[] lC = new float[3];
		lC[0] += mVertices[0];
		lC[1] += mVertices[1];
		lC[2] += mVertices[2];
		lC[0] += mVertices[iIndex+0];
		lC[1] += mVertices[iIndex+1];
		lC[2] += mVertices[iIndex+2];
		lC[0] += mVertices[iIndex+3];
		lC[1] += mVertices[iIndex+4];
		lC[2] += mVertices[iIndex+5];
		lC[0] /= 3f;
		lC[1] /= 3f;
		lC[2] /= 3f;
		return lC;
	}
	
	private void computeNormalVector()
	{
		float[] lFirstEdge = new float[3];
		lFirstEdge[0] = mVertices[3] - mVertices[0];
		lFirstEdge[1] = mVertices[4] - mVertices[1];
		lFirstEdge[2] = mVertices[5] - mVertices[2];
		float[] lLastEdge = new float[3];
		int li = 0;
		lLastEdge[0] = mVertices[mVertices.length-3] - mVertices[0];
		lLastEdge[1] = mVertices[mVertices.length-2] - mVertices[1];
		lLastEdge[2] = mVertices[mVertices.length-1] - mVertices[2];
		while(SlocMath.length(SlocMath.crossProd(lFirstEdge, lLastEdge)) == 0)
		{
			//coplanar
			li++;
			lLastEdge[0] = mVertices[mVertices.length-3-(3*li)] - mVertices[0];
			lLastEdge[1] = mVertices[mVertices.length-2-(3*li)] - mVertices[1];
			lLastEdge[2] = mVertices[mVertices.length-1-(3*li)] - mVertices[2];
		}
		mNormalVector = SlocMath.normalize(SlocMath.crossProd(lFirstEdge, lLastEdge));
	}
	
}