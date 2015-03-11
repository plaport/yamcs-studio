package org.csstudio.platform.libs.yamcs.ws;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.protostuff.JsonIOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.YamcsConnectionProperties;
import org.yamcs.protostuff.NamedObjectList;

/**
 * Netty-implementation of a Yamcs web socket client.
 * Extracted out of YamcsDataSource, since instances of that DS
 * appear to be created for every parameter separately :-/
 * Needs more research. would've thought everything under
 * same schema shares a datasource, but alas.
 *
 * TODO get this completely clean of any payload information. Should be more generic. therefore
 * transfer actual differentiating logic to the OutgoingEvent extensions.
 */
public class WebSocketClient {

    private static final Logger log = Logger.getLogger(WebSocketClient.class.getName());

    private WebSocketClientCallbackListener callback;
    private EventLoopGroup group = new NioEventLoopGroup();
    private URI uri;
    private Channel nettyChannel;
    private String userAgent;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private AtomicBoolean enableReconnection = new AtomicBoolean(true);
    private AtomicInteger seqId = new AtomicInteger(1);

    // Stores ws subscriptions to be sent to the server once ws-connection is established
    private BlockingQueue<OutgoingEvent> pendingOutgoingEvents = new LinkedBlockingQueue<>();

    // Sends outgoing subscriptions to the web socket
    //private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    // Keeps track of sent subscriptions, so that we can do a resend when we get
    // an InvalidException on some of them :-(
    private ConcurrentHashMap<Integer, NamedObjectList> upstreamSubscriptionsBySeqId = new ConcurrentHashMap<>();

    // TODO we should actually wait for the ACK to arrive, before sending the next request
    public WebSocketClient(YamcsConnectionProperties yprops, WebSocketClientCallbackListener callback) {
        this.uri = yprops.webSocketURI();
        this.callback = callback;
        new Thread(() -> {
            try {
                OutgoingEvent evt;
                while((evt = pendingOutgoingEvents.take()) != null) {
                    // We now have at least one event to handle
                    Thread.sleep(500); // Wait for more events, before going into synchronized block
                    synchronized(pendingOutgoingEvents) {
                        while(pendingOutgoingEvents.peek() != null
                                && evt.canMergeWith(pendingOutgoingEvents.peek())) {
                            OutgoingEvent otherEvt = pendingOutgoingEvents.poll();
                            evt = evt.mergeWith(otherEvt); // This is to counter bursts.
                        }
                    }

                    // Good, send the merged result
                    if (evt instanceof ParameterSubscribeEvent) {
                        doParameterSubscribe(((ParameterSubscribeEvent) evt).getIdList());
                    } else if (evt instanceof ParameterUnsubscribeEvent) {
                        doParameterUnsubscribe(((ParameterUnsubscribeEvent) evt).getIdList());
                    } else if (evt instanceof SubscribeAllCommandHistoryRequest) {
                        doSubscribeAllCommandHistory();
                    }
                }
            } catch(InterruptedException e) {
                log.log(Level.SEVERE, "OOPS, got interrupted", e);
            }
        }).start();
    }

    /**
     * Formatted as app/version. No spaces.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void connect() {
        enableReconnection.set(true);
        createBootstrap();
    }

    void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public boolean isConnected() {
        return connected.get();
    }

    private void createBootstrap() {
        HttpHeaders header = new DefaultHttpHeaders();
        if (userAgent != null) {
            header.add(HttpHeaders.Names.USER_AGENT, userAgent);
        }

        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, false, header);
        WebSocketClientHandler webSocketHandler = new WebSocketClientHandler(handshaker, this, callback);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        new HttpClientCodec(),
                        new HttpObjectAggregator(8192),
                        //new WebSocketClientCompressionHandler(),
                        webSocketHandler);
            }
        });

        log.info("WebSocket Client connecting");
        try {
            ChannelFuture future = bootstrap.connect(uri.getHost(), uri.getPort()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        // Set-up reconnection attempts every second during initial set-up.
                        log.info("reconnect..");
                        group.schedule(() -> createBootstrap(), 1L, TimeUnit.SECONDS);
                    }
                }
            });

            future.sync();
            nettyChannel = future.sync().channel();
        } catch (InterruptedException e) {
            System.out.println("interrupted while trying to connect");
            e.printStackTrace();
        }
    }

    /**
     * Adds said event to the queue. As soon as the web socket
     * is established, queue will be iterated and if possible, similar events will be merged.
     */
    public void sendRequest(OutgoingEvent request) {
        // sync, because the consumer will try to merge multiple outgoing events of the same type
        // using multiple operations on the queue.
        synchronized(pendingOutgoingEvents) {
            pendingOutgoingEvents.offer(request);
        }
    }

    private void doParameterSubscribe(NamedObjectList idList) { // TODO we could probably refactor this into ParameterSubscribeEvent
        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        try {
            JsonIOUtil.writeTo(barray, idList, idList.cachedSchema(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("will send subscribe of .. " + barray.toString());

        int id = seqId.incrementAndGet();
        upstreamSubscriptionsBySeqId.put(id, idList);
        nettyChannel.writeAndFlush(new TextWebSocketFrame(
                new StringBuilder("[").append(WSConstants.PROTOCOL_VERSION)
                .append(",").append(WSConstants.MESSAGE_TYPE_REQUEST)
                .append(",").append(id)
                .append(",")
                .append("{\"request\":\"subscribe\",\"data\":")
                .append(barray.toString()).append("}]").toString()));
    }

    private void doParameterUnsubscribe(NamedObjectList idList) { // TODO we could probably refactor this into ParameterSubscribeEvent
        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        try {
            JsonIOUtil.writeTo(barray, idList, idList.cachedSchema(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("will send unsubscribe of .. " + barray.toString());

        int id = seqId.incrementAndGet();
        //upstreamSubscriptionsBySeqId.put(id, idList);
        nettyChannel.writeAndFlush(new TextWebSocketFrame(
                new StringBuilder("[").append(WSConstants.PROTOCOL_VERSION)
                .append(",").append(WSConstants.MESSAGE_TYPE_REQUEST)
                .append(",").append(id)
                .append(",")
                .append("{\"request\":\"unsubscribe\",\"data\":")
                .append(barray.toString()).append("}]").toString()));
    }

    private void doSubscribeAllCommandHistory() {
        System.out.println("will send subscribe-all cmdhist");

        int id = seqId.incrementAndGet();
        nettyChannel.writeAndFlush(new TextWebSocketFrame(
                new StringBuilder("[").append(WSConstants.PROTOCOL_VERSION)
                .append(",").append(WSConstants.MESSAGE_TYPE_REQUEST)
                .append(",").append(id)
                .append(",")
                .append("{\"cmdhistory\":\"subscribe\"}]").toString()));
    }

    NamedObjectList getUpstreamSubscription(int seqId) {
        return upstreamSubscriptionsBySeqId.get(seqId);
    }

    void ackSubscription(int seqId) {
        upstreamSubscriptionsBySeqId.remove(seqId);
    }

    boolean isReconnectionEnabled() {
        return enableReconnection.get();
    }

    public void disconnect() {
        if (connected.compareAndSet(true, false)) {
            enableReconnection.set(false);
            log.info("WebSocket Client sending close");
            nettyChannel.writeAndFlush(new CloseWebSocketFrame());

            // WebSocketClientHandler will close the channel when the server responds to the CloseWebSocketFrame
            nettyChannel.closeFuture().awaitUninterruptibly();
        } else {
            log.fine("Close requested, but connection was already closed");
        }
    }

    public void shutdown() {
        //exec.shutdown();
        group.shutdownGracefully();
    }
}
