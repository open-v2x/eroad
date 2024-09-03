/**
 * Copyright (C), 2015-2019  http://www.buukle.top
 * FileName;ZKConfigure
 * Author;  zhanglei1102
 * Date;    2019/8/23 15:22
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package cn.eroad.trail.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhanglei1102
 * @description 〈〉
 * @create 2019/8/23
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "eroad-system")
public class FrameClientConfigureProperties {

    // 服务名称
    private String serverName;
    // 服务路径
    private String contextPath;
    // 是否开启会话认证
    private String openAuth;
    // 是否开启会话授权
    private String openPerm;
    // 是否开启租户隔离
    private String openIsol;
    // 是否开启接口认证
    private String openApiAuth;
    // 是否开启接口授权
    private String openApiPerm;
    // 全局会话的 cookie Domain
    private String cookieDomain;
    // 登机牌中心host
    private String passportHost;
    // boss私钥
    private String privateKey;

    /**
     * 路径配置
     */
    public static final String CONTEXT_PATH_ENVIRONMENT_KEY = "system.contextPath";
    //    public static final String COOKIE_DOMAIN_ENVIRONMENT_KEY = "boss.cookieDomain";
    public static final String SERVER_NAME_ENVIRONMENT_KEY = "system.serverName";
    public static final String SPRING_APPLICATION_NAME_KEY = "spring.application.name";

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getOpenAuth() {
        return openAuth;
    }

    public void setOpenAuth(String openAuth) {
        this.openAuth = openAuth;
    }

    public String getOpenPerm() {
        return openPerm;
    }

    public void setOpenPerm(String openPerm) {
        this.openPerm = openPerm;
    }

    public String getOpenIsol() {
        return openIsol;
    }

    public void setOpenIsol(String openIsol) {
        this.openIsol = openIsol;
    }

    public String getOpenApiAuth() {
        return openApiAuth;
    }

    public void setOpenApiAuth(String openApiAuth) {
        this.openApiAuth = openApiAuth;
    }

    public String getOpenApiPerm() {
        return openApiPerm;
    }

    public void setOpenApiPerm(String openApiPerm) {
        this.openApiPerm = openApiPerm;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public String getPassportHost() {
        return passportHost;
    }

    public void setPassportHost(String passportHost) {
        this.passportHost = passportHost;
    }


    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
