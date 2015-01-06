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
 * A position in a 2.5D map.
 * @author ojw28
 */
public class PointInMap {
	
	private float[] mPoint;
	private FloorPoly mPoly;
	
	/**
	 * Constructor. It is required that iPoint[2] == iPoly.getHeight()
	 * @param iPoint The position (x,y,z)
	 * @param iPoly The polygon to which the point is constrained
	 */
	public PointInMap(float[] iPoint, FloorPoly iPoly)
	{
		mPoint = iPoint;
		mPoly = iPoly;
	}

	/**
	 * The position (x,y,z)
	 * @return The position (x,y,z)
	 */
	public float[] getPosition()
	{
		return mPoint;
	}
	
	/**
	 * The floor polygon on which the point lies.
	 * @return The floor polygon on which the point lies
	 */
	public FloorPoly getPoly()
	{
		return mPoly;
	}
	
}
