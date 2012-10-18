package org.ldg.mcpd;

import com.beust.jcommander.JCommander;
import java.io.*;
import java.util.*;

public class MCPDeobfuscate {
    public static void main(String args[]) {
        MCPDOptions options = new MCPDOptions();

        JCommander jc;

        try {
            jc = new JCommander(options, args);
            jc.setProgramName("mcpd");
        } catch (com.beust.jcommander.ParameterException e) {
            System.out.println(e.getMessage());
            System.exit(1);
            return; // Stupid fucking compiler.
        }

        if (options.help) {
            jc.usage();
            System.exit(0);
        }

        if (options.inputs == null || options.inputs.size() == 0) {
            System.out.println("No input files.");
            System.exit(0);
        }

        boolean inheritance_only = false;
        if (options.outputs == null && options.outdir == null) {
            inheritance_only = true;
        }

        if (inheritance_only) {
            if (options.inheritance_file == null) {
                System.out.println("Nothing to do.");
                System.out.println("(Did you forget --output, --outdir, or "
                                   + "--inheritance?)");
                System.exit(0);
            }
        } else {
            if (options.config == null) {
                System.out.println("You must specify a --config to translate with.");
                System.exit(1);
            }

            if (options.outputs != null &&
                options.outputs.size() != options.inputs.size()) {
                System.out.println("--infiles and --outfiles must be the same size.");
            }

            if (options.keepdirs && !options.unsafe) {
                System.err.print(
    "WARNING: Using -k or --keepdirs can result in unexpected behaviour if any input\n" +
    "         files are not contained in --indir (defaults to current directory).\n" +
    "Are you sure you want to continue? (y/N) ");
                System.err.flush();

                String response = "n";
                try {
                    InputStreamReader rin = new InputStreamReader(System.in);
                    BufferedReader bin = new BufferedReader(rin);
                    response = bin.readLine().trim();
                } catch (Exception e) {}

                if (!response.equalsIgnoreCase("y")) {
                    System.out.println("Aborting.");
                    System.exit(2);
                }
            }
        }

        List<File> infiles = new ArrayList<File>();
        List<File> outfiles = new ArrayList<File>();

        // Base input file directory.
        File indir;
        if (options.indir == null) {
            indir = new File(".");
        } else {
            indir = new File(options.indir);
        }

        // Base output file directory.
        File outdir;
        if (options.outdir == null) {
            outdir = new File(".");
        } else {
            outdir = new File(options.outdir);
        }

        // Collect the input files.
        for (String in : options.inputs) {
            infiles.add(new File(indir, in));
        }

        // Collect the output files.
        if (options.outputs != null) {
            // With the names given.
            for (String out : options.outputs) {
                outfiles.add(new File(outdir, out));
            }
        } else {
            // By using the original names.
            if (options.keepdirs) {
                // And their directories (can be unsafe)
                for (String in : options.inputs) {
                    outfiles.add(new File(outdir, in));
                }
            } else {
                // Flat in the output directory.
                for (File infile : infiles) {
                    outfiles.add(new File(outdir, infile.getName()));
                }
            }
        }

        System.out.println("Calculating inheritance...");
        File inheritanceFile = null;
        if (options.inheritance_file != null) {
            inheritanceFile = new File(options.inheritance_file);
        }

        List<File> libraryFiles = new ArrayList<File>();
        if (options.library_files != null) {
            for (String library : options.library_files) {
                libraryFiles.add(new File(library));
            }
        }

        MCPDInheritanceVisitor inheritance;
        try {
            inheritance = new MCPDInheritanceVisitor(inheritanceFile, libraryFiles);
            MCPDFileHandler inheritanceProcessor = new MCPDFileHandler(inheritance);
            inheritanceProcessor.processFiles(infiles, outfiles);
            inheritance.done();
        } catch (IOException e) {
            System.out.println("Error: Unable to write to inheritance file.");
            System.exit(2);
            return; // Stupid fucking compiler.
        }

        if (inheritance_only) {
            System.exit(0);
        }

        System.out.println();
        System.out.println("Translating...");

        MCPDRemapper remapper;
        try {
            remapper = new MCPDRemapper(new File(options.config),
                                        options.exclude, inheritance.graph,
                                        options.invert);
        } catch (IOException e) {
            System.out.println("Unable to read config file.");
            e.printStackTrace();
            System.exit(3);
            return; // Stupid fucking compiler.
        }

        MCPDFileHandler remapProcessor = new MCPDFileHandler(remapper);
        remapProcessor.processFiles(infiles, outfiles);
    }
}
