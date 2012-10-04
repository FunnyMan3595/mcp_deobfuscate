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

        if (options.outputs == null && options.outdir == null) {
            System.out.println("You must specify --outdir or --outfiles.");
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

        List<File> infiles = new ArrayList<File>();
        List<File> outfiles = new ArrayList<File>();

        File indir;
        if (options.indir == null) {
            indir = new File(".");
        } else {
            indir = new File(options.indir);
        }

        File outdir;
        if (options.outdir == null) {
            outdir = new File(".");
        } else {
            outdir = new File(options.outdir);
        }

        for (String in : options.inputs) {
            infiles.add(new File(indir, in));
        }

        if (options.outputs != null) {
            for (String out : options.outputs) {
                outfiles.add(new File(outdir, out));
            }
        } else {
            if (options.keepdirs) {
                for (String in : options.inputs) {
                    outfiles.add(new File(outdir, in));
                }
            } else {
                for (File infile : infiles) {
                    outfiles.add(new File(outdir, infile.getName()));
                }
            }
        }

        MCPDRemapper remapper;
        try {
            remapper = new MCPDRemapper(new File(options.config),
                                        options.exclude);
        } catch (IOException e) {
            System.out.println("Unable to read config file.");
            e.printStackTrace();
            System.exit(3);
            return; // Stupid fucking compiler.
        }
        int errors = 0;
        int successes = 0;
        for (int i=0; i<infiles.size(); i++) {
            try {
                File infile = infiles.get(i);
                File outfile = outfiles.get(i);

                System.out.println("Translating from: "
                                   + infile.getCanonicalPath());
                System.out.println("              to: "
                                   + outfile.getCanonicalPath());

                remapper.remap(infile, outfile);

                successes += 1;
            } catch (Exception e) {
                System.out.println("Translation failed:");
                e.printStackTrace(new PrintStream(System.out));
                errors += 1;
            }
        }

        System.out.println("Translation complete: "
                           + successes + " files successful, "
                           + errors + " files failed.");
    }
}
