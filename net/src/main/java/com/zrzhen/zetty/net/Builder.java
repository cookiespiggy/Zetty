package com.zrzhen.zetty.net;

import com.zrzhen.zetty.net.aio.ReadHandler;
import com.zrzhen.zetty.net.aio.WriteHandler;

import java.net.SocketOption;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenanlian
 * <p>
 * 构建通信服务配置类
 */
public class Builder {

    public SocketEnum socketType;

    public String host = "localhost";

    public int port = 80;

    public int readBufSize = 1024 * 8;

    public long socketReadTimeout = 30;

    public Map<SocketOption<Object>, Object> socketOptions;

    public ReadHandler readHandler;

    public WriteHandler writeHandler;

    public Decode decode;

    public Encode encode;

    public Processor processor;

    public Builder() {
    }

    public ZettyServer buildServer() {
        init();
        return new ZettyServer(this);
    }

    public ZettyClient buildClient() {
        init();
        return new ZettyClient(this);
    }

    private void init() {
        if (socketType == null) {
            socketType = SocketEnum.AIO;
        }

        if (readHandler == null) {
            readHandler = new ReadHandler();
        }

    }

    public Builder host(String host) {
        this.host = host;
        return this;
    }

    public Builder port(int port) {
        this.port = port;
        return this;
    }

    public Builder readBufSize(int readBufSize) {
        this.readBufSize = readBufSize;
        return this;
    }

    public Builder socketReadTimeout(long socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
        return this;
    }


    public Builder setOption(SocketOption socketOption, Object f) {
        if (socketOptions == null) {
            socketOptions = new HashMap<>(5);
        }
        socketOptions.put(socketOption, f);
        return this;
    }

    public Builder decode(Decode decode) {
        this.decode = decode;
        return this;
    }

    public Builder encode(Encode encode) {
        this.encode = encode;
        return this;
    }

    public Builder processor(Processor processor) {
        this.processor = processor;
        return this;
    }

    public Builder writeHandler(WriteHandler writeHandler) {
        this.writeHandler = writeHandler;
        return this;
    }

    public Builder socketType(SocketEnum socketType) {
        this.socketType = socketType;
        return this;
    }
}
