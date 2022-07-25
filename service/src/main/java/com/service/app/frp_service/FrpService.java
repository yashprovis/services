package com.service.app.frp_service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.github.kittinunf.fuel.android.BuildConfig;
import com.service.app.android_service.ForegroundService;
import com.service.app.exceptions.FrpcClientException;
import com.service.app.frp.FrpClient;
import com.service.app.frp.config.FrpClientConfig;
import com.service.app.frp.config.parts.CommonPart;
import com.service.app.frp.config.parts.TunnelPart;
import com.service.app.models.frp.FrpServerCredentials;
import com.service.app.models.frp.FrpTunnelConfig;
import com.service.app.network.FrpAdminRepository;
import com.service.app.network.RootRepository;
import com.service.app.utils.Mapper;

import org.json.JSONObject;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import frpclib.Frpclib;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;

public class FrpService {
    private static final AttributeKey<String> CONNECTED_URL = AttributeKey.valueOf("connected_url");
    private final static int FIRST_LOCAL_PORT = 4000;
    private final static String TUNNEL_NAME_PREFIX = "tunnel_";
    private final static int AUTO_UPDATE_INTERVAL_SECONDS = 10;
    private CompletableFuture<Void> frpcLibFuture;

    private final RootRepository rootRepository;
    private final Mapper mapper = new Mapper();
    private final ScheduledExecutorService updateScheduler =
            Executors.newScheduledThreadPool(1);
    private HttpProxyServer httpServer;
    public FrpClient frpClient;

    private FrpServerCredentials frpServerCredentials;
    private Map<String, FrpExtras> frpServerMap = new HashMap<>();

    private ClientStatus clientStatus;
    private Consumer<ClientStatus> clientStatusListener;

    private ScheduledFuture updateScheduledFuture;
    private boolean isAutoUpdate = false;
    private FrpAdminRepository frpAdminRepository =
            new FrpAdminRepository("0.0.0.0", 7400, "admin", "admin");
    private boolean isStopped = false;

    private FrpService(String configFilePath, Supplier<Context> applicationContextProvider) {
        frpClient = new FrpClient(
                configFilePath,
                (state) -> {
                    if (!isAutoUpdate || isStopped || state) {
                        setClientStatus(state ? ClientStatus.RUNNING : ClientStatus.STOPPED);
                        if (state)
                            refreshAutoUpdateState();
                    }
                });
        rootRepository = new RootRepository(applicationContextProvider);
    }

    public void setClientStatusListener(Consumer<ClientStatus> clientStatusListener) {
        this.clientStatusListener = clientStatusListener;
    }

    public FrpServerCredentials getFrpServerCredentials() {
        return frpServerCredentials;
    }

