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

/**
 * A 3D bounding box class.
 * @author ojw28
 */
public class Bounds3D {
	
	//(xMin,yMin,zMin,xMax,yMax,zMax)
	private float[] mBounds = new float[6];
	
	/**
	 * Constructor.
	 */
	public Bounds3D()
	{
		reset();
	}
	
	public Bounds3D(float[] iBounds)
	{
		mBounds = iBounds;
	}
	
	public void reset()
	{
		mBounds[0] = Float.MAX_VALUE;
		mBounds[1] = Float.MAX_VALUE;
		mBounds[2] = Float.MAX_VALUE;
		mBounds[3] = -Float.MAX_VALUE;
		mBounds[4] = -Float.MAX_VALUE;
		mBounds[5] = -Float.MAX_VALUE;		
	}
	
	/**
	 * Expand the bounds to hold a polygon.
	 * @param iPoly The polygon
	 */
	public void expand(PlanarPoly iPoly)
	{
		expand(iPoly.getBounds());
	}
	
	public void expand(Bounds3D iBounds)
	{
		mBounds[0] = Math.min(mBounds[0], iBounds.getMinX());
		mBounds[1] = Math.min(mBounds[1], iBounds.getMinY());
		mBounds[2] = Math.min(mBounds[2], iBounds.getMinZ());
		mBounds[3] = Math.max(mBounds[3], iBounds.getMaxX());
		mBounds[4] = Math.max(mBounds[4], iBounds.getMaxY());
		mBounds[5] = Math.max(mBounds[5], iBounds.getMaxZ());
	}
	
	/**
	 * The lower X bound.
	 * @return The lower X bound
	 */
	public float getMinX()
	{
		return mBounds[0];
	}

	/**
	 * The upper X bound.
	 * @return The upper X bound
	 */
	public float getMaxX()
	{
		return mBounds[3];
	}

	/**
	 * The lower Y bound.
	 * @return The lower Y bound
	 */
	public float getMinY()
	{
		return mBounds[1];
	}

	/**
	 * The upper Y bound.
	 * @return upper lower Y bound
	 */
	public float getMaxY()
	{
		return mBounds[4];
	}

	/**
	 * The lower Z bound.
	 * @return The lower Z bound
	 */
	public float getMinZ()
	{
		return mBounds[2];
	}

	/**
	 * The upper X bound.
	 * @return The upper X bound
	 */
	public float getMaxZ()
	{
		return mBounds[5];
	}
	
	/**
	 * The bounds as an array.
	 * @return (xMin,yMin,zMin,xMax,yMax,zMax)
	 */
	public float[] getBounds()
	{
		return mBounds;
	}

}
