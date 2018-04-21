package com.cmcciot.platform.hoapi.http.nio.threadPool;


/**
 * 用于临时存储服务管理主动调用httpOpenAPI时传递过来的操作结果
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年4月18日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class TemporaryObject {
    /**
     * 服务管理北向接口传递过来的值
     */
    private String responseVal;

    /**
     * 等待时间
     */
    private Long waitTime;

    public String getResponseVal() {
        return responseVal;
    }

    public void setResponseVal(String responseVal) {
        this.responseVal = responseVal;
    }

    public Long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Long waitTime) {
        this.waitTime = waitTime;
    }
}
