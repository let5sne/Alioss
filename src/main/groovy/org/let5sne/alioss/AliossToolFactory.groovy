package org.let5sne.alioss

import groovy.transform.CompileStatic
import org.moqui.context.ExecutionContextFactory
import org.moqui.context.ToolFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * more detail see https://help.aliyun.com/document_detail/32008.html?spm=5176.doc32013.6.660.tE4O4J
 */
@CompileStatic
class AliossToolFactory implements ToolFactory<AliossTool> {

    protected final static Logger logger = LoggerFactory.getLogger(this.class)

    final static String TOOL_NAME = "AliossTool"

    protected ExecutionContextFactory ecf = null

    /** OSSClient Instance */
    protected AliossTool aliossTool  = null

    @Override
    String getName() { return TOOL_NAME }

    @Override
    void init(ExecutionContextFactory ecf) { }


    @Override
    void preFacadeInit(ExecutionContextFactory ecf) {
        this.ecf = ecf

        String endpoint = System.getProperty("alioss.endpoint")
        String accessKeyId = System.getProperty("alioss.trust.ak")
        String accessKeySecret = System.getProperty("alioss.trust.sk")
        if (endpoint && accessKeyId && accessKeySecret) {
            logger.info("Starting OSSClient with alioss.endpoint system property (${endpoint})")
            logger.info("Starting OSSClient with alioss.trust.ak system property (${accessKeyId})")
            logger.info("Starting OSSClient with alioss.trust.sk system property (${accessKeySecret})")
        } else {
            // TODO logger.info("Starting OSSClient with alioss.xml from classpath")
        }
        aliossTool = new AliossTool(endpoint, accessKeyId, accessKeySecret);
    }

    @Override
    AliossTool getInstance(Object... parameters) {
        if (aliossTool == null) throw new IllegalStateException("AliossToolFactory not initialized")
        return aliossTool
    }

    @Override
    void destroy() {
        // shutdown Hazelcast
        aliossTool.shutdown();
        logger.info("OSSClient shutdown")
    }

    ExecutionContextFactory getEcf() { return ecf }
}
