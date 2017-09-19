package com.spaceuptech.clientapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ClientApi {


    public final static String CALL = "api-call";
    private String ws;

    private OnMessageReceived onMessageReceived = null;
    private OnConnected onConnected = null;
    private OnDisconnected onDisconnected = null;

    private boolean sslAuthenticateHost = true;

    private Gson gson = new Gson();
    private boolean isConnected = false, hasRequested = false;
    private WebSocket socket;

    ClientApi(String host, int port, boolean sslEnable, OnMessageReceived onMessageReceived, OnConnected onConnected, OnDisconnected onDisconnected) {
        ws = "ws://" + host;
        if (sslEnable) {
            ws = "wss://" + host;
        }

        if (port > 0)
            ws += ":"+port;
        ws += "/api/proto/websocket";

        this.onMessageReceived = onMessageReceived;
        this.onConnected = onConnected;
        this.onDisconnected = onDisconnected;
    }

    public void setSslAuthenticateHost(boolean value) {
        sslAuthenticateHost = value;
    }

    private WebSocketListener listener = new WebSocketListener() {
        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {

        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            Log.d("api", "connected");

            hasRequested = false;
            isConnected = true;
            if (onConnected != null)
                onConnected.connected();
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException cause) throws InterruptedException {
            hasRequested = false;
            isConnected = false;
            Thread.sleep(5000);
            onDisconnected.disconnected();
            /*try {
                if (!hasRequested) {
                    hasRequested = true;
                    socket = socket.recreate().connectAsynchronously();
                    socket.addListener(listener);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws InterruptedException {
            isConnected = false;
            Thread.sleep(5000);
            onDisconnected.disconnected();/*
            try {
                if (!hasRequested) {
                    hasRequested = true;
                    socket = socket.recreate().connectAsynchronously();
                    socket.addListener(listener);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onTextMessage(WebSocket websocket, String message) throws Exception {}

        @Override
        public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
            Log.d("api", "connected");
            System.out.println("I got some bytes!");

            // Invoke user callback
            if (onMessageReceived != null) {
                Schema.Message message = Schema.Message.parseFrom(binary);
                switch (message.getType()) {
                    case FAAS:
                        Schema.Faas faas = message.getFaas();
                        onMessageReceived.messageReceived(new Request(faas.getEngine(), faas.getFunc(), faas.getArgs().toByteArray()));
                        break;
                }
            }
        }

        @Override
        public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

        }

        @Override
        public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

        }

        @Override
        public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {

        }

        @Override
        public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

        }

        @Override
        public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

        }

        @Override
        public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

        }

        @Override
        public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

        }

        @Override
        public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

        }

        @Override
        public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

        }
    };

    public void connect() {
        if (!isConnected && !hasRequested) {
            hasRequested = true;
            Log.d("API", "Connecting to server");
            try {
                socket = new WebSocketFactory().setVerifyHostname(sslAuthenticateHost).createSocket(ws);
                socket.connectAsynchronously();
                socket.addListener(listener);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnMessageReceived {
        void messageReceived(Request message);
    }

    public interface OnConnected {
        void connected();
    }

    public interface OnDisconnected {
        void disconnected();
    }

    public class Request {
        String engine, func;
        byte[] args;

        Request(String engine, String func, byte[] args) {
            this.engine = engine;
            this.func = func;
            this.args = args;
        }
    }

    public boolean isConnected() { return isConnected; }

    public void send(String engine, String func, byte[] args) {
        Schema.Faas faas = Schema.Faas.newBuilder()
                .setEngine(engine)
                .setFunc(func)
                .setArgs(ByteString.copyFrom(args))
                .build();
        Schema.Message message = Schema.Message.newBuilder()
                .setType(Schema.Message.Type.FAAS)
                .setFaas(faas)
                .build();

        socket.sendBinary(message.toByteArray());
    }

    public BroadcastReceiver requestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String engine = intent.getStringExtra("engine");
            String func = intent.getStringExtra("func");
            byte[] args = intent.getByteArrayExtra("args");

            // TODO: Avoid buffering certain engine:func pairs
            // TODO: Add buffering support

            send(engine, func, args);
        }
    };

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (isNetworkConnected(context)) connect();
        }
    }

    public NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    public static void call(Context context, String engine, String func, byte[] args) {
        Intent intent = new Intent(CALL);

        intent.putExtra("engine", engine);
        intent.putExtra("func", func);
        intent.putExtra("args", args);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}