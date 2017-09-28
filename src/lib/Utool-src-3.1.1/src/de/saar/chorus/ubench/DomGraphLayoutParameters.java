/*
 * @(#)DomGraphLayoutParameters.java created 17.04.2005
 * 
 * Copyright (c) 2005 Alexander Koller
 *  
 */

package de.saar.chorus.ubench;

/**
 * Storing the general parameters (distances between
 * nodes and fragments).
 * 
 * @author Alexander Koller
 *
 */
public class DomGraphLayoutParameters {
    //  x/y distance between siblings within a fragment   
    public static final int nodeXDistance = 15;  
    public static final int nodeYDistance = 15;
    
    // x/y distances between different fragments
    public static final int fragmentXDistance = 30;
    public static final int fragmentYDistance = 100;
    
    // x distance between towers of the same fragment
    public static final int towerXDistance = 30;
}
