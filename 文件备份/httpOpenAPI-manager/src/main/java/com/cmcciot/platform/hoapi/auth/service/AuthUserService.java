/*
 * 文 件 名:  AuthUserService.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年4月10日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmcciot.platform.hoapi.auth.service;

import com.cmcciot.platform.hoapi.auth.bean.User;

/**
 * 查询用户信息
 * <功能详细描述>
 *
 * @author Administrator
 * @version [版本号, 2014年4月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface AuthUserService {

    /**
     * 根据用户名查找用户
     * <功能详细描述>
     *
     * @param username
     * @return User [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    User findUserByUsername(String username);

    /**
     * 判断是否锁定的用户
     * <功能详细描述>
     *
     * @param userName 用户名
     * @return boolean 是否锁定
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    boolean isLockedUser(String userName);

    /**
     * 锁定账号
     * <功能详细描述>
     *
     * @param userName [参数说明]
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    void lockUser(String userName);

    /**
     * 删除锁定用户
     * <功能详细描述>
     *
     * @param userName 用户名
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    void removeLockUser(String userName);
}
