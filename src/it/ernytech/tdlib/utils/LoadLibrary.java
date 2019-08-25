/*
 * Copyright (c) 2018. Ernesto Castellotti <erny.castell@gmail.com>
 * This file is part of JTdlib.
 *
 *     JTdlib is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     JTdlib is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with JTdlib.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.ernytech.tdlib.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;

/**
 * The class to load the libraries needed to run Tdlib
 */
public class LoadLibrary {
    private static ConcurrentHashMap<String, Boolean> libraryLoaded = new ConcurrentHashMap<>();

    /**
     * Load a library installed in the system (priority choice) or a library included in the jar.
     * @param libname The name of the library.
     * @throws CantLoadLibrary An exception that is thrown when the LoadLibrary class fails to load the library.
     */
    public static void load(String libname) throws Throwable {
        if (libname == null || libname.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (libraryLoaded.containsKey(libname)) {
            if (libraryLoaded.get(libname)) {
                return;
            }
        }

        loadLibrary(libname);
        libraryLoaded.put(libname,true);
    }

    private static void loadLibrary(String libname) throws Throwable {
      
        try {
			
            System.load("/usr/local/ntt/libs/td/" + libname + ".so");
			
        } catch (Exception | UnsatisfiedLinkError e) {
			
            throw new CantLoadLibrary().initCause(e);
			
        }
    }

   
}
