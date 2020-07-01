package com.hapi.hapiplugin;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;

public class ReClassPool extends ClassPool {


    public static synchronized ReClassPool getDefault() {
        if (defaultPool == null) {
            defaultPool = new ReClassPool(false);
            defaultPool.appendSystemPath();
        }

        return defaultPool;
    }

    private static ReClassPool defaultPool = null;

    public ReClassPool(boolean b){
        super(b);
    }

    public CtClass removeCached2(String classname) {
        classes.clear();
        return (CtClass)classes.remove(classname);
    }


    @Override
    public CtClass getCtClass(String classname) throws NotFoundException {
        classes.clear();

        CtClass c=  createCtClass(classname,false);///super.getCtClass(classname);

        System.out.println(classname+"   "+c.isModified()+"c.createCtClass().createCtClass();" + c.getURL().toString());

        try {
            CtField f = new CtField(CtClass.intType, "hiddenValue", c);
            f.setModifiers(Modifier.PUBLIC);
            c.addField(f);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    protected void cacheCtClass(String classname, CtClass c, boolean dynamic) {

    }

    @Override
    protected synchronized CtClass get0(String classname, boolean useCache) throws NotFoundException {
        useCache = false;
        System.out.println("get0get0get0"+classname+" parent  "+parent==null);
        return super.get0(classname, false);
    }

    public CtClass getnew(String classname){
        try {
            return get0(classname,false);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }
    @Override
    protected CtClass removeCached(String classname) {
        return super.removeCached(classname);
    }
}
