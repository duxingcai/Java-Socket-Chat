package com.duxingcai.chat;


import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class SocketClient {

	private JFrame frame;
	private JTextArea area;
	private JLabel label;
	private JTextField field;
	private JButton button;
	private Socket socket;
	private String userName;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private static final Log log = LogFactory.getLog(SocketClient.class);

	public SocketClient() {
		frame=new JFrame("聊天室");
		area=new JTextArea(30,60);
		label=new JLabel();
		field=new JTextField(30);
		button=new JButton("发送");
		createConnection();
		init();
		addEventHandler();
	}



	private void createConnection() {
		do {
			String hostName=JOptionPane.showInputDialog(frame,"请输入服务器地址：8080");
			String port=JOptionPane.showInputDialog(frame,"请输入端口号：");
			try {
				socket=new Socket(hostName, Integer.parseInt(port));
				printWriter=new PrintWriter(socket.getOutputStream());
				bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "连接参数不正确，请重新输入");
			}
		}while(socket==null);
	}


	
	private void init() {
		field.setFont(new Font("", Font.BOLD, 20));
		area.setFont(new Font("", Font.BOLD, 24));
		JScrollPane jScrollPane=new JScrollPane(area);
		JPanel panel=new JPanel();
		panel.add(label);
		panel.add(field);
		panel.add(button);
		
		frame.add(jScrollPane,BorderLayout.CENTER);
		frame.add(panel, BorderLayout.SOUTH);
	}

	private void addEventHandler() {
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(field.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(frame, "不能发送空内容");
					return;
				}
				printWriter.println(userName+":"+field.getText());
				printWriter.flush();
				field.setText("");
			}
		});
		
	

		frame.addWindowFocusListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				/**
				 * 弹出对话框询问是否确定退出
				 * 服务器发送退出请求：userName+“：%GOODBYE%”
				 * 等待200毫秒，再关闭socket，退出程序
				 */
				int op=JOptionPane.showConfirmDialog(frame, "确定离开聊天室吗？"," 确认退出",
						JOptionPane.YES_NO_OPTION);
				if(op==JOptionPane.YES_OPTION) {
					printWriter.println(userName+":%GGODBYE%");
					log.info(userName+"离开聊天室!");
					printWriter.flush();
					try {
						Thread.sleep(200);
					}catch (Exception e) {
						e.printStackTrace();
					}finally {
						try {
							socket.close();
						}catch (IOException e) {}
						System.exit(0);
					}
				}	
			}
		});
	}
	
	
	
	public void showMe() {
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		new Thread() {
			public void run() {
				while(true) {
						try {
							String str=bufferedReader.readLine();
							area.append(str+"\n");
						}catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
		}.start();
	}

	public static void main(String[] args) {
		new SocketClient().showMe();
	}
}
