package org.let5sne.alioss

import groovy.transform.CompileStatic
import org.moqui.context.ExecutionContextFactory
import org.moqui.context.ToolFactory
import com.aliyun.oss.OSSClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * more detail see https://help.aliyun.com/document_detail/32008.html?spm=5176.doc32013.6.660.tE4O4J
 */
@CompileStatic
class AliossToolFactory implements ToolFactory<OSSClient> {

    protected final static Logger logger = LoggerFactory.getLogger(this.class)

    final static String TOOL_NAME = "AliOSSClient"

    protected ExecutionContextFactory ecf = null

    /** OSSClient Instance */
    protected OSSClient oSSClientInstance  = null

    @Override
    String getName() { return TOOL_NAME }

    @Override
    void init(ExecutionContextFactory ecf) { }


    @Override
    void preFacadeInit(ExecutionContextFactory ecf) {
        this.ecf = ecf

        String endpoint = System.getProperty("alioss.endpoint")
        String accessKeyId = System.getProperty("alioss.accessKeyId")
        String accessKeySecret = System.getProperty("alioss.accessKeySecret")
        if (endpoint && accessKeyId && accessKeySecret) {
            logger.info("Starting OSSClient with alioss.endpoint system property (${endpoint})")
            logger.info("Starting OSSClient with alioss.accessKeyId system property (${accessKeyId})")
            logger.info("Starting OSSClient with alioss.accessKeySecret system property (${accessKeySecret})")
        } else {
            // TODO logger.info("Starting OSSClient with alioss.xml from classpath")
        }
        oSSClientInstance = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    @Override
    OSSClient getInstance(Object... parameters) {
        if (oSSClientInstance == null) throw new IllegalStateException("AliossToolFactory not initialized")
        return oSSClientInstance
    }

    @Override
    void destroy() {
        // shutdown Hazelcast
        oSSClientInstance.shutdown();
        logger.info("OSSClient shutdown")
    }

    ExecutionContextFactory getEcf() { return ecf }
}
