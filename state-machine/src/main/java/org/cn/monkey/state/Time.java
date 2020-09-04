package org.cn.monkey.state;

public class Time implements ITime {

  @Override
  public long getCurrentTime() {
    return System.currentTimeMillis();
  }
}
