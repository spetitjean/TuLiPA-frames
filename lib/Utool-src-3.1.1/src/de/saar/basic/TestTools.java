/*
 * @(#)TestTools.java created 23.06.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.basic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestTools {
    public static <E> Set<E> makeSet(E[] array) {
        return new HashSet<E>(Arrays.asList(array));
    }
}
