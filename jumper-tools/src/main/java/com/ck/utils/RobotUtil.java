package com.ck.utils;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

/**
 * 机器人操作工具类
 *
 * @author cyk
 * @since 2021-06-06
 */
public final class RobotUtil {
    private static final Logger log = Logger.getLogger(RobotUtil.class.getName());


    /**
     * 获取操作机器人对象
     *
     * @return
     */
    public static Robot getRobot() {
        Robot robot = null;
        try {
            robot = new Robot();
            // 执行完一个事件后再执行下一个
            robot.setAutoWaitForIdle(true);
        } catch (AWTException e) {
            log.warning(String.format("Robot Create Error:%s 创建失败", e.getMessage()));
        }
        return robot;
    }

    /**
     * 移动鼠标位置
     *
     * @param x 移动到的横坐标
     * @param y 移动到的纵坐标
     */
    public static void MouseMove(int x, int y) {
        // 执行鼠标移动
        if (x > 0 && y > 0) {
            getRobot().mouseMove(x, y);
        }
    }

    /**
     * 返回鼠标的真正事件<br>
     * 鼠标事件不能直接处理，需要进过转换
     *
     * @return
     */
    public static int getMouseKey(int button) {
        if (button == MouseEvent.BUTTON1) {
            // 鼠标左键
            return InputEvent.BUTTON1_MASK;
        } else if (button == MouseEvent.BUTTON2) {
            // 鼠标右键
            return InputEvent.BUTTON2_MASK;
        } else if (button == MouseEvent.BUTTON3) {
            // 滚轮
            return InputEvent.BUTTON3_MASK;
        } else {
            return 0;
        }
    }

    /**
     * 鼠标事件处理<br>
     * 用来判断事件类型，并用robot类执行
     *
     * @param mouse
     */
    public static void MouseEvent(MouseEvent mouse) {
        // 拿到事件类型
        int type = mouse.getID();
        if (type == Event.MOUSE_DOWN) {
            // 鼠标按下
            getRobot().mousePress(getMouseKey(mouse.getButton()));
        } else if (type == Event.MOUSE_UP) {
            // 鼠标抬起
            getRobot().mouseRelease(getMouseKey(mouse.getButton()));
        } else if (type == Event.MOUSE_DRAG) {
            // 鼠标拖动
            getRobot().mouseMove(mouse.getX(), mouse.getY());
        }
    }


    /**
     * 键盘按下
     *
     * @param keyCode 键盘点击事件code码
     */
    public static void keyPress(int keyCode) {
        getRobot().keyPress(keyCode);
    }

    /**
     * 键盘抬起
     *
     * @param keyCode 键盘点击事件code码
     */
    public static void keyRelease(int keyCode) {
        getRobot().keyRelease(keyCode);
    }

}
