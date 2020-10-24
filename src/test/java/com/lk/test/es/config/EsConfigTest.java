package com.lk.test.es.config;

import com.alibaba.fastjson.JSON;
import com.lk.test.es.entity.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;


@SpringBootTest()
public class EsConfigTest {


    @Resource
    public RestHighLevelClient client;
    private String myIndex = "java_create_index";


    /**
     * @throws IOException 创建
     */
    @Test
    void testCreateIndex() throws IOException {
        // 1.创建索引请求
        CreateIndexRequest req = new CreateIndexRequest(myIndex);
        // 2.客户端执行创建请求
        CreateIndexResponse resp =
                client.indices()
                      .create(req, RequestOptions.DEFAULT);
    }

    /**
     * @throws IOException 获取索引
     */
    @Test
    void testGetIndex() throws IOException {
        GetIndexRequest req = new GetIndexRequest(myIndex);
        boolean b = client.indices()
                          .exists(req, RequestOptions.DEFAULT);
        System.out.println("b = " + b);
    }

    /**
     * @throws IOException 删除
     */
    @Test
    void testDelIndex() throws IOException {
        DeleteIndexRequest req = new DeleteIndexRequest(myIndex);
        AcknowledgedResponse delete = client.indices()
                                            .delete(req, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }


    @Test
    void testSetIndexForShardsAndReplicas() {
        CreateIndexRequest req = new CreateIndexRequest(myIndex);
        // 设置索引的分片和副本
        req.settings(Settings.builder()
                             .put("index.number_of_shards", 5)
                             .put("index.number_of_replicas", 1));
    }

    /**
     * 创建文档
     */
    @Test
    void testAddDoc() {
        // 1.存储在索中的对象
        User user = new User("李可", 18);
        // 2.选择需要添加内容的索引
        IndexRequest indexReq = new IndexRequest(myIndex);

        // 3.PUT /java_create_index/_doc/1
        indexReq.id("1");
        indexReq.timeout(TimeValue.timeValueSeconds(1));
        // 将数据放入请求中
        indexReq.source(JSON.toJSONString(user),XContentType.JSON);

        // 4.发送请求
        IndexResponse resp = null;
        try {
            resp = client.index(indexReq, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("resp.toString() = " + resp.toString());
            System.out.println("resp.status() = " + resp.status());
        }
    }


    /**
     * 判断文档是否存在
     */
    @Test
    void testIsExists() {
        GetRequest getRequest = new GetRequest(myIndex, "1");
        getRequest.fetchSourceContext();
        getRequest.storedFields("_none_");
        boolean exists = false;
        try {
            exists = client.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("exists = " + exists);
        }
    }

    /**
     * 获取文档
     */
    @Test
    void testGetDoc() {
        GetRequest getRequest = new GetRequest(myIndex, "1");
        GetResponse documentFields = null;
        try {
            documentFields = client.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("documentFields.getSourceAsString() = " + documentFields.getSourceAsString());
        }
    }

    /**
     * 更新文档
     */
    @Test
    void testUpdateDoc() {
        UpdateRequest upReq = new UpdateRequest(myIndex, "1");
        upReq.timeout("1s");

        User user = new User("keke", 20);
        upReq.doc(JSON.toJSONString(user),XContentType.JSON);

        try {
            client.update(upReq,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testDelDoc() {
        DeleteRequest delReq = new DeleteRequest(myIndex, "1");
        delReq.timeout("1s");

        DeleteResponse delete = null;
        try {
            delete = client.delete(delReq, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("delete.status() = " + delete.status());
        }
    }



    //创建索引方法，传入索引名和类型名
    public boolean reCreateIndex(String index, String type) {
        //删除索引请求，如果创建之前存在就需要删除掉
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest(index);
        try {
            //删除索引
            AcknowledgedResponse deleteIndexResponse = client.indices()
                                                             .delete(deleteRequest, RequestOptions.DEFAULT);
            //如果没删除成功，抛出异常
            if (! deleteIndexResponse.isAcknowledged()) {
                throw new RuntimeException("delete index {} error" + index);
            }
            //创建索引请求
            CreateIndexRequest request = new CreateIndexRequest(index);
            //设置分片和副本数
            request.settings(Settings.builder()
                                     .put("index.number_of_shards", 5)
                                     .put("index.number_of_replicas", 1));
            //创建映射，中间填写需要存到ES上的JavaDTO对象对应的JSON数据。
            request.mapping(type, XContentType.JSON);
            //创建索引
            CreateIndexResponse createIndexResponse = client.indices()
                                                            .create(request, RequestOptions.DEFAULT);
            //如果没成功，抛出异常
            if (! createIndexResponse.isAcknowledged()) {
                throw new RuntimeException("create index {} error" + index);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}