/*
 *  File TextUtilities.java
 *
 *  Authors:
 *     Wolfgang Maier  <wo.maier@uni-tuebingen.de>
 *     Yannick Parmentier <parmenti@sfs.uni-tuebingen.de>
 *     
 *  Copyright:
 *     Wolfgang Maier, 2007
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:32:10 CEST 2007
 *
 *  This file is part of the TuLiPA system
 *     http://www.sfb441.uni-tuebingen.de/emmy-noether-kallmeyer/tulipa
 *
 *  TuLiPA is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TuLiPA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tuebingen.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class TextUtilities {

    static public void writeText(File file, String text)
            throws FileNotFoundException, IOException {
        Writer output = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            output = new BufferedWriter(out);
            output.write(text);
        } finally {
            if (output != null)
                output.close();
        }
    }

    // attempt to save time on String processing:
    static public String append(String init, String s) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(init);
        strBuf.append(s);
        return strBuf.toString();
    }

    static public String appendList(List<String> ls) {
        StringBuffer strBuf = new StringBuffer();
        for (String s : ls) {
            strBuf.append(s);
        }
        return strBuf.toString();
    }

    /**
     * Appends each element of that list to a single string by calling the
     * toString method of each element. Inserts the @param separator after each
     * element.
     * 
     * @param <T>
     * 
     * @param ls
     *
     * @return
     */
    static public <T> String appendList(List<T> ls, String separator) {
        StringBuffer strBuf = new StringBuffer();
        for (T s : ls) {
            strBuf.append(s.toString());
            strBuf.append(separator);
        }
        return strBuf.toString();
    }
}
