import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutorStatus;
import com.ai.gss.instance.YamlConfig;
import com.ai.gss.scheduler.GithubSensitiveSearchScheduler;
import com.ai.gss.scheduler.reporter.DefaultTaskReporter;
import com.ai.gss.scheduler.reporter.TaskReportEntity;
import com.ai.gss.util.YamlToObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

/**
 * @author pangms
 * @date 2020/9/23
 */
public class TestScheduler {

    @Test
    public void testTask() {
        Task t = new Task().setPage(1)
                .setToken("sdfffsdfsafd")
                .addExt("html")
                .addExt("css")
                .addExt("python")
                .addExt("properties")
                .addExt("js")
                .addExt("java")
                .addQ("Asiainfo");

        DefaultTaskReporter taskReporter = new DefaultTaskReporter();

        GithubSensitiveSearchScheduler scheduler = new GithubSensitiveSearchScheduler().withExecutorNum(5);
        scheduler.addToken("sdfasdfsafasd")
                .addTask(t)
                .addReport(taskReporter)
                .startWorks();
        while (true) {
            try {
                TaskReportEntity entity = taskReporter.fetchReport();
                if(TaskExecutorStatus.Error.getCode() == entity.getStatus().getCode()){
                    continue;
                }
                // System.out.println("queue entity:" + entity);
                JSONObject jsonObject = JSONObject.parseObject(entity.getResponseBody());
                JSONArray items = (JSONArray) jsonObject.get("items");
                if(items != null) {
                    items.forEach(i-> {
                        System.out.println();
                    });
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    @Test
    public void TestYamlParse() {

        try {
            System.out.println(YamlToObject.parseByClassPath("tasks.yaml", YamlConfig.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