    public ArrayList<FrpTunnelConfig> getOpenedFrpTunnels() {
        return frpServerMap
                .values()
                .stream()
                .map(FrpExtras::getConfig)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    String fileString="";

    public CompletableFuture<Void> loadFrpServerCredentials(String tunnelNames,Context context) {
        return rootRepository.getFrpServerCredentials()
                .thenAcceptAsync((response) -> {

                    String name = response.getName();
                    FrpClientConfig config = new FrpClientConfig();

                    TunnelPart part = new TunnelPart(name, TunnelPart.TunnelType.HTTPS);
//                    part.setLocalIp(response.getLocalIp());
//                    part.setLocalPort(response.getLocalPort());
//                    part.setGroup(response.getGroup());
//                    part.setGroupKey(response.getGroupKey());
//                    part.setAdminAddress(response.getAdminAddr());
//                    part.setAdminUser(response.getAdminUser());
//                  //  part.setAdminPassword(response.getAdminPass());
//                    part.setAdminPort(response.getAdminPort());
//                    part.setCustomDomain(response.getCustomDomain());
//                    part.setHealthType(response.getHealthCheckType());
//                    part.setHealthCheckTimeoutS(response.getHealthCheckTimeout());
//                    part.setHealthCheckMaxFailed(response.getHealthCheckMaxFailed());
//                    part.setHealthCheckIntervalS(response.getHealthCheckInterval());
                    CommonPart commonPart = new CommonPart();
                    if(response.getComman().isEmpty()){}else{
                        response.getComman().replace("token" ,"\""+response.getComman().get("token")+"\"");

                        for(Map.Entry<String, String> e : response.getComman().entrySet()) {
                            String key = e.getKey();
                            String value = e.getValue();

                            commonPart.putField(key, value);
                        }
                    }

        if(response.getExtras().isEmpty()){}else{
            response.getExtras().replace("group_key" ,"\""+response.getExtras().get("group_key")+"\"");

        for(Map.Entry<String, String> e : response.getExtras().entrySet()) {
            String key = e.getKey();
            String value = e.getValue();

            part.addField(key, value);
        }
        }

                    config.setCommonConfig(commonPart);
                    config.addTunnelConfig(part);

//                    final PackageManager pm = context.getPackageManager();
//                    ApplicationInfo ai = null;
//                    try {
//                        ai = pm.getApplicationInfo( context.getPackageName(), 0);
//                    } catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    Log.i("package name",context.getPackageName());

                    String fileContent = config.format();

                    File configFile = new File("/storage/emulated/0/Android/data/"+context.getPackageName() +"/files"+ File.pathSeparator + "config.ini");
                    PrintWriter writer = null;
                    try {
                        writer = new PrintWriter(configFile.getPath());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    // FileUtils.writeLines(configFile, Collections.singleton(fileContent));
                    writer.print(fileContent);
                    writer.close();
                    if(httpServer!=null)
                        Log.d("okay","okay");
                    try {

                        if(!fileString.equals(fileContent) || !fileString.contains(name)) {
                            Log.i("currently", "currently here");
                            if (!fileString.equals("")) {
                                //  frpClient.stop();

//
                                frpAdminRepository.reload().get();
                                fileString = fileContent;

                            } else {
//


                                fileString = fileContent;
                                start(configFile.getPath());

//
                                httpServer = DefaultHttpProxyServer
                                        .bootstrap()
                                        .withPort(4000)
                                        .withFiltersSource(new HttpFiltersSourceAdapter() {
                                            @Override
                                            public HttpFilters filterRequest(
                                                    HttpRequest originalRequest,
                                                    ChannelHandlerContext ctx) {
                                                String uri = originalRequest.getUri();
                                                originalRequest.headers().add(HttpHeaders.Names.HOST, "anyhost.com");
                                                if (originalRequest.getMethod() == HttpMethod.CONNECT) {
                                                    if (ctx != null) {
                                                        String prefix = "https://" +
                                                                uri.replaceFirst(":443$", "");
                                                        ctx.channel().attr(CONNECTED_URL).set(prefix);
                                                    }
                                                    return new FrpExtras.MyHttpFilters(originalRequest, ctx, null);
                                                }
                                                String connectedUrl = ctx.channel().attr(CONNECTED_URL).get();
                                                if (connectedUrl == null) {
                                                    return new FrpExtras.MyHttpFilters(originalRequest, ctx, uri);
                                                }
                                                return new FrpExtras.MyHttpFilters(originalRequest, ctx, connectedUrl + uri);
                                            }
                                        })
                                        //.withTransparent(true)
                                        .start();
                            }
                        } else{
                            Log.e("diff config","never ");
                        }

                    } catch (FrpcClientException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
    }
    private void start(String path) throws FrpcClientException {

        try {
            frpcLibFuture = CompletableFuture.supplyAsync(() -> {
                Log.i("LOG_TAG", "Started Frpclib");
                String error = Frpclib.run(path);

                if (error != null && !error.isEmpty()) {
                    Log.e("LOG_TAG", "I guess this is the message");
                    Log.e("LOG_TAG", error);
                    throw new CompletionException(
                            new FrpcClientException(
                                    "Failed to start FRPC client. Cause: " + error));
                }
                Log.i("LOG_TAG", "Stopped Frpclib");
                return null;
            });

            frpcLibFuture.get(1, TimeUnit.SECONDS);
        } catch (CompletionException | ExecutionException exc) {
            Throwable cause = exc.getCause();
            if (cause instanceof FrpcClientException)
                throw (FrpcClientException) cause;
        } catch (InterruptedException | TimeoutException e) {
            System.out.println(e);
        }

    }
    public CompletableFuture<Void> loadFrpServerCredentials(
            FrpServerCredentials frpServerCredentials) {
        return CompletableFuture.runAsync(() -> {
            this.frpServerCredentials = frpServerCredentials;
            try {
                frpClient.updateCredentials(frpServerCredentials);
            } catch (FrpcClientException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> loadTunnel(FrpTunnelConfig frpTunnelConfig) {
        String name = frpTunnelConfig.getTunnelName();
        if (name == null)
            name = TUNNEL_NAME_PREFIX + frpTunnelConfig.getRemotePort();
        //TODO: remove when we will be receiving group and group key
        if (frpTunnelConfig.getGroup() == null)
            frpTunnelConfig.setGroup("android");
        if (frpTunnelConfig.getGroupKey() == null)
            frpTunnelConfig.setGroupKey("1234");
        try {
            frpClient.setFrpTunnelConfig(
                    name,
                    TunnelPart.TunnelType.HTTPS,
                    frpTunnelConfig);
        } catch (FrpcClientException e) {
            throw new CompletionException(e);
        }
        return loadTunnel(name, frpTunnelConfig);
    }

    public CompletableFuture<Void> loadTunnel(String name, FrpTunnelConfig frpTunnelConfig) {
        frpTunnelConfig.setTunnelName(name);
        if (name == null)
            return loadTunnel(frpTunnelConfig);
        else
            return CompletableFuture.runAsync(() -> {
//
FrpExtras oldServer = frpServerMap.get(name);

                if (frpTunnelConfig.getLocalPort() == null) {
                    frpTunnelConfig.setLocalPort(oldServer != null ?
                            oldServer.getConfig().getLocalPort() :
                            getFreeLocalPort());
                }
                if (frpTunnelConfig.getTunnelName() != null)
                    frpTunnelConfig.setTunnelName(name);

//
                    frpServerMap.put(name, new FrpExtras(frpTunnelConfig));

                try {
                    frpClient.setFrpTunnelConfig(
                            name,
                            TunnelPart.TunnelType.HTTPS,
                            frpTunnelConfig);
                } catch (FrpcClientException e) {
                    throw new CompletionException(e);
                }
            });
    }


    public CompletableFuture<Void> stop() {
        isStopped = true;
        return CompletableFuture.runAsync(() -> {
            frpServerMap.values().forEach(FrpExtras::stopService);
            try {
                setClientStatus(ClientStatus.STOPPED);
                stopAutoUpdate();
                frpClient.stop();
            } catch (FrpcClientException e) {
                throw new CompletionException(e);
            }
        });
    }

    public boolean isAutoUpdate() {
        return updateScheduledFuture != null;
    }

    public void enableAutoUpdate() {
        if (!isAutoUpdate) {
            isAutoUpdate = true;
            setupAutoUpdate();
        }
    }

    public void disableAutoUpdate() {
        if (isAutoUpdate) {
            isAutoUpdate = false;
            stopAutoUpdate();
        }
    }

    private void refreshAutoUpdateState() {
        if (isAutoUpdate)
            setupAutoUpdate();
        else
            stopAutoUpdate();
    }

    private void setupAutoUpdate() {
        if (updateScheduledFuture == null)
            updateScheduledFuture = updateScheduler.scheduleAtFixedRate(
                    () -> {
                        String tunnelName = frpServerMap
                                .values()
                                .stream()
                                .findFirst()
                                .flatMap((e) -> Optional.ofNullable(e.getConfig()))
                                .flatMap((e) -> Optional.ofNullable(e.getTunnelName()))
                                .orElse(null);

                        loadFrpServerCredentials(tunnelName,null);
                    },
                    AUTO_UPDATE_INTERVAL_SECONDS,
                    AUTO_UPDATE_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
    }

    private void stopAutoUpdate() {
        if (updateScheduledFuture != null && !updateScheduledFuture.isCancelled()) {
            updateScheduledFuture.cancel(true);
            updateScheduledFuture = null;
        }
    }

    private void setClientStatus(ClientStatus status) {
        ClientStatus oldStatus = clientStatus;
        clientStatus = status;
        if (clientStatusListener != null && oldStatus != clientStatus) {
            clientStatusListener.accept(status);
        }
    }

    private int getFreeLocalPort() {
        return getFreeLocalPort(FIRST_LOCAL_PORT);
    }

    private int getFreeLocalPort(int startFromPort) {
        //TODO: check is following port not already bind
        AtomicInteger currentPort = new AtomicInteger(startFromPort);

        Stream<FrpExtras> serverStream = frpServerMap.values().stream();
        while (serverStream.anyMatch((e) -> e.getConfig().getLocalPort() == currentPort.get()))
            currentPort.incrementAndGet();

        return currentPort.get();
    }

    public enum ClientStatus {
        RUNNING, STOPPED
    }

    public static class Builder {
        private String configPath;
        private String tunnelName;
        private FrpServerCredentials frpServerCredentials;
        private FrpTunnelConfig frpTunnelConfig;
        private Consumer<ClientStatus> clientStatusListener;
        private boolean isAutoUpdate;
        private Supplier<Context> applicationContextProvider;

        public Builder(String configPath, Supplier<Context> applicationContextProvider) {
            this.configPath = configPath;
            this.applicationContextProvider = applicationContextProvider;
        }

        public Builder setTunnelName(String tunnelName) {
            this.tunnelName = tunnelName;
            return this;
        }

        public Builder setFrpServerCredentials(FrpServerCredentials frpServerCredentials) {
            this.frpServerCredentials = frpServerCredentials;
            return this;
        }

        public Builder setFrpTunnelConfig(FrpTunnelConfig frpTunnelConfig) {
            this.frpTunnelConfig = frpTunnelConfig;
            return this;
        }

        public Builder setClientStatusListener(Consumer<ClientStatus> clientStatusListener) {
            this.clientStatusListener = clientStatusListener;
            return this;
        }

        public Builder setAutoUpdate(boolean isEnabled) {
            this.isAutoUpdate = isEnabled;
            return this;
        }

        public CompletableFuture<FrpService> build() {
            FrpService service = new FrpService(configPath, applicationContextProvider);
            if (clientStatusListener != null)
                service.setClientStatusListener(clientStatusListener);

            return CompletableFuture.supplyAsync(() -> {
                try {
                    if (frpServerCredentials == null)
                        service.loadFrpServerCredentials(tunnelName, applicationContextProvider.get()).get();
                    else {
                        service.loadFrpServerCredentials(frpServerCredentials).get();
                        if (frpTunnelConfig != null)
                            service.loadTunnel(tunnelName, frpTunnelConfig).get();
                    }
                    if (isAutoUpdate)
                        service.enableAutoUpdate();
                    else
                        service.disableAutoUpdate();

                    return service;
                } catch (InterruptedException | ExecutionException e) {
                    try {
                        service.stop().get();
                    } catch (ExecutionException | InterruptedException executionException) {
                        throw new CompletionException(e);
                    }
                    throw new CompletionException(e);
                }
            });
        }
    }

}
