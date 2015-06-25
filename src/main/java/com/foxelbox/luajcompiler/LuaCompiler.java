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
package com.foxelbox.luajcompiler;

import com.foxelbox.luajcompiler.compiler.LuaJC;
import org.codehaus.plexus.compiler.*;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
  * @plexus.component role="org.codehaus.plexus.compiler.Compiler" role-hint="luac"
  */
public class LuaCompiler extends AbstractCompiler {
    private static final Globals globals = JsePlatform.standardGlobals();
    private static final LuaJC luaJC = new LuaJC();

    public LuaCompiler() {
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, ".lua", ".class", null);
    }

    @Override
    public String[] createCommandLine(CompilerConfiguration compilerConfiguration) throws CompilerException {
        return new String[0];
    }

    private String findSourceDir(CompilerConfiguration configuration, File sourceFile) {
        for(String sourceDir : configuration.getSourceLocations()) {
            if(sourceFile.getAbsolutePath().startsWith(sourceDir)) {
                return sourceFile.getAbsolutePath().substring(sourceDir.length() + 1);
            }
        }
        return sourceFile.getAbsolutePath();
    }

    private String stripExtension(String name) {
        int lastDot = name.lastIndexOf('.');
        return name.substring(0, lastDot);
    }

    @Override
    public CompilerResult performCompile(CompilerConfiguration configuration) throws CompilerException {
        List<CompilerMessage> messageList = new ArrayList<>();
        boolean success = true;

        try {
            for (File sourceFile : configuration.getSourceFiles()) {
                String relativeName = findSourceDir(configuration, sourceFile);
                Hashtable t = luaJC.compileAll(new FileInputStream(sourceFile), relativeName, relativeName, globals, false);

                for ( Enumeration e = t.keys(); e.hasMoreElements(); ) {
                    String key = (String) e.nextElement();
                    byte[] bytes = (byte[]) t.get(key);
                    File destpath = new File(configuration.getOutputLocation(), key.replace('.', '/') + ".class");
                    destpath.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream( destpath );
                    fos.write( bytes );
                    fos.close();
                }
            }
        } catch (IOException e) {
            messageList.add(new CompilerMessage(e.getMessage(), CompilerMessage.Kind.ERROR));
            success = false;
        }

        return new CompilerResult(success, messageList);
    }
}
