import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class RandomLoadBalancerTester {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8083/consumer/hello";
        int testCount = 100;
        HashMap<String, Integer> responseStats = new HashMap<>();

        HttpClient client = HttpClient.newHttpClient();

        for (int i = 0; i < testCount; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseText = response.body().trim();

                responseStats.put(responseText, responseStats.getOrDefault(responseText, 0) + 1);
                System.out.println("[" + (i + 1) + "] " + responseText);

                Thread.sleep(100); // 控制调用间隔
            } catch (Exception e) {
                System.out.println("[" + (i + 1) + "] 请求失败：" + e.getMessage());
            }
        }

        System.out.println("\n=== 统计结果 ===");
        for (String instance : responseStats.keySet()) {
            System.out.println(instance + ": " + responseStats.get(instance) + " 次");
        }
    }
}
