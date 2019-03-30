package com.duxingcai.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.logging.*;



public class SocketServer {
	private ServerSocket serverSocket;
	private HashSet<Socket> allSocket;
	private static final Log LOG = LogFactory.getLog(SocketServer.class);
	
	


	public SocketServer() {
		try {
			serverSocket=new ServerSocket(8080);
			allSocket=new HashSet<Socket>();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	private void startService() throws IOException {
		while(true) {
			LOG.info("服务器成功启动，等待客户端相应.....");
			Socket s=serverSocket.accept();
			LOG.info(new Date()+" ip:"+s.getLocalAddress()+" "+"登入了聊天室！");
			allSocket.add(s);
			new ChatRoomServerThread(s).start();
		}
	}

	class ChatRoomServerThread extends Thread{
		private Socket socket;
	
		public ChatRoomServerThread(Socket s) {
			this.socket=s;
		}
	

		public void run(){
			/**
			*  得到s的输入流，并包装成BufferedReader
			*  循环不停的从BufferedReader中读取数据。
			*  每读到一行数据就将这一行数据转发给所有在线的客户端。
			*  循环遍历allSockets,得到每一个socket，
			*  然后得到该socket的输出流，并包装，再向输出流中写数据。
			*/
	
			BufferedReader br=null;
			try {
				br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
				while(true) {
					String str=br.readLine();
					if(str.split(":").equals("%GOODBYE%")) {
						allSocket.remove(socket);
						sendMessageToAllClient(str.split(":")[0]+",离开聊天室！");
						LOG.info(str.split(":")[0]+",离开聊天室！");
						socket.close();
						break;
					}
					sendMessageToAllClient(str);
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}//run end!
		
		public void sendMessageToAllClient(String str)throws IOException{
			Date date=new Date();
			SimpleDateFormat sf=new SimpleDateFormat("hh-mm-ss");
			String dateStr = sf.format(date);
			for(Socket temp:allSocket) {
				PrintWriter pWriter=new PrintWriter(temp.getOutputStream());
				pWriter.println(str+"\t["+dateStr+"]");
				pWriter.flush();
			}
		}
	}//Thread class end!

	public static void main(String[] args){

		try{
			new SocketServer().startService();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}

