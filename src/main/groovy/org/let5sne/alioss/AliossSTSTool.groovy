package org.let5sne.alioss

import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.auth.sts.AssumeRoleRequest
import com.aliyuncs.auth.sts.AssumeRoleResponse
import com.aliyuncs.exceptions.ClientException
import com.aliyuncs.http.MethodType
import com.aliyuncs.http.ProtocolType
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.profile.IClientProfile
import groovy.transform.CompileStatic

@CompileStatic
class AliossSTSTool {

    // 目前只有"cn-hangzhou"这个region可用, 不要使用填写其他region的值
    public static final String REGION_CN_HANGZHOU = "cn-hangzhou";
    // 当前 STS API 版本
    public static final String STS_API_VERSION = "2015-04-01";

    private AssumeRoleResponse assumeRole(String accessKeyId, String accessKeySecret,
                                          String roleArn, String roleSessionName, String policy,
                                          ProtocolType protocolType) {
        try {
            // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
            IClientProfile profile = DefaultProfile.getProfile(REGION_CN_HANGZHOU, accessKeyId, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);
            // 创建一个 AssumeRoleRequest 并设置请求参数
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setVersion(STS_API_VERSION);
            request.setMethod(MethodType.POST);
            request.setProtocol(protocolType);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy);
            // 发起请求，并得到response
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            throw e;
        }
    }
    /**

     * @param roleSessionName
     *       RoleSessionName 是临时Token的会话名称，自己指定用于标识你的用户，主要用于审计，或者用于区分Token颁发给谁
     * 但是注意RoleSessionName的长度和规则，不要有空格，只能有'-' '_' 字母和数字等字符
     * 具体规则请参考API文档中的格式要求
     */
    public AssumeRoleResponse getUploadPolicy(String roleSessionName,String path) {
        String accessKeyId = System.getProperty("alioss.accessKeyId")
        String accessKeySecret = System.getProperty("alioss.accessKeySecret")
        // RoleArn 需要在 RAM 控制台上获取
        String roleArn = System.getProperty("alioss.roleArn")

        def policy = '''
            {
              "Version": "1",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Action": [
                    "oss:PutObject"
                  ],
                  "Resource": [
                    "acs:oss:*:*:'''+path+''''"
                  ]
                }
              ]
            }
        '''
        // 此处必须为 HTTPS
        ProtocolType protocolType = ProtocolType.HTTPS;
        try {
            AssumeRoleResponse response = assumeRole(accessKeyId, accessKeySecret,roleArn, roleSessionName, policy, protocolType);
            System.out.println("Expiration: " + response.getCredentials().getExpiration());
            System.out.println("Access Key Id: " + response.getCredentials().getAccessKeyId());
            System.out.println("Access Key Secret: " + response.getCredentials().getAccessKeySecret());
            System.out.println("Security Token: " + response.getCredentials().getSecurityToken())
            return response;
        } catch (ClientException e) {
            System.out.println("Failed to get a token.");
            System.out.println("Error code: " + e.getErrCode());
            System.out.println("Error message: " + e.getErrMsg());
            return null
        }
    }
}
