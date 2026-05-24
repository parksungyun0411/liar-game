

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;

class LiarServer extends Thread implements ActionListener {
    ServerSocket ss;
    Socket s;
    int port = 3000;
    String portN;
    Vector<OneClientModul> v = new Vector<OneClientModul>();
    OneClientModul ocm;
    Thread gameThread = new Thread();
    Thread serverThread;
    ServerUi sui;
    String msg;
    String liarTopic = "10초초과";
    ArrayList voteList;

    LiarServer(ServerUi sui) {
        try {
            this.sui = sui;
            this.portN = sui.ui.port;
            port = Integer.parseInt(portN);
            ss = new ServerSocket(port);
            System.out.println(ss);
            sui.setTitle("ip: " + InetAddress.getLocalHost().getHostAddress() + ", port: " + port + " 서버관리자");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverThread = new Thread(this);
        serverThread.start();
        this.sui = sui;
        this.s = sui.s;
        act();
    }


    void kick() {
        String banId = String.valueOf(sui.idBox.getSelectedItem());
        for (OneClientModul ocm : v) {
            if (ocm.chatId.equals(banId)) {
                ocm.broadcast(ocm.chatId + "님이 강퇴당했습니다..");
                v.remove(ocm);
                ocm.closeAll();
                break;
            }
        }
    }

    @Override
    public void run() {
        if (currentThread().equals(serverThread)) {
            try {
                while (true) {
                    s = ss.accept();
                    OutputStream os = s.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    if (v.size() == 8) {
                        dos.writeUTF("false");
                    } else if (gameThread.isAlive() == true) {
                        dos.writeUTF("true");
                        dos.writeUTF("3초후");
                        System.out.println("enterfalse");
                    } else if (v.size() < 8) {
                        dos.writeUTF("true");
                        ocm = new OneClientModul(this);
                        sui.idBox.addItem(ocm.chatId);
                        v.add(ocm);
                        ocm.start();
                    }
                }
            } catch (IOException ie) {
                pln(port + "번 포트 사용중.");
            } finally {
                try {
                    if (ss != null) ss.close();
                    System.out.println("서버다운");
                } catch (IOException ie) {
                }
            }
        }
        if (currentThread().equals(gameThread)) {
            try {
                if (v.size() != 0) {
                    ocm.broadcast("3초후 게임을 시작합니다.");
                    sleep(1000);
                    ocm.broadcast("2초후 게임을 시작합니다.");
                    sleep(1000);
                    ocm.broadcast("1초후 게임을 시작합니다.");
                    sleep(1000);
                    voteList = new ArrayList();
                    System.out.println("겜메");
                    new GameManager(this);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void sleepTh(int i) {
        try {
            currentThread().sleep(i * 1000);
        } catch (InterruptedException e) {
        }
    }


    void act() {
        Action enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg = sui.chatTf.getText();
                msg = msg.trim();
                msg = "관리자 >> " + msg;
                sui.chatTf.setText(null);
                if (v.size() != 0) {
                    ocm.broadcast(msg);
                } else {
                    sui.ta.append("서버에 인원이 없습니다.\n");
                }
            }
        };
        sui.chatTf.addActionListener(enter);
        sui.banBtn.addActionListener(this);
        sui.startBtn.addActionListener(this);
        sui.endBtn.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(sui.banBtn)) {
            kick();
        }
        if (e.getSource().equals(sui.startBtn)) {
            if (v.size() != 0) {
                sui.startBtn.setEnabled(false);
                gameThread = new Thread(this);
                gameThread.start();
            }
        }
        if (e.getSource().equals(sui.endBtn)) {

            System.exit(0);

        }
    }

    void pln(String str) {
        System.out.println(str);
    }

    void p(String str) {
        System.out.print(str);
    }
}                                                                                               //라이어서버


class OneClientModul extends Thread {                                                           //원클모듈
    LiarServer ls;
    Socket s;
    InputStream is;
    OutputStream os;
    DataInputStream dis;
    DataOutputStream dos;
    String chatId;
    ServerUi sui;

    OneClientModul(LiarServer ls) {
        this.ls = ls;
        this.s = ls.s;
        this.sui = ls.sui;
        try {
            is = s.getInputStream();
            os = s.getOutputStream();
            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);
            System.out.println("ocm 입장");
            chatId = dis.readUTF();
            System.out.println("chatId : " + chatId);
            if (chatId.equals("enterfalse")) {
                closeAll();
            } else {
                String enterId = chatId;
                Boolean checkId = true;
                for (OneClientModul ocm : ls.v) {
                    if (ocm.chatId.equals(enterId)) {
                        System.out.println("중복 아이디 찾는중");
                        checkId = false;
                        continue;
                    }
                }
                if (checkId == false) {
                    System.out.println("중복아이디 있음");
                    dos.writeUTF("falseid");
                    System.out.println("falseid");
                    closeAll();
                } else {
                    System.out.println("주ㅡㅇ복아이디 없음");
                    dos.writeUTF("true");
                }
            }
        } catch (IOException ie) {
        }
    }

    public void run() {
        listen();
    }

    void listen() {
        String msg = "";
        int i;
        try {
            broadcast(chatId + " 님이 입장하셨습니다. (현재 인원: " + ls.v.size() + "명)");
            while (true) {
                msg = dis.readUTF();
                if (msg.startsWith("liarTopic")) {
                    if (msg != null) {
                        ls.liarTopic = msg.substring(9);
                    }
                    System.out.println(ls.liarTopic);
                } else if (msg.startsWith("cVote")) {
                    msg = msg.substring(5);
                    ls.voteList.add(msg);
                    System.out.println("투표 : " + msg);
                    if (ls.voteList.size() == ls.v.size()) {
                        ls.gameThread.interrupt();
                    }
                } else if (msg.equals("enterfalse")) {
                } else {
                    broadcast(msg);
                }
            }
        } catch (IOException ie) {
            ls.v.remove(this);
            broadcast(chatId + " 님이 퇴장하셨습니다. (현재 인원: " + ls.v.size() + "명)");
            ls.sui.idBox.removeItem(chatId);
        } finally {
            closeAll();
        }
    }

    void broadcast(String msg) {
        try {
            for (OneClientModul ocm : ls.v) {
                ocm.dos.writeUTF(msg);
                ocm.dos.flush();
            }
            if (msg.startsWith("gm")) {
                msg = msg.substring(2);
                sui.ta.append(msg + "\n");
                sui.sp.getVerticalScrollBar().setValue(sui.sp.getVerticalScrollBar().getMaximum());
            } else sui.ta.append(msg + "\n");
            sui.sp.getVerticalScrollBar().setValue(sui.sp.getVerticalScrollBar().getMaximum());
        } catch (IOException ie) {
        }
    }

    void closeAll() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (is != null) is.close();
            if (os != null) os.close();
            if (s != null) s.close();
        } catch (IOException ie) {
        }
    }
}