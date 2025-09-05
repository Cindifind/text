package org.example.text.service;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.example.text.client.Client;
import org.example.text.client.RegisterClient;
import org.example.text.client.State;
import org.example.text.config.ServerClineConfig;
import org.example.text.util.StageTimer;
import org.example.text.util.SystemMonitor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;

import java.lang.reflect.Method;
import java.util.Map;

// 移除 @Service 注解，让自动配置类来管理 Bean
public class ClientAnnotationService implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ClientAnnotationService.class);
    private final ApplicationContext applicationContext;
    private final ServerClineConfig serverClineConfig;
    private final String applicationName;
    private final Integer serverPort;
    private boolean isRunning = false;
    public static JSONArray body = new JSONArray();

    // 添加构造函数
    public ClientAnnotationService(ApplicationContext applicationContext, ServerClineConfig serverClineConfig, String applicationName, Integer serverPort) {
        this.applicationContext = applicationContext;
        this.serverClineConfig = serverClineConfig;
        this.applicationName = applicationName;
        this.serverPort = serverPort;
    }

    @Override
    public void start() {
        // 扫描类级别的 @Client 注解
        scanClassLevelClients();

        // 扫描方法级别的 @Client 注解
        scanMethodLevelClients();
        //
        scanRegisterClient();

        isRunning = true;
    }

    private void scanRegisterClient() {
        Map<String, Object> beansWithClientAnnotation = applicationContext.getBeansWithAnnotation(RegisterClient.class);
        for (Object bean : beansWithClientAnnotation.values()) {
            RegisterClient clientAnnotation = bean.getClass().getAnnotation(RegisterClient.class);
            if (clientAnnotation != null) {
                beat();
            }
        }
    }

    private void scanClassLevelClients() {
        Map<String, Object> beansWithClientAnnotation = applicationContext.getBeansWithAnnotation(Client.class);

        for (Object bean : beansWithClientAnnotation.values()) {
            Client clientAnnotation = bean.getClass().getAnnotation(Client.class);
            if (clientAnnotation != null) {
                sendRequest(clientAnnotation);
            }
        }
    }

    private void scanMethodLevelClients() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Method[] methods = bean.getClass().getDeclaredMethods();

            for (Method method : methods) {
                Client clientAnnotation = method.getAnnotation(Client.class);
                if (clientAnnotation != null) {
                    sendRequest(clientAnnotation);
                }
            }
        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    private void sendRequest(Client client) {
        State state = new State();
        state.setProName(applicationName);
        state.setAddress(serverClineConfig.getPubIp() + ":" + serverPort + client.address());
        state.setInfName(client.name());
        body.put(new JSONObject(state));
    }


    //
    StageTimer stageTimer = new StageTimer();

    private void beat() {
//        log.info(body.toString());
        stageTimer.addTask(() -> {
            String url = serverClineConfig.getServer() + ":" + serverClineConfig.getPort() + "/register";
            System.out.println(url);
            for (int i = 0; i < body.length(); i++){
                body.getJSONObject(i).put("CPU", SystemMonitor.getCPU());
                body.getJSONObject(i).put("memory", SystemMonitor.getMemory());
            }
            try {
                HttpResponse<String> response = Unirest.post("http://"+url)
                        .header("Content-Type", "application/json")
                        .body(body.toString())
                        .asString();
                log.info(String.valueOf(response.getStatus()));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                log.error("false");
                log.warn(body.toString());
            }
        }, 30000L);
    }
}
