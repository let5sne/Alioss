package org.let5sne.alioss

import com.aliyun.oss.HttpMethod
import com.aliyun.oss.OSSClient
import com.aliyun.oss.common.utils.DateUtil
import com.aliyun.oss.model.GeneratePresignedUrlRequest
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.auth.sts.AssumeRoleRequest
import com.aliyuncs.auth.sts.AssumeRoleResponse
import com.aliyuncs.exceptions.ClientException
import com.aliyuncs.http.MethodType
import com.aliyuncs.http.ProtocolType
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.profile.IClientProfile
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Date

@CompileStatic
class AliossTool extends OSSClient {

    protected final static Logger logger = LoggerFactory.getLogger(this.class)

    // 目前只有"cn-hangzhou"这个region可用, 不要使用填写其他region的值
    public static final String REGION_CN_HANGZHOU = "cn-hangzhou"

    // 当前 STS API 版本
    public static final String STS_API_VERSION = "2015-04-01"

    public static String bucket

    private static String endpointOuter

    AliossTool(String endpoint, String accessKeyId, String secretAccessKey) {
        super(endpoint, accessKeyId, secretAccessKey)
        this.bucket = System.getProperty("alioss.bucket")
        this.endpointOuter = System.getProperty("alioss.outer.endpoint")
    }

    public String getPresignedUrl(String key,String roleSessionName,String ak,String sk,String style){
        String roleArn = System.getProperty("alioss.roleArn.read")
        def policy = '''
            {
              "Version": "1",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Action": [
                    "oss:GetBucket",
                    "oss:GetObject"
                  ],
                  "Resource": [
                    "acs:oss:*:*:'''+bucket+'''/'''+key+'''"
                  ]
                }
              ]
            }
        '''
        AssumeRoleResponse assumeRoleResponse = this.assumeRole(roleArn,roleSessionName,policy,ak,sk)
        if(!assumeRoleResponse) return ""
        OSSClient ossClient = new OSSClient(endpointOuter,assumeRoleResponse.getCredentials().getAccessKeyId(),assumeRoleResponse.getCredentials().getAccessKeySecret(),assumeRoleResponse.getCredentials().securityToken);
        Date dexpiration = DateUtil.parseIso8601Date(assumeRoleResponse.getCredentials().getExpiration())
        if(style){
            GeneratePresignedUrlRequest signReq = new GeneratePresignedUrlRequest(bucket, key, HttpMethod.GET)
            signReq.setExpiration(dexpiration)
            signReq.setProcess(style)
            URL signedUrl = ossClient.generatePresignedUrl(signReq)
            if(signedUrl) return signedUrl.toURI()
            return ""
        }else{
            URL url = ossClient.generatePresignedUrl(bucket,key,dexpiration)
            ossClient.shutdown()
            if(url) return url.toURI()
            return ""
        }
    }

    public String getPresignedUrl(String key,String roleSessionName,String style){
        String ak = System.getProperty("alioss.oss.reader.ak")
        String sk = System.getProperty("alioss.oss.reader.sk")
        return this.getPresignedUrl(key,roleSessionName,ak,sk,style)
    }

    private AssumeRoleResponse assumeRole(String roleArn, String roleSessionName, String policy,String ak,String sk) {
        try {
            // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
            IClientProfile profile = DefaultProfile.getProfile(REGION_CN_HANGZHOU, ak, sk);
            DefaultAcsClient client = new DefaultAcsClient(profile);
            // 创建一个 AssumeRoleRequest 并设置请求参数
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setVersion(STS_API_VERSION);
            request.setMethod(MethodType.POST);
            request.setProtocol(ProtocolType.HTTPS);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy);
            // 发起请求，并得到response
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace()
            return null;
        }
    }

}
