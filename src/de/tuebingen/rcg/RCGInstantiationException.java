/*
 *  File RCGInstantiationException.java
 *
 *  Authors:
 *     Wolfgang Maier <wo.maier@uni-tuebingen.de>
 *
 *  Copyright:
 *     Wolfgang Maier, 2007
 *
 *  Last modified:
 *     Di 16. Okt 09:57:06 CEST 2007
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
package de.tuebingen.rcg;

/**
 * Thrown on instantiation failures of RangeLists.
 *
 * @author wmaier
 */
public class RCGInstantiationException extends RCGException {

    public String getMessage() {
        return super.getMessage();
    }

    private static final long serialVersionUID = 1L;

    public RCGInstantiationException() {
        super();
    }

    public RCGInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RCGInstantiationException(String message) {
        super(message);
    }

    public RCGInstantiationException(Throwable cause) {
        super(cause);
    }


}
