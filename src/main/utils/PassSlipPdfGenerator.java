package main.utils;

import models.PassSlip;
import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

/**
 * Generates a PDF pass slip by calling generate_passlip.py.
 * Requires Python + reportlab installed on the machine.
 */
public class PassSlipPdfGenerator {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Generates a PDF for the given pass slip and opens it.
     * @return path to the generated PDF, or null if failed
     */
    public static String generateAndOpen(PassSlip slip) {
        String pdfPath = generatePdf(slip);
        if (pdfPath != null) openPdf(pdfPath);
        return pdfPath;
    }

    /**
     * Generates the PDF and returns the file path (without opening).
     */
    public static String generatePdf(PassSlip slip) {
        try {
            // Locate the Python script next to the JAR / project root
            String scriptPath = findScript("generate_passlip.py");
            if (scriptPath == null) {
                System.out.println("[PDF] generate_passlip.py not found!");
                return null;
            }

            // Output path — Desktop folder for easy access
            String desktop  = System.getProperty("user.home") + File.separator + "Desktop";
            String fileName = String.format("PassSlip_PS-%04d.pdf", slip.getSlipId());
            String outPath  = desktop + File.separator + fileName;

            // Format times
            String timeOut = slip.getTimeOut() != null ? slip.getTimeOut().format(TIME_FMT) : "--:--";
            String timeIn  = slip.getTimeIn()  != null ? slip.getTimeIn() .format(TIME_FMT) : "--:--";
            String cat     = slip.getCategory() != null ? slip.getCategory() : "Official Business";

            // Build command: python generate_passlip.py <args>
            ProcessBuilder pb = new ProcessBuilder(
                getPythonCommand(),
                scriptPath,
                String.valueOf(slip.getSlipId()),
                safe(slip.getEmpName()),
                safe(slip.getDepartment()),
                safe(slip.getReason()),
                timeOut,
                timeIn,
                cat,
                outPath
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode  = process.waitFor();

            System.out.println("[PDF] Python output: " + output.trim());

            if (exitCode == 0 && output.contains("OK:")) {
                System.out.println("[PDF] Generated: " + outPath);
                return outPath;
            } else {
                System.out.println("[PDF] Generation failed (exit " + exitCode + "): " + output);
                return null;
            }

        } catch (Exception e) {
            System.out.println("[PDF] Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void openPdf(String path) {
        try {
            File file = new File(path);
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            System.out.println("[PDF] Could not open PDF: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private static String findScript(String scriptName) {
        // 1. Next to JAR (deployed)
        try {
            String jarDir = new File(PassSlipPdfGenerator.class
                .getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            File f = new File(jarDir, scriptName);
            if (f.exists()) return f.getAbsolutePath();
        } catch (Exception ignored) {}

        // 2. Working directory (IntelliJ dev mode)
        File f2 = new File(System.getProperty("user.dir"), scriptName);
        if (f2.exists()) return f2.getAbsolutePath();

        // 3. One level up from working directory
        File f3 = new File(System.getProperty("user.dir")).getParentFile();
        if (f3 != null) {
            File f4 = new File(f3, scriptName);
            if (f4.exists()) return f4.getAbsolutePath();
        }

        return null;
    }

    private static String getPythonCommand() {
        // Try python3 first, fall back to python
        for (String cmd : new String[]{"python", "python3"}) {
            try {
                Process p = new ProcessBuilder(cmd, "--version").start();
                p.waitFor();
                if (p.exitValue() == 0) return cmd;
            } catch (Exception ignored) {}
        }
        return "python";
    }

    private static String safe(String s) {
        if (s == null) return "";
        // Escape quotes so they don't break the command
        return s.replace("\"", "").replace("'", "");
    }
}
