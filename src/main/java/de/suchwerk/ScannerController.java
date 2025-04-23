package de.suchwerk;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ApplicationScoped
public class ScannerController extends Controller {
    @ConfigProperty(name = "scanner.command")
    String scannerCommand;

    @ConfigProperty(name = "scanner.filename")
    String defaultFilename;

    @ConfigProperty(name = "scanner.output.path", defaultValue = ".")
    String defaultPath;
    @Inject
    ScanLogger logger;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance logs(List<ScanLogEntry> entries, String scannerCommand, String defaultFilename, String defaultPath);        
    }

    @GET
    @Path("/")
    public TemplateInstance index() {
        return Templates.logs(logger.getLogs(), scannerCommand, defaultFilename, defaultPath);
    }
  
}
