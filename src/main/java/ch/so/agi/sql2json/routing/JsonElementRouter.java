package ch.so.agi.sql2json.routing;

import ch.so.agi.sql2json.exception.TrafoException;
import ch.so.agi.sql2json.tag.BaseTag;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Routes the JsonElements to the writer or to the corresponding sql-tag
 * for appending the json from the sql resultset.
 */
public class JsonElementRouter {

    private static Logger log = LogManager.getLogger(JsonElementRouter.class);

    private JsonGenerator gen;
    private ObjectElementBuffer buf;

    public JsonElementRouter(JsonGenerator gen){
        this.gen = gen;
        this.buf = new ObjectElementBuffer(gen);
    }

    public void objStartElem(){
        log.debug("objStartElem()");

        buf.objStartElem();
    }

    public void objEndElem(){
        log.debug("objEndElem()");

        boolean candidateIsTemplate = false;
        if (buf.isComplete()) {
            BaseTag t = BaseTag.forName(buf.getName());
            if (t != null){
                try {
                    candidateIsTemplate = true;
                    t.execSql(buf.getValue(), gen);
                }
                catch(Exception e){
                    try {
                        buf.flushWithException(e);
                        gen.writeEndObject();

                        if(e instanceof TrafoException)
                            TemplateWalker.addTagException((TrafoException)e);
                        else
                            TemplateWalker.addTagException(new TrafoException(e, "{0}: Exception occurred while executing tag", buf.getValue()));
                    }
                    catch (Exception ex) {
                        throw new TrafoException(ex);
                    }
                }
                finally {
                    buf.reset();
                }
            }
        }

        if(!candidateIsTemplate){
            try {
                buf.flush();
                gen.writeEndObject();
            }
            catch (Exception e) {
                throw new TrafoException(e);
            }
        }
    }

    public void arrayStartElem(){
        log.debug("arrayStartElem()");

        try {
            gen.writeStartArray();
        }
        catch (Exception e) {
            throw new TrafoException(e);
        }
    }

    public void arrayEndElem(){
        log.debug("arrayEndElem()");

        try {
            gen.writeEndArray();
        }
        catch (Exception e) {
            throw new TrafoException(e);
        }
    }

    public void paraName(String name) {
        log.debug("paraName(). name: {}", name);

        buf.paraName(name);
    }

    public void value(JsonToken type, Object value){
        log.debug("value(). type: {}, value: {}", type, value);

        buf.value(type, value);
    }
}
