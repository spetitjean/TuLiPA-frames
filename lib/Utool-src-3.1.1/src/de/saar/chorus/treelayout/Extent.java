//
//  Extent.java
//  GecodeExplorer
//
//  Created by Marco Kuhlmann on 2005-01-17.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

package de.saar.chorus.treelayout;

/**
 * 
 * @author Marco Kuhlmann
 *
 */

public class Extent {

	public int extentL;
	public int extentR;
	
	public Extent(int pExtentL, int pExtentR) {
		extentL = pExtentL;
		extentR = pExtentR;
	}
	
	public Extent(int width) {
    int halfWidth = width / 2;
		extentL = 0 - halfWidth;
		extentR = 0 + halfWidth;
	}
  
  public void extend(int deltaL, int deltaR) {
    extentL += deltaL;
    extentR += deltaR;
  }
  
  public void move(int delta) {
    extentL += delta;
    extentR += delta;
  }
  
  public String toString() {
    return "(" + extentL + "," + extentR + ")";
  }
  
}
