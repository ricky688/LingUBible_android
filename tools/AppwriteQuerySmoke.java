import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

/** Runs against the optimized APK via app_process, without opening or changing app data.
 * Pass Query's output class name from the variant's R8 mapping as the sole argument. */
public final class AppwriteQuerySmoke {
    public static void main(String[] args) throws Exception {
        Constructor<?> constructor = Class.forName(args[0]).getDeclaredConstructor(
                String.class, String.class, List.class);
        constructor.setAccessible(true);
        JSONObject equal = query(constructor, "equal", "course_code", Arrays.asList("BUS1102"));
        require("equal".equals(equal.getString("method")), "equal method");
        require("course_code".equals(equal.getString("attribute")), "equal attribute");
        require("BUS1102".equals(equal.getJSONArray("values").getString(0)), "equal values");
        JSONObject order = query(constructor, "orderAsc", "course_code", null);
        require("orderAsc".equals(order.getString("method")), "order method");
        require("course_code".equals(order.getString("attribute")), "order attribute");
        JSONObject limit = query(constructor, "limit", null, Arrays.asList(100));
        require("limit".equals(limit.getString("method")), "limit method");
        require(limit.getJSONArray("values").getInt(0) == 100, "limit values");
        System.out.println("PASS: optimized Appwrite equal, orderAsc and limit JSON wire fields");
    }

    private static JSONObject query(Constructor<?> constructor, String method,
                                    String attribute, List<?> values) throws Exception {
        return new JSONObject(constructor.newInstance(method, attribute, values).toString());
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
