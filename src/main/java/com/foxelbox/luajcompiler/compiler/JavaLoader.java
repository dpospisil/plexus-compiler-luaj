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