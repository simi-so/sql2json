package ch.so.agi.sql2json;

import ch.so.agi.sql2json.log.Logging;
import ch.so.agi.sql2json.routing.TemplateWalker;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.*;

public class Application {

    private static Logger log = LogManager.getLogger(Application.class);

    private static Configuration conf = null;

    public static void main(String[] args){

        try {
            conf = Configuration.createConfig4Args(args);

            String level = conf.getConfigValue(Configuration.LOG_LEVEL);
            Logging.initToLogLevel(level);

            if(conf.helpPrinted())
                return;

            conf.assertComplete();

            String template = "{\"tableInfo\":{\"schemaName\":\"tiger\",\"description\":\"empty\",\"layers\":{\"$trafo:fuu\": \"bar\"},\"tvName\":\"county\"}}";
            OutputStream output = new ByteArrayOutputStream();

            try {
                TemplateWalker.walkTemplate(template, output);
            }
            finally {
                output.close();
            }

            log.info(output.toString());
        }
        catch (Exception e){
            log.error("Exception occured. Exiting...", e);
        }
    }


    public static Configuration conf(){return conf;}
}
