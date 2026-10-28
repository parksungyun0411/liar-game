import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TopicFileTest {

    // GameManager.setTopic() 과 같은 방식으로 읽는다. Scanner 는 디코딩 오류를 입력 끝으로 처리하므로
    // 파일이 UTF-8 이 아니면 0개가 읽힌다.
    @Test
    void topicFileHas177DistinctUtf8Topics() throws IOException {
        List<String> topics = new ArrayList<>();
        try (Scanner sc = new Scanner(new File("주제.txt"), StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                topics.add(sc.nextLine());
            }
        }
        assertEquals(177, topics.size());
        assertEquals(177, new HashSet<>(topics).size());
    }
}
