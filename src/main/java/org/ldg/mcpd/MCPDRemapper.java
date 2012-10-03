package org.ldg.mcpd;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class MCPDRemapper extends Remapper {
    public String map(String typeName) {
        System.out.println("map(" + typeName + ")");
        return typeName;
    }

    public String mapFieldName(String cls, String name, String descriptor) {
        System.out.println("mapFieldName(" + cls + ", " + name + ", " + descriptor + ")");
        return name;
    }

    public String mapMethodName(String cls, String name, String descriptor) {
        System.out.println("mapMethodName(" + cls + ", " + name + ", " + descriptor + ")");
        return name;
    }

    public String mapFilename(String infile) {
        // Translate from "org/me/Foo.class" to "org.me.Foo"
        String inclass = infile.substring(0, infile.length() - 6).replace("/", ".");

        // Apply standard remapping.
        String outclass = map(inclass);

        // Translate from "org.me.Foo" back to "org/me/Foo.class"
        return outclass.replace(".", "/") + ".class";
    }

    public void remap(File input, File output) throws IOException {
        String name = input.getName().toLowerCase();
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
            // Open the input and output zip files.
            ZipFile inzip = new ZipFile(input);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));

            // Iterate through the file in the input zip.
            Enumeration<? extends ZipEntry> entries = inzip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // File name.
                String entryname = entry.getName();

                // File contents.
                InputStream in = inzip.getInputStream(entry);

                if (entryname.toLowerCase().endsWith(".class")) {
                    out.putNextEntry(new ZipEntry(mapFilename(entryname)));
                    remap(in, out);
                    in.close();
                    out.closeEntry();
                } else {
                    // Not a .class file, just copy it unaltered.
                    out.putNextEntry(new ZipEntry(entryname));
                    byte[] data = new byte[1024];
                    int amount = in.read(data);
                    while (amount > 0) {
                        out.write(data, 0, amount);
                        amount = in.read(data);
                    }
                    out.closeEntry();
                }
            }

            out.close();
        } else {
            InputStream in = new FileInputStream(input);
            OutputStream out = new FileOutputStream(output);
            remap(in, out);
            in.close();
            out.close();
        }
    }

    public void remap(InputStream in, OutputStream out) throws IOException {
        // Set up the ASM prerequisites.
        ClassReader cr = new ClassReader(in);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS |
                                         ClassWriter.COMPUTE_FRAMES);
        RemappingClassAdapter visitor = new RemappingClassAdapter(cw, this);

        // Do the actual remapping.
        cr.accept(visitor, ClassReader.EXPAND_FRAMES);

        // Write out the translated class.
        out.write(cw.toByteArray());
    }
}
