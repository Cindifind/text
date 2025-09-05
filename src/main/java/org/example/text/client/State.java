package org.example.text.client;

public class State {
    String infName;
    String address;
    String CPU;
    String memory;
    String proName;

    public State() {
    }

    public State(String infName, String address, String CPU, String memory, String proName) {
        this.infName = infName;
        this.address = address;
        this.CPU = CPU;
        this.memory = memory;
        this.proName = proName;
    }

    /**
     * 获取
     * @return infName
     */
    public String getInfName() {
        return infName;
    }

    /**
     * 设置
     * @param infName
     */
    public void setInfName(String infName) {
        this.infName = infName;
    }

    /**
     * 获取
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * 设置
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取
     * @return CPU
     */
    public String getCPU() {
        return CPU;
    }

    /**
     * 设置
     * @param CPU
     */
    public void setCPU(String CPU) {
        this.CPU = CPU;
    }

    /**
     * 获取
     * @return memory
     */
    public String getMemory() {
        return memory;
    }

    /**
     * 设置
     * @param memory
     */
    public void setMemory(String memory) {
        this.memory = memory;
    }

    /**
     * 获取
     * @return proName
     */
    public String getProName() {
        return proName;
    }

    /**
     * 设置
     * @param proName
     */
    public void setProName(String proName) {
        this.proName = proName;
    }

    public String toString() {
        return "{infName = " + infName + ", address = " + address + ", CPU = " + CPU + ", memory = " + memory + ", proName = " + proName + "}";
    }
}
