/*-
 * Copyright (C) 2021 Erik Larsson
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

package org.catacombae.hfs.original;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import org.catacombae.util.Util;

/**
 * Generic string codec for single-byte codepage encodings (simple table with
 * 256 entries).
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class SingleByteCodepageStringCodec implements StringCodec {
    private final char[] codepageData;
    private final HashMap<String, Byte> unicodeToCodepageMap =
            new HashMap<String, Byte>();

    protected SingleByteCodepageStringCodec(char[] codepageData) {
        if(codepageData.length != 256) {
            throw new RuntimeException("Unexpected size of codepage data: " +
                    codepageData.length);
        }

        this.codepageData = codepageData;

        for(int i = 0; i < 256; ++i) {
            this.unicodeToCodepageMap.put(
                    Character.toString(codepageData[i]), (byte) i);
        }
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public String decode(byte[] data) {
        return decode(data, 0, data.length);
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public String decode(byte[] data, int off, int len)
    {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < len; ++i) {
            sb.append(codepageData[data[off + i] & 0xFF]);
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public byte[] encode(String str) {
        return encode(str, 0, str.length());
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public byte[] encode(String str, int off, int len) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for(int i = 0; i < len;) {
            final int remaining = len - i;
            char firstChar;
            Byte replacement = null;

            firstChar = str.charAt(off + i);
            if(firstChar < 0x20) {
                replacement = (byte) firstChar;
            }
            else {
                for(int j = 5; j > 0; --j) {
                    if(remaining >= j) {
                        /* Check if the j-character substring has a match. */
                        replacement = unicodeToCodepageMap.get(str.substring(
                                off + i, j));
                        if(replacement != null) {
                            i += j;
                            break;
                        }
                    }
                }
            }

            if(replacement == null) {
                throw new StringCodecException("Unable to encode sequence at " +
                        "character " + i + ": " +
                        "0x" + Util.toHexStringBE(str.charAt(i)));
            }

            if(replacement > 0xFF) {
                os.write((replacement >>> 8) & 0xFF);
            }

            os.write(replacement & 0xFF);
        }

        return os.toByteArray();
    }
}
