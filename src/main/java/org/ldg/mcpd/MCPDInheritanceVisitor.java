package org.ldg.mcpd;

import org.objectweb.asm.*;
import java.io.*;

public class MCPDInheritanceVisitor implements ClassVisitor, MCPDClassHandler {
    private PrintWriter out = null;
    public MCPDInheritanceGraph graph;

    public MCPDInheritanceVisitor(File output) throws IOException {
        if (output != null) {
            FileOutputStream fos = new FileOutputStream(output);
            out = new PrintWriter(fos);
        }

        graph = new MCPDInheritanceGraph();
    }

    public void recordRelationship(String parent, String child) {
        if (out != null) {
            out.println(parent + ";" + child);
        }

        graph.addRelationship(parent, child);
    }

    public void done() throws IOException {
        out.close();
    }

    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        recordRelationship(superName, name);
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
