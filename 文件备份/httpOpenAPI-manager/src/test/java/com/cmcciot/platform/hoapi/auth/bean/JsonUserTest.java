package com.cmcciot.platform.hoapi.auth.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

/**
 * Created by Ginda.Tseng on 2016/8/17.
 */
public class JsonUserTest {

    @Test
    public void testJson() throws Exception {
        String json = "{\"errorCode\":0,\"description\":\"成功\",\"msgType\":null,\"version\":null,\"msgSeq\":null,\"isRegistered\":null,\"userID\":\"6167\",\"password\":\"2afe4a0240dc892962bd07e00abc68e7\",\"routerList\":null,\"isBounded\":null,\"devStatus\":null,\"userStatus\":\"0\",\"cameraList\":null}";
        ObjectMapper mapper = new ObjectMapper();
        JsonUser ju = (JsonUser) mapper.readValue(json, JsonUser.class);
    }
}