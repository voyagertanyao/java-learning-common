/*
 * 文 件 名:  IllegalParamException.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月10日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.exception;

/**
 * 参数不合法异常
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年4月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class IllegalParamException extends RuntimeException {
    private static final long serialVersionUID = 4937339384481861488L;

    /**
     * <默认构造函数>
     */
    public IllegalParamException() {
    }

    public IllegalParamException(String mesg) {
        super(mesg);
    }


    public IllegalParamException(Throwable throwable) {
        super(throwable);
    }


    public IllegalParamException(String mesg, Throwable throwable) {
        super(mesg, throwable);
    }

}
