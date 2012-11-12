/*-
 * Copyright (C) 2006 Erik Larsson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catacombae.hfs.types.finder;

import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.util.Util;
import java.io.PrintStream;
import org.catacombae.csjc.StructElements;

/** This class was generated by CStructToJavaClass. */
public class ExtendedFileInfo extends CommonExtendedFinderInfo implements StructElements {
    /*
     * struct ExtendedFileInfo
     * size: 16 bytes
     * description:
     *
     * BP  Size  Type       Identifier           Description
     * -----------------------------------------------------
     * 0   2*4   SInt16[4]  reserved1
     * 8   2     UInt16     extendedFinderFlags
     * 10  2     SInt16     reserved2
     * 12  4     SInt32     putAwayFolderID
     */

    private final byte[] reserved1 = new byte[2*4];

    public ExtendedFileInfo(byte[] data, int offset) {
        super(data, offset);
	System.arraycopy(data, offset+0, reserved1, 0, 2*4);
    }

    public static int length() { return 16; }

    @Override
    public byte[] getBytes() {
        byte[] result = new byte[length()];
	byte[] tempData;
	int offset = 0;

	System.arraycopy(reserved1, 0, result, offset, reserved1.length); offset += reserved1.length;

        tempData = super.getBytes();
	System.arraycopy(tempData, 0, result, offset, tempData.length); offset += tempData.length;

        return result;
    }

    public short[] getReserved1() { return Util.readShortArrayBE(reserved1); }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        {
            short[] tmp = getReserved1();
            ps.println(prefix + " reserved1: { " +
                    "0x" + Util.toHexStringBE(tmp[0]) + ", " +
                    "0x" + Util.toHexStringBE(tmp[1]) + ", " +
                    "0x" + Util.toHexStringBE(tmp[2]) + ", " +
                    "0x" + Util.toHexStringBE(tmp[3]) + " }");
        }
        super.printFields(ps, prefix);
    }

    /* @Override */
    public void print(PrintStream ps, String prefix) {
	ps.println(prefix + "ExtendedFileInfo:");
	printFields(ps, prefix);
    }

    @Override
    public Dictionary getStructElements() {
         DictionaryBuilder db = new DictionaryBuilder(ExtendedFileInfo.class.getName());

         db.addIntArray("reserved1", reserved1, BITS_16, UNSIGNED, BIG_ENDIAN);
         db.addAll(super.getStructElements());

         return db.getResult();
    }
}
