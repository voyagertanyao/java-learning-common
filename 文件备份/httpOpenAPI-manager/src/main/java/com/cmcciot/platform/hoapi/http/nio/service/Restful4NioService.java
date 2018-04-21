/*
 * 文 件 名:  Restful4NioService.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年12月11日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.http.nio.service;

import com.cmcciot.platform.hoapi.http.nio.bean.LockUserInfo;

/**
 * 业务处理
 * <功能详细描述>
 *
 * @author xuxiaochuan
 * @version [版本号, 2014年12月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface Restful4NioService {
    /**
     * 创建查询map
     * <功能详细描述>
     *
     * @param userName 用户名
     * @return Map<String,Object> [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public LockUserInfo buildLockMap(String userName);

}
