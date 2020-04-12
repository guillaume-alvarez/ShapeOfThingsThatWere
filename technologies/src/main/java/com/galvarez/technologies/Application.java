package com.galvarez.technologies;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static spark.Spark.get;

public final class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String... args) {
        Application app = new Application();
    }

    private static VelocityEngine createEngine() {
        Properties properties = new Properties();
        properties.setProperty("resource.loaders", "class");
        properties.setProperty("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty("resource.default_encoding", "UTF-8");
        properties.setProperty("output.encoding", "UTF-8");
        properties.setProperty("runtime.strict_mode.enable", "true");
        return new VelocityEngine(properties);
    }

    private final VelocityEngine engine;

    public Application() {
        this.engine = createEngine();

        Spark.staticFiles.location("com/galvarez/technologies/static/");
        createRoutes();
    }

    private void createRoutes() {
        get("/", (req, res) -> {
            res.redirect("/graph/");
            return "";
        });

        get("graph/", (req, res) -> {
            return renderGraph();
        });
    }

    private String render(Map<String, Object> model, String templateName) {
        Template template = engine.getTemplate("com/galvarez/technologies/" + templateName, "UTF-8");
        VelocityContext context = new VelocityContext(model);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        log.debug("Render:\n{}", writer.toString());
        return writer.toString();
    }

    private Object renderGraph() throws IOException {
        Map<String, Object> data = new HashMap<>();
        try (Reader r = new InputStreamReader(Application.class.getResourceAsStream("/com/galvarez/technologies/technologies.plantuml"))) {
            data.put("technologies", new PlantumlParser().parse(new BufferedReader(r)));
        }
        return render(data, "technologies.vm");
    }

}