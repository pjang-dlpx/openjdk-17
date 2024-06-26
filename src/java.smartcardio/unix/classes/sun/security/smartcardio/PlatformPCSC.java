/*
 * Copyright (c) 2005, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.security.smartcardio;

import java.io.File;
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.security.util.Debug;

/**
 * Platform specific code and functions for Unix / MUSCLE based PC/SC
 * implementations.
 *
 * @since   1.6
 * @author  Andreas Sterbenz
 */
class PlatformPCSC {

    static final Debug debug = Debug.getInstance("pcsc");

    private final static String PROP_NAME = "sun.security.smartcardio.library";

    private final static String LIB0 = "libpcsclite.so.1";
    private final static String LIB1 = "/usr/$LIBISA/libpcsclite.so";
    private final static String LIB2 = "/usr/local/$LIBISA/libpcsclite.so";
    private final static String PCSC_FRAMEWORK = "/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC";

    PlatformPCSC() {
        // empty
    }

    @SuppressWarnings("removal")
    static final Throwable initException
            = AccessController.doPrivileged(new PrivilegedAction<Throwable>() {
        public Throwable run() {
            try {
                System.loadLibrary("j2pcsc");
                String library = getLibraryName();
                if (debug != null) {
                    debug.println("Using PC/SC library: " + library);
                }
                initialize(library);
                return null;
            } catch (Throwable e) {
                return e;
            }
        }
    });

    // expand $LIBISA to the system specific directory name for libraries
    private static String expand(String lib) {
        int k = lib.indexOf("$LIBISA");
        if (k == -1) {
            return lib;
        }
        String s1 = lib.substring(0, k);
        String s2 = lib.substring(k + 7);
        String libDir;
        if ("64".equals(System.getProperty("sun.arch.data.model"))) {
            // assume Linux convention
            libDir = "lib64";
        } else {
            // must be 32-bit
            libDir = "lib";
        }
        String s = s1 + libDir + s2;
        return s;
    }

    private static String getLibraryName() throws IOException {
        // if system property is set, use that library
        String lib = expand(System.getProperty(PROP_NAME, "").trim());
        if (lib.length() != 0) {
            return lib;
        }
	// let dlopen do the work
	lib = LIB0;
	return lib;
    }

    private static native void initialize(String libraryName);

    // PCSC constants defined differently under Windows and MUSCLE
    // MUSCLE version
    final static int SCARD_PROTOCOL_T0     =  0x0001;
    final static int SCARD_PROTOCOL_T1     =  0x0002;
    final static int SCARD_PROTOCOL_RAW    =  0x0004;

    final static int SCARD_UNKNOWN         =  0x0001;
    final static int SCARD_ABSENT          =  0x0002;
    final static int SCARD_PRESENT         =  0x0004;
    final static int SCARD_SWALLOWED       =  0x0008;
    final static int SCARD_POWERED         =  0x0010;
    final static int SCARD_NEGOTIABLE      =  0x0020;
    final static int SCARD_SPECIFIC        =  0x0040;

}
