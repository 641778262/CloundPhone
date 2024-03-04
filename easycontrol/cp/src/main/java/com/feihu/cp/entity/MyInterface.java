package com.feihu.cp.entity;

public final class MyInterface {
  public interface MyFunction {
    void run();
  }

  public interface MyFunctionBoolean {
    void run(Boolean bool);
  }

  public interface MyFunctionString {
    void run(String str);
  }

  public interface MyFunctionBytes {
    void run(byte[] buffer);
  }
}
