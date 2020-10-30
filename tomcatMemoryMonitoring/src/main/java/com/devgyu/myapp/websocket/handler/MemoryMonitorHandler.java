package com.devgyu.myapp.websocket.handler;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class MemoryMonitorHandler extends TextWebSocketHandler implements InitializingBean{
	
	private Set<WebSocketSession> sessionSet = new HashSet<WebSocketSession>();
	

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
		super.afterConnectionClosed(session, status);
		sessionSet.remove(session);
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		sessionSet.add(session);
	}
	
	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception{
		super.handleMessage(session, message);
	}
	
	@Override
	public boolean supportsPartialMessages() {
		return super.supportsPartialMessages();
	}
	
	private void sendMessage (String message) {
		for(WebSocketSession session: this.sessionSet) {
			if(session.isOpen()) {
				try {
					session.sendMessage(new TextMessage(message));
				}catch(Exception ignored) {
					ignored.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Thread thread = new Thread() {
			@Override
			public void run() {
				MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
				MemoryUsage memoryUsage = memoryBean.getHeapMemoryUsage();
				while(true) {
					try {
						long time = System.currentTimeMillis()+32400000;
						int committed = (int)(memoryUsage.getCommitted()/(1024*1024));
						int max = (int)(memoryUsage.getMax()/(1024*1024));
						int used = (int)(memoryUsage.getUsed()/(1024*1024));
						
						sendMessage ("[{\"time\":" + time + ", \"used\":" + used + "},"
								+ "{\"time\":" + time + ", \"max\":" + max + "},"
										+ "{\"time\":" + time + ", \"committed\":" + committed + "}]");
						Thread.sleep(5000);
					}catch(InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		};
		thread.start();
	}

}
