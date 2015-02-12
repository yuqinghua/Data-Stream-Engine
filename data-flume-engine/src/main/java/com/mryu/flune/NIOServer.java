package com.mryu.flune;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import com.mryu.flune.action.LoginAction;
import com.mryu.flune.socket.Response;

import net.sf.json.JSONObject;

public class NIOServer {
	private int port = 8888;
	// 解码buffer
	private Charset cs = Charset.forName("UTF-8");
	/* 接受数据缓冲区 */
	private static ByteBuffer sBuffer = ByteBuffer.allocate(1024);
	/* 发送数据缓冲区 */
	private static ByteBuffer rBuffer = ByteBuffer.allocate(1024);
	
	private static Set<SocketChannel> loginChannelSet = new HashSet<SocketChannel>();
	
	private static Selector selector;

	public NIOServer(int port) {
		this.port = port;
		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(port));
			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("server start on port:" + port);;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 服务器端轮询监听，select方法会一直阻塞直到有相关事件发生或超时
	 */
	private void listen() {
		while (true) {
			try {
				selector.select();// 返回值为本次触发的事件数
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				for (SelectionKey key : selectionKeys) {
					handle(key);
				}
				selectionKeys.clear();// 清除处理过的事件
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}
	}

	/**
	 * 处理不同的事件
	 */
	private void handle(SelectionKey selectionKey) throws IOException {
		ServerSocketChannel server = null;
		SocketChannel client = null;
		String receiveText = null;
		try {
			int count = 0;
			if (selectionKey.isAcceptable()) {
				/*
				 * 客户端请求连接事件 serversocket为该客户端建立socket连接，将此socket注册READ事件，监听客户端输入
				 * READ事件：当客户端发来数据，并已被服务器控制线程正确读取时，触发该事件
				 */
				server = (ServerSocketChannel) selectionKey.channel();
				client = server.accept();
				client.configureBlocking(false);
				client.register(selector, SelectionKey.OP_READ);
			} else if (selectionKey.isReadable()) {
				/*
				 * READ事件，收到客户端发送数据，读取数据后继续注册监听客户端
				 */
				client = (SocketChannel) selectionKey.channel();
				rBuffer.clear();
				count = client.read(rBuffer);
				if (count > 0) {
					rBuffer.flip();
					receiveText = String.valueOf(cs.decode(rBuffer).array());
					dispatch(client, receiveText);
					client = (SocketChannel) selectionKey.channel();
					client.register(selector, SelectionKey.OP_READ);
				}
			} else if (!selectionKey.isValid()) {
				if(loginChannelSet.contains(client)){
					loginChannelSet.remove(client);
				}
			}
		} catch (Exception e) {
			if(client != null){
				client.close();
			}
		}
	}

	private void dispatch(SocketChannel client, String info) throws IOException {
		System.out.println("Client Port:" + client.socket().getPort());
		System.out.println("Client IP:" + client.socket().getInetAddress());
		System.out.println("Client Text:" + info);
		try {
			JSONObject json = JSONObject.fromObject(info);
			String action = json.getString("action");
			if(action== null || action.trim().equals("")){
				sendResponse_1(client);
			}
			if(!loginChannelSet.contains(client)){
				if(action.equalsIgnoreCase("login")){
					LoginAction obj = (LoginAction) JSONObject.toBean(json.getJSONObject("obj"), LoginAction.class);
					sendResponse_0(client);
					loginChannelSet.add(client);
				}else{
					sendResponse_2(client);
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
			sendResponse_1(client);
		}
		
	}

	private void sendResponse(SocketChannel client, Response response) throws IOException{
		sBuffer.clear();
		sBuffer.put(JSONObject.fromObject(response).toString().getBytes());
		sBuffer.flip();
		client.write(sBuffer);
	}
	private void sendResponse_0(SocketChannel client) throws IOException{
		sendResponse(client, new Response(0, "Login Success!", new JSONObject()));
	}
	private void sendResponse_1(SocketChannel client) throws IOException{
		sendResponse(client, new Response(1, "Unknown Action!", new JSONObject()));
	}
	private void sendResponse_2(SocketChannel client) throws IOException{
		sendResponse(client, new Response(2, "Please login first!", new JSONObject()));
	}
	
	public static void main(String[] args) throws IOException {
		NIOServer server = new NIOServer(7777);
		server.listen();
	}

}
