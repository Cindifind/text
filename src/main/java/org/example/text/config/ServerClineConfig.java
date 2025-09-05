package org.example.text.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "server.cline")
public class ServerClineConfig {
    private String port;
    private String server;
    private String pubIp;

    public ServerClineConfig() {
    }

    public ServerClineConfig(String port, String server, String pubIp) {
        this.port = port;
        this.server = server;
        this.pubIp = pubIp;
    }

    /**
     * 获取
     * @return port
     */
    public String getPort() {
        return port;
    }

    /**
     * 设置
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * 获取
     * @return server
     */
    public String getServer() {
        return server;
    }

    /**
     * 设置
     * @param server
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * 获取
     * @return pubIp
     */
    public String getPubIp() {
        return pubIp;
    }

    /**
     * 设置
     * @param pubIp
     */
    public void setPubIp(String pubIp) {
        this.pubIp = pubIp;
    }

    public String toString() {
        return "ServerClineConfig{port = " + port + ", server = " + server + ", pubIp = " + pubIp + "}";
    }
}
