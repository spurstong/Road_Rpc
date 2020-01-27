package com.lwrpc.registry.data;

import java.io.Serializable;
import java.util.Objects;

public class URL implements Serializable {
    private String hostname;
    private Integer port;
    private String interfaceName;
    private String implClassName;

    public URL(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public URL(String hostname, Integer port, String interfaceName, String implClassName) {
        this.hostname = hostname;
        this.port = port;
        this.interfaceName = interfaceName;
        this.implClassName = implClassName;
    }

    public String getHostname() {
        return hostname;
    }


    public Integer getPort() {
        return port;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getImplClassName() {
        return implClassName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        URL url = (URL)obj;
        return Objects.equals(hostname, url.hostname) && Objects.equals(port, url.port) && Objects.equals(implClassName, url.implClassName);
    }

    @Override
    public String toString() {
        return this.hostname +":" + this.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, port, implClassName);
    }
}