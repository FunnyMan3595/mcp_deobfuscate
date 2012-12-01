package org.ldg.mcpd;

import org.objectweb.asm.*;
import java.util.*;
import java.io.*;

public class InheritanceVisitor extends ClassVisitor implements ClassHandler {
    private PrintWriter out = null;
    public InheritanceGraph graph;

    public InheritanceVisitor(File output, List<File> caches) throws IOException {
        super(Opcodes.ASM4);

        if (output != null) {
            FileOutputStream fos = new FileOutputStream(output);
            out = new PrintWriter(fos);
        }

        graph = new InheritanceGraph();

        for (File cache : caches) {
            BufferedReader contents = new BufferedReader(new InputStreamReader(new FileInputStream(cache)));

            String line = contents.readLine();
            while (line != null) {
                String parts[] = line.split(";");
                if (parts.length != 2) {
                    System.out.println("Bad stored inheritance: " + line);
                } else {
                    String parent = parts[0];
                    String child = parts[1];
                    graph.addRelationship(parent, child);
                }

                line = contents.readLine();
            }
        }
    }

    public void recordRelationship(String parent, String child) {
        if (out != null) {
            out.println(parent + ";" + child);
        }

        graph.addRelationship(parent, child);
    }

    public void done() throws IOException {
        if (out != null) {
            out.close();
        }
    }

    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        recordRelationship(superName, name);

        for (String iface : interfaces) {
            recordRelationship(iface, name);
        }
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {}

    public void visitEnd() {}

    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        return null;
    }

    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {}

    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        return null;
    }

    public void visitOuterClass(String owner, String name, String desc) {}

    public void visitSource(String source, String debug) {}


    public boolean needsOutput() {
        return false;
    }

    public String processZippedFilename(String filename) {
        if (filename.endsWith(".class")) {
            return filename;
        } else {
            return null;
        }
    }

    public void handleClass(InputStream in, OutputStream out) throws IOException {
        assert(out == null);

        // Pass it in as leanly as possible.
        ClassReader cr = new ClassReader(in);
        cr.accept(this, cr.SKIP_CODE | cr.SKIP_DEBUG | cr.SKIP_FRAMES);
    }
}
