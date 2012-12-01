package org.ldg.mcpd;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class FileHandler {
    public ClassHandler handler;

    public FileHandler(ClassHandler classHandler) {
        handler = classHandler;
    }

    public int processFiles(List<File> inputs, List<File> outputs) {
        assert(inputs.size() == outputs.size());

        int errors = 0;
        int successes = 0;
        for (int i=0; i<inputs.size(); i++) {
            try {
                File input = inputs.get(i);
                File output = outputs.get(i);

                System.out.println("Processing '" + input.getCanonicalPath()
                                   + "'...");

                processFile(input, output);

                successes += 1;
            } catch (Exception e) {
                System.out.println("Processing failed:");
                e.printStackTrace(new PrintStream(System.out));
                errors += 1;
            }
        }

        System.out.println("Processing complete: "
                           + successes + " files successful, "
                           + errors + " files failed.");

        return errors;
    }

    public void processFile(File input, File output) throws IOException {
        String name = input.getName().toLowerCase();
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
            // Open the input and output zip files.
            ZipFile inzip = new ZipFile(input);
            ZipOutputStream out = null;

            if (handler.needsOutput()) {
                out = new ZipOutputStream(new FileOutputStream(output));
            }

            // Iterate through the files in the input zip.
            Enumeration<? extends ZipEntry> entries = inzip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // File name.
                String entryName = entry.getName();
                String processedName = handler.processZippedFilename(entryName);

                // File contents.
                InputStream in = inzip.getInputStream(entry);

                if (processedName == null) {
                    // Exempt or not a .class file, just copy it unaltered.
                    if (out != null) {
                        out.putNextEntry(new ZipEntry(entryName));
                        byte[] data = new byte[1024];
                        int amount = in.read(data);
                        while (amount > 0) {
                            out.write(data, 0, amount);
                            amount = in.read(data);
                        }
                        out.closeEntry();
                    }
                } else {
                    // Pass it through the handler, with appropriate IO streams.
                    if (out != null) {
                        out.putNextEntry(new ZipEntry(processedName));
                    }
                    handler.handleClass(in, out);
                    in.close();
                    if (out != null) {
                        out.closeEntry();
                    }
                }
            }

            if (out != null) {
                out.close();
            }
        } else {
            // Pass it through the handler, with appropriate IO streams.
            InputStream in = new FileInputStream(input);
            OutputStream out = null;
            if (handler.needsOutput()) {
                out = new FileOutputStream(output);
            }
            handler.handleClass(in, out);
            in.close();
            if (out != null) {
                out.close();
            }
        }
    }
}
