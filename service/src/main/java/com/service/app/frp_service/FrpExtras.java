package com.service.app.frp_service;

import com.service.app.models.frp.FrpTunnelConfig;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.util.stream.Stream;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;

public class FrpExtras {

    private static final AttributeKey<String> CONNECTED_URL = AttributeKey.valueOf("connected_url");

    private FrpTunnelConfig config;
    private HttpProxyServer extraServer;

    public FrpExtras(FrpTunnelConfig config) {
        setConfig(config);
    }

    public FrpTunnelConfig getConfig() {
        return config;
    }

    public void setConfig(FrpTunnelConfig config) {
        this.config = config;
        initServer();
    }

    public void stopService() {
        if (extraServer != null) {
            extraServer.stop();
            extraServer = null;
        }
    }

    private void initServer() {


        stopService();
        extraServer = DefaultHttpProxyServer
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
                            return new MyHttpFilters(originalRequest, ctx, null);
                        }
                        String connectedUrl = ctx.channel().attr(CONNECTED_URL).get();
                        if (connectedUrl == null) {
                            return new MyHttpFilters(originalRequest, ctx, uri);
                        }
                        return new MyHttpFilters(originalRequest, ctx, connectedUrl + uri);
                    }
                })
                //.withTransparent(true)
                .start();
    }

    public static class MyHttpFilters extends HttpFiltersAdapter {
        private static final String[] restrictedHeaders = {
                HttpHeaders.Names.FROM,
                HttpHeaders.Names.SERVER,
                HttpHeaders.Names.WWW_AUTHENTICATE,
                "Link",
                HttpHeaders.Names.CACHE_CONTROL,
                "Proxy-Connection",
                "X-Cache",
                "X-Cache-Lookup",
                HttpHeaders.Names.VIA,
                "X-Forwarded-For",
                HttpHeaders.Names.PRAGMA,
                "Keep-Alive",
        };

        private final String uri;

        public MyHttpFilters(HttpRequest originalRequest, ChannelHandlerContext ctx, String uri) {
            super(originalRequest, ctx);
            this.uri = uri;
        }

        public MyHttpFilters(HttpRequest originalRequest, String uri) {
            super(originalRequest);
            this.uri = uri;
        }

        @Override
        public HttpResponse proxyToServerRequest(HttpObject httpObject) {
            if (httpObject instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) httpObject;
                if (httpObject instanceof DefaultHttpMessage)
                    removeHeaders(request.headers());
            }
            return super.proxyToServerRequest(httpObject);
        }

        @Override
        public HttpObject proxyToClientResponse(HttpObject httpObject) {
            if (httpObject instanceof DefaultHttpMessage)
                removeHeaders(((DefaultHttpMessage) httpObject).headers());

            return super.proxyToClientResponse(httpObject);
        }

        private void removeHeaders(HttpHeaders httpHeaders) {
            Stream.of(restrictedHeaders).forEach(httpHeaders::remove);
        }
    }

}
