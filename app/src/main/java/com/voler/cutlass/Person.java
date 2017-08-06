package com.voler.cutlass;

import java.io.Serializable;

/**
 * 三尺春光驱我寒，一生戎马为长安
 * Created by Han on 17/7/10.
 */

public class Person implements Serializable {
    String name;
    boolean haveObject;

    public Person(String name, boolean haveObject) {
        this.name = name;
        this.haveObject = haveObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHaveObject() {
        return haveObject;
    }

    public void setHaveObject(boolean haveObject) {
        this.haveObject = haveObject;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", haveObject=" + haveObject +
                '}';
    }
}
