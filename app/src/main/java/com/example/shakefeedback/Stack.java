package com.example.shakefeedback;

import java.util.Vector;

public class Stack<T extends Object> {
    Vector<T> vector = new Vector();

    public T pop() {
        T t = null;
        if (!isBottom()) {
            t = vector.lastElement();
            int pos = vector.lastIndexOf(t);
//            vector.remove(t);
            vector.removeElementAt(pos);
        }
        return t;
    }

    public void push(T t) {
        vector.addElement(t);
    }

    public boolean isBottom() {
        if (vector.isEmpty()) {
            return true;
        }
        return false;
    }

    public int size() {
        return vector.size();
    }
}