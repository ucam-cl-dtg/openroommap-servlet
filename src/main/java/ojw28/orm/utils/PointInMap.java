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
