package com.cmcciot.platform.common.utils;

import java.util.List;


public class ListTools
{
    
    /**
     * 判断集合是否为空
     * 
     * @param list
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(List list)
    {
        if (null == list)
        {
            return true;
        }
        else
        {
            if (null != list && list.size() == 0)
            {
                return true;
            }
            return false;
        }
    }
}
