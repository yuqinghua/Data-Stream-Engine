package com.mryu.flune;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.mryu.flune.action.LoginAction;
import com.mryu.flune.core.ActionBean;
import com.mryu.flune.socket.Response;
  
public class NIOClient {  
	
	private static Logger logger = Logger.getLogger(NIOClient.class);
    /*发送数据缓冲区*/  
    private static ByteBuffer sBuffer = ByteBuffer.allocate(1024);  
    /*接受数据缓冲区*/  
    private static ByteBuffer rBuffer = ByteBuffer.allocate(1024);  
    /*服务器端地址*/  
    private InetSocketAddress serverAddress;  
    private static Selector selector;  
      
    
    public NIOClient(String host, int port, String key) {
        serverAddress = new InetSocketAddress(host, port);
        SocketChannel socketChannel = null;
        try {
			socketChannel = SocketChannel.open();  
			socketChannel.configureBlocking(false);  
			selector = Selector.open();  
			socketChannel.register(selector, SelectionKey.OP_CONNECT);  
			socketChannel.connect(serverAddress);
			boolean bool = doLogin(selector, socketChannel, new ActionBean("Login", new LoginAction(key)));
			if(bool){
				System.out.println("login Success!");
				doRequest(socketChannel);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(socketChannel != null){
				try {
					socketChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
    
    
	private void doRequest(SocketChannel socketChannel) {
		System.out.print("\n[cli] :");
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while(true){
			try {
				line = input.readLine();
				if(line == null || line.trim().equals("")){
					System.out.print("[cli] :");
					continue;
				}
				line = line.trim().toLowerCase();
				if(line.equals("exit") || line.equals("quit")){
					socketChannel.close();
					System.out.println("Good bye!");
					return;
				}
				
				if(line.equals("help")){
					System.out.print("\thelp\n[cli] :");
				}else if(line.equals("stop service")){
					
				}
				else{
					System.out.print("[cli] :");
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}


	private boolean doLogin(Selector selector, SocketChannel socketChannel, ActionBean actionBean) {
		Response response = doRequest(selector, socketChannel, actionBean);
		if(response == null || response.getCode() != 0){
			return false;
		}
		return true;
	}
	
	private Response doRequest(Selector selector, SocketChannel socketChannel, ActionBean actionBean){
		try {
			selector.select();
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			for(SelectionKey selectionKey : selectionKeys){
				if(selectionKey.isConnectable() && socketChannel.isConnectionPending()){
					socketChannel.finishConnect();
					sBuffer.put(JSONObject.fromObject(actionBean).toString().getBytes());
					sBuffer.flip();
					socketChannel.write(sBuffer);
					sBuffer.clear();
				}
			}
			socketChannel.register(selector, SelectionKey.OP_READ);
			selector.select();
			selectionKeys = selector.selectedKeys();
			int count;
			for(SelectionKey selectionKey : selectionKeys){
				try {
					if(selectionKey.isReadable()){
						rBuffer.clear();
						count = socketChannel.read(rBuffer);
						Response response = (Response) JSONObject.toBean(JSONObject.fromObject(new String(rBuffer.array(), 0, count)), Response.class);
						return response;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


    public static void main(String[] args) throws IOException {
    	String host = "localhost";
    	int port = 7777;
    	String key = "123456";
        new NIOClient(host, port, key);
    }  
} 