import org.json.JSONObject;

public class TestJSON {
    public static void main(String[] args) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("test", "success");
            System.out.println(obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
