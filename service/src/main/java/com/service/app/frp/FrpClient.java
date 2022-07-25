package com.service.app.frp;

import com.service.app.exceptions.FrpcClientException;

import com.service.app.frp.config.FrpClientConfig;
import com.service.app.frp.config.parts.CommonPart;
import com.service.app.frp.config.parts.TunnelPart;
import com.service.app.logging.log.Logger;
import com.service.app.logging.log.LoggerFactory;


import com.service.app.models.frp.FrpServerCredentials;
import com.service.app.models.frp.FrpTunnelConfig;
import com.service.app.network.FrpAdminRepository;

import org.apache.commons.io.FileUtils;

import java.util.Collections;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import frpclib.Frpclib;

public class FrpClient {

    private static final Logger logger = LoggerFactory.getLogger();

    private final String LOG_TAG = FrpClient.class.getName();

    //TODO: change to 'localhost'
    private final String ADMIN_ADDRESS = "0.0.0.0";
    private final int ADMIN_PORT = 7400;
    //TODO: possible solution, to generate user and password automatically
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASSWORD = "admin";

//    private final TunnelPart.HealthCheckType HEALTH_CHECK_TYPE = TunnelPart.HealthCheckType.TCP;
//    private final int HEALTH_CHECK_TIMEOUT_S = 3;
//    private final int HEALTH_CHECK_MAX_FAILED = 3;
//    private final int HEALTH_CHECK_INTERVAL_S = 1;

    private FrpAdminRepository frpAdminRepository =
            new FrpAdminRepository(ADMIN_ADDRESS, ADMIN_PORT, ADMIN_USER, ADMIN_PASSWORD);


    private FrpClientConfig config = new FrpClientConfig();
    private FrpClientConfig oldConfig = config.clone();
    private boolean isEnabled = false;
    private String configFilePath;
    private boolean isNotifyAboutStatus = true;

    private CompletableFuture<Void> frpcLibFuture;

    private Consumer<Boolean> clientStatusListener;

    public FrpClient(String configFilePath, Consumer<Boolean> clientStatusListener) {
        this.configFilePath = configFilePath + File.pathSeparator + "config.ini";

        this.clientStatusListener = clientStatusListener;

    }

    public void updateCredentials(FrpServerCredentials credentials)
            throws FrpcClientException {
        CommonPart commonPart = config.getCommonConfig();
        if (commonPart == null)
            commonPart = new CommonPart();

        commonPart.setServerAddress(credentials.getServerAddress());
        commonPart.setServerPort(credentials.getServerPort());
        commonPart.setToken(credentials.getToken());
        logger.d("comman", commonPart.formatPart());
        logger.d("File",this.configFilePath);
        config.setCommonConfig(commonPart);
        updateFrp();
    }

    public void setFrpTunnelConfig( String name,
    TunnelPart.TunnelType type,
    FrpTunnelConfig tunnelConfig) throws FrpcClientException {
        TunnelPart tunnelPart = config.getTunnelConfig(tunnelConfig.getTunnelName());
        if (tunnelPart == null)
            tunnelPart = new TunnelPart(tunnelConfig.getTunnelName(), type);
        tunnelConfig.setTunnelName(tunnelConfig.getTunnelName());
        tunnelPart.setLocalIp(tunnelConfig.getLocalIp());
        tunnelPart.setLocalPort(tunnelConfig.getLocalPort());

        tunnelPart.setGroup(tunnelConfig.getGroup());
        tunnelPart.setGroupKey(tunnelConfig.getGroupKey());


        tunnelPart.setAdminAddress(ADMIN_ADDRESS);
        tunnelPart.setAdminUser(ADMIN_USER);
        tunnelPart.setAdminPassword(ADMIN_PASSWORD);
        tunnelPart.setAdminPort(ADMIN_PORT);
//        if(tunnelConfig.getextras().isEmpty()){}else{
//
//
//        for(Map.Entry<String, String> e : tunnelConfig.getextras().entrySet()) {
//            String key = e.getKey();
//            String value = e.getValue();
//            tunnelPart.addField(key, value);
//        }
//        }
        tunnelPart.setCustomDomain(tunnelConfig.getCustomDomain());
        tunnelPart.setHealthType(tunnelConfig.getHealthType());
        tunnelPart.setHealthCheckTimeoutS(tunnelConfig.getHealthTimeout());
        tunnelPart.setHealthCheckMaxFailed(tunnelConfig.getHealthFailed());
        tunnelPart.setHealthCheckIntervalS(tunnelConfig.getHealthInterval());


        //tunnelPart.setBandwidthLimit(
        //        new TunnelPart.BandwidthLimit(100, TunnelPart.BandwidthLimitType.KILOBYTES));
        logger.d("tunnel", tunnelPart.formatPart());
        config.addTunnelConfig(tunnelPart);
        updateFrp();
    }

