

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class GameManager {

    String topic;
    String liar;
    Scanner sc;
    ArrayList<String> topicList;
    List<String> playerList = new ArrayList();
    LiarServer ls;
    HashSet<OneClientModul> ocmSet = new HashSet<>();

    GameManager(LiarServer ls) {
        System.out.println("겜메 들어옴");
        this.ls = ls;
        for (OneClientModul ocm : ls.v) {
            playerList.add(ocm.chatId);
        }
        setTopic();
        setLair();
        System.out.println(liar);
        System.out.println(topic);
        for (OneClientModul ocm : ls.v) {
            gm("liar:" + liar);
            gm("topic:" + topic);
        }
        oneChat();
        vote();
        liarSelect();
        unlockAll();
        ls.sui.startBtn.setEnabled(true);
    }

    void lockChat() {
        gm("채팅락");
    }

    void unlockAll() {
        gm("올언락");
    }

    void oneChat() {
        ls.ocm.broadcast("채팅이 잠깁니다.");
        ls.ocm.broadcast("10초 후 순서대로 주제를 한마디로 설명하세요.");
        lockChat();
        ls.sleepTh(10);
        ArrayList<Integer> idx = new ArrayList<>();
        Random r = new Random();
        System.out.println(ls.v.size());
        for (int i = 0; i < ls.v.size(); i++) {
            int z = r.nextInt(ls.v.size());
            idx.add(z);
            System.out.println(idx.get(i));
            for (int j = 0; j < i; j++) {
                if (idx.get(i) == idx.get(j)) {
                    idx.remove(i);
                    i--;
                }
            }
        }
        System.out.println(idx);
        for (int i = 0; i < ls.v.size(); i++) {
            ls.ocm.broadcast(ls.v.get(idx.get(i)).chatId+"님이 입력중입니다.");
            gm("채팅언락" + ls.v.get(idx.get(i)).chatId);
            ls.sleepTh(10);
        }
    }

    public void setTopic() {

        File topicListFile = new File("주제.txt");
        try {
            sc = new Scanner(topicListFile);
        } catch (FileNotFoundException e) {
            System.out.println("파일을 찾을 수 없습니다.");
        }

        topicList = new ArrayList<>();
        while (sc.hasNextLine()) {
            topicList.add(sc.nextLine());
        }

        Random topicGenerator = new Random();
        System.out.println(topicList.size());
        int topicListIndex = topicGenerator.nextInt(topicList.size());
        topic = topicList.get(topicListIndex);

    }

    public void setLair() {

        Random lairSelector = new Random();
        int listIndex = lairSelector.nextInt(playerList.size());
        liar = playerList.get(listIndex);

    }

    public void vote() {
        for (OneClientModul ocm : ls.v) {
            gm("list" + ocm.chatId);
        }
        gm("vote");
        ls.sleepTh(20);
        System.out.println("쓰레드깸");
    }

    void liarSelect() { //fffff
        int i = 0;
        String voteId = "";
        for (OneClientModul ocm : ls.v) {
            System.out.println(ls.voteList.size());
            int j = Collections.frequency(ls.voteList, ocm.chatId);
            System.out.println(j);
            if (j > i) {
                i = j;
                voteId = ocm.chatId;
                System.out.println(i);
            }
        }
        System.out.println("Max: " + voteId);
        if (liar.equals(voteId)) {
            gm("votecom" + voteId);
            ls.ocm.broadcast("라이어를 찾았습니다.");
            ls.ocm.broadcast("Liar: " + liar);
            ls.ocm.broadcast("라이어가 제시어를 추리중입니다.");
            ls.sleepTh(10);
            String liarTopic;
            liarTopic = ls.liarTopic;
            System.out.println(liarTopic);
            if (liarTopic.equals(topic)) {
                System.out.println("라이어승리");
                ls.ocm.broadcast("라이어승리");
                result("resultliarWin");
            } else if (liarTopic.equals("10초초과")) {
                ls.ocm.broadcast("라이어가 제한시간에 제시어를 입력하지 못했습니다.");
                ls.ocm.broadcast("라이어패배");
                result("resultliarLose");

            } else {
                ls.ocm.broadcast("라이어가 제시어를 맞히지 못햇습니다." +
                        "\n라이어 패배");
                ls.ocm.broadcast(" 제시어 : " + topic + "\n라이어가 입력한 제시어 : " + liarTopic);
                result("resultliarLose");
            }
            ls.liarTopic = "10초초과";
        } else {
            System.out.println("라이어승리");
            ls.ocm.broadcast("라이어를 찾아내지 못했습니다.\n Liar: " + liar);
            ls.ocm.broadcast("라이어승리");
            result("resultliarWin");
        }
        //result();
    }

    void result(String str) {
        gm(str);
        ocmSet.removeAll(ocmSet);
    }
    void gm(String str) {
        ls.ocm.broadcast("gm" + str);
    }

}