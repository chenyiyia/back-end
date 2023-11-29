package com.sky.utils;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@Slf4j
public class QiniuyunOssUtil {
    private String accessKey;
    private String secretKey;
    private String bucket;

    public String upload(byte[] bytes,String objectName){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Zone.zone2());//...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);//...生成上传凭证，然后准备上传
        String key = objectName;//默认不指定key的情况下，以文件内容的hash值作为文件名

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        DefaultPutRet putRet=null;
        try {
            Response response = uploadManager.put(bytes, key, upToken);
            //解析上传成功的结果
            putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            //System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }

        //文件访问路径规则 "http://s4tc2wvog.hn-bkt.clouddn.com/"+putRet.key
        StringBuilder stringBuilder = new StringBuilder("http://s4tc2wvog.hn-bkt.clouddn.com//");
        stringBuilder.append(putRet.key);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
