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

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.luajc.JavaGen;

import java.util.HashMap;
import java.util.Map;

public class JavaLoader extends ClassLoader {

    private Map<String,byte[]> unloaded = new HashMap<>();

    public JavaLoader(ClassLoader parent) {
        super(parent);
    }

    public LuaFunction load(Prototype p, String classname, String filename) {
        return load(new JavaGen(p, classname, filename, false));
    }

    public LuaFunction load(JavaGen jg) {
        include(jg);
        return load(jg.classname);
    }

    public LuaFunction load(String classname) {
        try {
            return (LuaFunction)loadClass(classname).newInstance();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new IllegalStateException("bad class gen: "+e);
        }
    }

    public void include(JavaGen jg) {
        unloaded.put(jg.classname, jg.bytecode);
        for(int i = 0, n = ((jg.inners != null) ? jg.inners.length : 0); i < n; i++) {
            include(jg.inners[i]);
        }
    }

    public Class findClass(String classname) throws ClassNotFoundException {
        byte[] bytes = unloaded.get(classname);
        if (bytes != null) {
            return defineClass(classname, bytes, 0, bytes.length);
        }
        return super.findClass(classname);
    }

}