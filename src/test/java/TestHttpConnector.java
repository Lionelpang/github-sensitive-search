/**
 * @author pangms
 * @date 2020/8/19
 */
import com.ai.gss.executor.GithubClient;
import com.ai.gss.executor.Task;
import okhttp3.Call;
import okhttp3.Response;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;


public class TestHttpConnector {

    @Test
    public void testBuildUrl() throws Exception {
        Task t = new Task().setPage(5)
                .setToken("sfsfs")
                .addExt("html")
                .addExt("css")
                .addExt("python")
                .addExt("properties")
                .addExt("js")
                .addExt("java")
                .addQ("Asiainfo")
                .addQ("java");
        System.out.println(GithubClient.newInstance().buildUrl(t));
    }

    @Test
    public void testCall() throws Exception {
        Task t = new Task().setPage(5)
                .setToken("sdfasdf")
                .addExt("html")
                .addExt("css")
                .addExt("python")
                .addExt("properties")
                .addExt("js")
                .addExt("java")
                .addQ("Asiainfo");
        Call call = GithubClient.newInstance()
                .call(t);
        Response response = call.execute();
        System.out.println(response);
        System.out.println(response.body().byteString());
    }

    @Test
    public void testDate() {
        Calendar calendar = Calendar.getInstance();
        System.out.println(new Date(1597889680));
        // 1597890209386
        // 1597889680
        System.out.println(new Date().getTime());
    }
}
