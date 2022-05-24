/*
 *  File CatPairs.java
 *
 *  Authors:
 *     Yannick Parmentier  <parmenti@sfs.uni-tuebingen.de>
 *
 *  Copyright:
 *     Yannick Parmentier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 11:09:47 CEST 2007
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
package de.tuebingen.converter;

public class CatPairs {

    private String topCat;
    private String botCat;

    public CatPairs(String t, String b) {
        topCat = t;
        botCat = b;
    }

    public CatPairs(CatPairs cp) {
        topCat = new String(cp.getTopCat());
        botCat = new String(cp.getBotCat());
    }

    public String getTopCat() {
        return topCat;
    }

    public void setTopCat(String topCat) {
        this.topCat = topCat;
    }

    public String getBotCat() {
        return botCat;
    }

    public void setBotCat(String botCat) {
        this.botCat = botCat;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    public String toString() {
        return "top category: " + topCat + ", bot category: " + botCat;
    }

}
