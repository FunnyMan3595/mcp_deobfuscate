package org.ldg.mcpd;

import java.io.*;

public interface MCPDClassHandler {
    public boolean needsOutput();
    public String processZippedFilename(String filename);
    public void handleClass(InputStream in, OutputStream out) throws IOException;
}
