//
// JiST (Java In Simulation Time) kernel project.
//
// Copyright (C) 2008 by Tronje Krop (University of Technology Darmstadt)
// All rights reserved. Refer to LICENSE for terms and conditions of use.
//
package java.lang;

/**
 *
 */
public class TestLoader extends ClassLoader {
    private final ClassLoader parent;

    public TestLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;
    }

    public Class defineClass(String name, byte[] b) {
        return this.parent.defineClass(name, b, 0, b.length);
    }
}