package org.ldg.mcpd;

import org.objectweb.asm.*;
import java.util.*;

public class NonloadingClassWriter extends ClassWriter {
    InheritanceGraph inheritance;

    public NonloadingClassWriter(InheritanceGraph inheritance) {
        super(0);
        this.inheritance = inheritance;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        return inheritance.getCommonAncestor(type1, type2);
    }
}