    public void removeFroTunnelConfig(String name) throws FrpcClientException {
        config.removeTunnelConfig(name);
        updateFrp();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void stop() throws FrpcClientException {
        try {
            frpAdminRepository.stop().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new FrpcClientException(
                    "Failed to stop FRPC client. Cause: " + e.getMessage());
        }
    }

    private void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        if (clientStatusListener != null && isNotifyAboutStatus)
            clientStatusListener.accept(isEnabled);
    }

    private void start() throws FrpcClientException {
        if (!isEnabled) {
            try {
                frpcLibFuture = CompletableFuture.supplyAsync(() -> {
                    logger.i(LOG_TAG, "Started Frpclib");
                    String error = Frpclib.run(configFilePath);
                    setEnabled(false);
                    if (error != null && !error.isEmpty()) {
                        logger.e(LOG_TAG, "I guess this is the message");
                        logger.e(LOG_TAG, error);
                        throw new CompletionException(
                                new FrpcClientException(
                                        "Failed to start FRPC client. Cause: " + error));
                    }
                    logger.i(LOG_TAG, "Stopped Frpclib");
                    return null;
                });
                frpcLibFuture.get(1, TimeUnit.SECONDS);
            } catch (CompletionException | ExecutionException exc) {
                Throwable cause = exc.getCause();
                if (cause instanceof FrpcClientException)
                    throw (FrpcClientException) cause;
            } catch (InterruptedException | TimeoutException e) {
                setEnabled(true);
            }
        }
    }

    private void updateFrp() throws FrpcClientException {
        if (!(oldConfig.equals(config))) {
            if (!writeNewConfig(config))
                return;

            if (isEnabled) {
                String reloadExceptionStr = "Failed to reload FRPC client. Cause: ";


                    try {
                        isNotifyAboutStatus = false;
                        stop();
                        while (isEnabled) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        start();
                        isNotifyAboutStatus = true;
                    } catch (FrpcClientException exc) {
                        isNotifyAboutStatus = true;
                        if (!writeNewConfig(oldConfig))
                            setEnabled(false);
                        else
                            try {
                                start();
                            } catch (FrpcClientException e) {
                                logger.e(
                                        LOG_TAG,
                                        "Failed to restart FRPC client with old configuration. Cause: " +
                                                e.getMessage());
                            }

                        throw new FrpcClientException(reloadExceptionStr + exc.getMessage());
                    }

            } else {
                isNotifyAboutStatus = true;
                start();
            }
         oldConfig = config;
        }else{
            logger.d("Exception","Old File is Same as New File");
        }
    }


    private boolean writeNewConfig(FrpClientConfig config) {
        String fileContent = config.format();
        File configFile = new File(configFilePath);


        try {
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    logger.e(LOG_TAG, "Config file could not be created");
                    return false;
                }
            }
            PrintWriter writer = new PrintWriter(configFile.getPath());
           // FileUtils.writeLines(configFile, Collections.singleton(fileContent));
            writer.print(fileContent);
            writer.close();
        } catch (IOException e) {

            logger.e(LOG_TAG, e.getMessage());
            return false;
        }
        return true;
    }

}
