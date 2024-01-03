/**
 * This file is part of plexus-compiler-luaj.
 *
 * plexus-compiler-luaj is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * plexus-compiler-luaj is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with plexus-compiler-luaj.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.foxelbox.luajcompiler.compiler;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.luajc.JavaGen;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

public class LuaJC {
    private static final ClassLoader parentClassLoader = LuaJC.class.getClassLoader();

    public LuaFunction load(Prototype p, String name) throws IOException {
        String luaname = toStandardLuaFileName(name);
        String classname = toStandardJavaClassName(luaname);
        JavaLoader loader = new JavaLoader(parentClassLoader);
        return loader.load(p, classname, luaname);
    }

    public Hashtable compileAll(InputStream script, String chunkname, String filename, Globals globals, boolean genmain) throws IOException {
        final String classname = toStandardJavaClassName( chunkname );
        final Prototype p = globals.loadPrototype(script, classname, "bt");
        return compileProtoAndSubProtos(p, classname, filename, genmain);
    }

    public Hashtable compileAll(Reader script, String chunkname, String filename, Globals globals, boolean genmain) throws IOException {
        final String classname = toStandardJavaClassName(chunkname );
        final Prototype p = globals.compilePrototype(script, classname);
        return compileProtoAndSubProtos(p, classname, filename, genmain);
    }

    private Hashtable compileProtoAndSubProtos(Prototype p, String classname, String filename, boolean genmain) throws IOException {
        final String luaname = toStandardLuaFileName( filename );
        final Hashtable h = new Hashtable();
        final JavaGen gen = new JavaGen(p, classname, luaname, genmain);
        insert( h, gen );
        return h;
    }

    private void insert(Hashtable h, JavaGen gen) {
        h.put(gen.classname, gen.bytecode);
        for ( int i=0, n=gen.inners!=null? gen.inners.length: 0; i<n; i++ )
            insert(h, gen.inners[i]);
    }


    private static String toStandardJavaClassName(String luachunkname) {
        String stub = toStub(luachunkname);
        StringBuilder classname = new StringBuilder();
        for (int i = 0, n = stub.length(); i < n; ++i) {
            final char c = stub.charAt(i);
            classname.append((((i == 0) && Character.isJavaIdentifierStart(c)) || ((i > 0) && Character.isJavaIdentifierPart(c))) ? c : '_');
        }
        return "lua." + classname.toString();
    }

    private static String toStandardLuaFileName(String luachunkname) {
        String filename = toStub(luachunkname).replace('.', '/') + ".lua";
        return filename.startsWith("@") ? filename.substring(1) : filename;
    }

    private static String toStub(String s) {
        return s.endsWith(".lua") ? s.substring(0, s.length() - 4) : s;
    }
}