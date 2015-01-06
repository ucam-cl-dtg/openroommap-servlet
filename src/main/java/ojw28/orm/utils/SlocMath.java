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
 * A class implementing useful maths.
 * @author Olly
 */
public class SlocMath {
	
	public static double normalDensityFunction(double iMean, double iStandardDeviation, double iValue)
	{
		double lValue = 1d/(iStandardDeviation * Math.sqrt(2*Math.PI));
		lValue *= Math.exp(-((iValue-iMean)*(iValue-iMean))/(2*iStandardDeviation*iStandardDeviation));
		return lValue;
	}
	
	/**
	 * Computes the mean of an array of samples
	 * @param iSamples The set of samples
	 * @return The mean
	 */
	public static double mean(double[] iSamples)
	{
		return mean(iSamples,0,iSamples.length);
	}

	/**
	 * Computes the mean of a subset of a double array
	 * @param iSamples The array
	 * @param iStartIndex The start index of the data to average
	 * @param iLength The length of the data to average
	 * @return
	 */
	public static double mean(double[] iSamples, int iStartIndex, int iLength)
	{
		double lAcc = 0;
		for(int li = iStartIndex; li < (iLength+iStartIndex) ; li++)
		{
			lAcc += iSamples[li];
		}
		return lAcc / iLength;
	}
	
	/**
	 * Computes the variance of an array of values.
	 * @param iSamples The values
	 * @return The variance
	 */
	public static double variance(double[] iSamples)
	{
		return variance(iSamples, 0, iSamples.length);
	}
	
	/**
	 * Computes the variance a set of values which form a subset of an array.
	 * @param iSamples The array containing the values
	 * @param iStartIndex The index of the first value
	 * @param iLength The number of values
	 * @return
	 */
	public static double variance(double[] iSamples, int iStartIndex, int iLength)
	{
		double lMean = mean(iSamples, iStartIndex, iLength);
		
		double lAcc = 0;
		for(int li = iStartIndex; li < (iLength+iStartIndex); li++)
		{
			lAcc += ((iSamples[li] - lMean)*(iSamples[li] - lMean)) / (iLength - 1d);
		}
		return lAcc;
	}
	
	/**
	 * Computes the distance between two points.
	 * @param iPoint1
	 * @param iPoint2
	 * @return
	 */
	public static double distance(float[] iPoint1, float[] iPoint2)
	{
		if(iPoint1.length != iPoint2.length)
		{
			throw new IllegalArgumentException();
		}
		float lDistance = 0;
		for(int li = 0; li < iPoint1.length; li++)
		{
			lDistance += (iPoint1[li]-iPoint2[li])*(iPoint1[li]-iPoint2[li]);
		}
		return Math.sqrt(lDistance);
	}
	
	/**
	 * Computes the distance between two points.
	 * @param iPoint1
	 * @param iPoint2
	 * @return
	 */
	public static double distance(double[] iPoint1, double[] iPoint2)
	{
		if(iPoint1.length != iPoint2.length)
		{
			throw new IllegalArgumentException();
		}
		double lDistance = 0;
		for(int li = 0; li < iPoint1.length; li++)
		{
			lDistance += (iPoint1[li]-iPoint2[li])*(iPoint1[li]-iPoint2[li]);
		}
		return Math.sqrt(lDistance);
	}
	
	/**
	 * Calculates the length of a vector
	 * @param iVector
	 * @return
	 */
	public static double length(double[] iVector)
	{
		double lLength = 0;
		for(int li = 0; li < iVector.length; li++)
		{
			lLength += iVector[li]*iVector[li];
		}
		return Math.sqrt(lLength);
	}	
	
	/**
	 * Calculates the length of a vector
	 * @param iVector
	 * @return
	 */
	public static float length(float[] iVector)
	{
		float lLength = 0;
		for(int li = 0; li < iVector.length; li++)
		{
			lLength += iVector[li]*iVector[li];
		}
		return (float) Math.sqrt(lLength);
	}	
	
	public static double dotProduct(double[] iVector, double[] iVector2)
	{
		double lDotProd = 0;
		for(int li = 0; li < iVector.length; li++)
		{
			lDotProd += iVector[li]*iVector2[li];
		}
		return lDotProd;
	}

	public static double dotProduct(float[] iVector, float[] iVector2)
	{
		double lDotProd = 0;
		for(int li = 0; li < iVector.length; li++)
		{
			lDotProd += iVector[li]*iVector2[li];
		}
		return lDotProd;
	}
	
	public static double[] crossProd(double[] iVector, double[] iVector2)
	{
		double[] lProd = new double[] {
				iVector[1]*iVector2[2] - iVector[2]*iVector2[1],
				iVector[2]*iVector2[0] - iVector[0]*iVector2[2],
				iVector[0]*iVector2[1] - iVector[1]*iVector2[0]							
		};
		return lProd;
	}

	public static float[] crossProd(float[] iVector, float[] iVector2)
	{
		float[] lProd = new float[] {
				iVector[1]*iVector2[2] - iVector[2]*iVector2[1],
				iVector[2]*iVector2[0] - iVector[0]*iVector2[2],
				iVector[0]*iVector2[1] - iVector[1]*iVector2[0]							
		};
		return lProd;
	}
	
	public static double[] normalize(double[] iVector)
	{
		double[] lNormed = new double[iVector.length];
		for(int li = 0; li < iVector.length; li++)
		{
			lNormed[li] = iVector[li] / SlocMath.length(iVector);
		}
		return lNormed;
	}
	
	public static float[] normalize(float[] iVector)
	{
		float[] lNormed = new float[iVector.length];
		for(int li = 0; li < iVector.length; li++)
		{
			lNormed[li] = iVector[li] / SlocMath.length(iVector);
		}
		return lNormed;
	}
	
//	public static double[] matrixTimesVector(Jama.Matrix iMatrix, double[] iVector)
//	{
//		Jama.Matrix lVectorMatrix = new Jama.Matrix(iVector,iVector.length);
//		Jama.Matrix lResult = iMatrix.times(lVectorMatrix);
//		return lResult.getColumnPackedCopy();
//	}

}
