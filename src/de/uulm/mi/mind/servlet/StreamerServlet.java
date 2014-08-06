package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.logger.Messenger;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;

@ServerEndpoint(value = "/streamerServlet/{ip}")
public class StreamerServlet {
    // notice:not thread-safe
//	private static ArrayList<Session> sessionList = new ArrayList<Session>();

    private static HashMap<String, Session> sessionMap = new HashMap<String, Session>();

    Messenger log = Messenger.getInstance();
    String TAG = "StreamerServlet";
    public static StreamerServlet streamer;

    @OnOpen
    public void onOpen(@PathParam("ip") String ip, Session session) {
        streamer = this;

        try {
            sessionMap.put(ip, session);
            // asynchronous communication
//			System.out.println("PARAMETERMAP size: "+session.getRequestParameterMap().size());
//			for (String key : session.getRequestParameterMap().keySet()) {
//				System.out.println(key+":"+session.getRequestParameterMap().get(key));
//			}
            session.getBasicRemote().sendText("Hello from MIND StreamerServlet!");
        } catch (IOException e) {
        }
    }

    @OnClose
    public void onClose(@PathParam("ip") String ip, Session session) {
        sessionMap.remove(ip);
    }

    @OnMessage
    public void onMessage(@PathParam("ip") String ip, String msg) {

//		System.out.println("STREAMERSERVLET:"+msg+" from ip:"+ip);

        log.log(TAG, "onMessage from IP '" + ip + "' with message '" + msg + "'");

        try {
            if (sessionMap.get(msg) != null) {
                sessionMap.get(msg).getBasicRemote().sendText(ip);
            }
            //ip is ip that canceled; msg: "cancel:'ipToNotifyAboutCancel"
            else if (msg.startsWith("cancel:")) {
                String ipToNotifyAboutCancel = msg.substring(7);
                log.log(TAG, "try to send cancel message to '" + ipToNotifyAboutCancel + "' from ip '" + ip + "'");
                if (sessionMap.get(ipToNotifyAboutCancel) != null) {
                    log.log(TAG, "send cancel message to '" + ipToNotifyAboutCancel + "' from ip '" + ip + "'");
                    sessionMap.get(ipToNotifyAboutCancel).getBasicRemote().sendText("canceled:" + ip);
                }
            }

        } catch (IOException e) {
        }
    }
}
