package com.meritoki.module.library.model;

import javax.swing.tree.DefaultMutableTreeNode;

public class N extends DefaultMutableTreeNode {
    public N(String s) { super(s, true); }
    public N add(String... strs) {
        for (String s : strs) {
            super.add(new N(s));
        }
        return this;
    }
    public N add(N... ns) {
        for (N n : ns) {
            super.add(n);
        }
        return this;
    }
}