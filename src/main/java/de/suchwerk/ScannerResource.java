package de.suchwerk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

@ApplicationScoped
@Path("api")
public class ScannerResource {

    @ConfigProperty(name = "scanner.command")
    String scannerCommand;

    @ConfigProperty(name = "scanner.filename")
    String defaultFilename;

    @ConfigProperty(name = "scanner.output.path", defaultValue = ".")
    String defaultPath;

    @Inject
    ScanLogger logger;

    /**
     * Endpoint to initiate a scanning process. using a predefined scanner command.
     * 
     * This API endpoint allows the user to start a scan operation using a predefined scanner command.
     * The user can optionally specify a filename for the output file via the 'filename' query parameter.
     * 
     * Configuration:
     * - scanner.command: The base command used to initiate the scan.
     * - scanner.filename: The default filename used if no filename is provided by the user.
     * - scanner.output.path: The default directory path where the output file will be stored if not specified.
     * 
     * Usage:
     * - Send a GET request to '/api/scan' with an optional 'filename' query parameter to specify the output file name.
     * 
     * Response:
     * - On success, returns the scanned file as an octet-stream with a 'Content-Disposition' header for file download.
     * - On failure, returns a server error with a message indicating the reason for failure.
     */
    @Path("scan")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)    
    public Response scan(@QueryParam("filename") Optional<String> filename) {
        try {
            List<String> commandParts = parseCommand(scannerCommand, filename); // this also replaces the filename placeholder
            Log.info("commandParts");
            Log.info(commandParts);
            Optional<String> outputPath = getOutputFileFromCommand(commandParts);
            
            if (outputPath.isEmpty()) {
                logger.log(scannerCommand, "FAILURE", "no output path");
                return Response.serverError().entity("Output file path (-o) is not specified.").build();                
            }
        
            File outputFile = new File(outputPath.get());
            File parentDir = outputFile.getParentFile();
            if (parentDir == null) {
                String defaultParentPath = defaultPath;
                parentDir = new File(defaultParentPath);
            }
            parentDir.mkdirs();

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String processOutpString;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                processOutpString = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();


            if (exitCode != 0 || !outputFile.exists()) {
                logger.log(scannerCommand, "FAILURE", "file not created");
                return Response.serverError().header("Content-Type", "plain/text").entity("Scan failed or file was not created. %s. Exit code is %d".formatted(processOutpString, exitCode)).build();
            }

            logger.log(scannerCommand, "SUCCESS", outputPath.get());
            return Response.ok((StreamingOutput) output -> {
                try (InputStream in = new FileInputStream(outputFile)) {
                    in.transferTo(output);
                }
            })
                    .header("Content-Disposition", "attachment; filename=\"" + outputFile.getName() + "\"")
                    .build();

        } catch (Exception e) {
            logger.log(scannerCommand, "ERROR", e.getMessage());
            return Response.serverError().entity("Scan error: " + e.getMessage()).build();
        }
    }

    private List<String> parseCommand(String commandLine, Optional<String> filename) {
        List<String> parts = new ArrayList<>();
        Scanner sc = new Scanner(commandLine);
        while (sc.hasNext())
            parts.add(sc.next().replace("{filename}", filename.orElse(defaultFilename)));
        sc.close();
        return parts;
    }

    private Optional<String> getOutputFileFromCommand(List<String> commandParts) {
        for (int i = 0; i < commandParts.size(); i++) {
            if (commandParts.get(i).equals("-o") && i + 1 < commandParts.size()) {
                var outputFilePath = commandParts.get(i + 1).replace("\"", "");
                return Optional.of(outputFilePath);
            }
        }
        return Optional.empty();
    }

}
