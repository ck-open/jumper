package com.ck.core.event;

import com.ck.core.utils.SpringContextUtil;
import org.springframework.context.ApplicationEvent;

/**
 * @ClassName EventHandler
 * @Description 事件处理器
 * @Author Cyk
 * @Version 1.0
 * @since 2022/4/24 10:46
 **/
public class EventHandler {

    public static  <T extends ApplicationEvent> void publish(T event){
        SpringContextUtil.getApplicationContext().publishEvent(event);
    }


    /**
     * Sql 编译BaseMapper 失败事件通知
     */
    public static class SqlCompileError extends ApplicationEvent {
        public SqlCompileError(Object source) {
            super(source);
        }
    }
}
