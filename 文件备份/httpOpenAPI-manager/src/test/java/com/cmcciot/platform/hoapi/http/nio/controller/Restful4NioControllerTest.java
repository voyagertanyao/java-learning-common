package com.cmcciot.platform.hoapi.http.nio.controller;

import com.cmcciot.platform.hoapi.auth.filter.DigestFilterUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Ginda.Tseng on 2016/2/14.
 */
public class Restful4NioControllerTest {

    @Test
    public void testAuthServiceManagerNio() throws Exception {
        String url = "http://localhost:18080/auth/nio/serviceManager";
//        String url = "http://172.19.1.20:7080/httpOpenAPI-manager/auth/nio/serviceManager";
        String message = "test";
        System.out.println(DigestFilterUtil.sendMessage(url, message));
    }

    @Test
    public void testNoAuthServiceManagerNio() throws Exception {

    }

    @Test
    public void testResponseResult() throws Exception {

    }

    @Test
    public void testClearCacheResult() throws Exception {

    }

    @Test
    public void testQueryLockUser() throws Exception {

    }

    @Test
    public void testUnLockUser() throws Exception {

    }
}