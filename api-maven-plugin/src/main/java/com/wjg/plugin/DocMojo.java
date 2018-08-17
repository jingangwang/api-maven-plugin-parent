package com.wjg.plugin;

import com.wjg.util.HmlParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动生成API插件实现
 * @author wjg
 * @date 2018/6/28 10:07
 */
@Mojo(name="doc" , defaultPhase = LifecyclePhase.PACKAGE)
public class DocMojo extends AbstractMojo{
    /**
     * 文档生成的存放目录
     */
    @Parameter(property = "targetFile")
    private String targetFile;
    /**
     * 扫描的包路径
     */
    @Parameter(property = "basePackage")
    private String basePackage;
    /**
     * 类路径
     */
    @Parameter(property = "classPath")
    private String classPath;
    /**
     * 依赖包路径
     */
    @Parameter(property = "libPath")
    private String libPath;
    /**
     * url的类加载器
     */
    private URLClassLoader urlClassLoader;

    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("--- 参数列表");
        this.getLog().info("classPath=" + classPath);
        this.getLog().info("libDir=" + libPath);
        this.getLog().info("basePackage=" + basePackage);
        this.getLog().info("targetFile=" + targetFile);
        //1、从类路径下扫描出所有的.class
        try {
            String cPath = new URL("file",null,new File(classPath).getCanonicalPath()+File.separator).toString();
            String lPath = new URL("file",null,new File(libPath).getCanonicalPath()+File.separator).toString();
            List<URL> libs = new ArrayList<URL>();
            File libDir = new File(libPath.replaceAll("file:",""));
            if(!libDir.exists()){
                this.getLog().warn("libPath not exists");
            }
            for (File jar : libDir.listFiles()){
                libs.add(new URL(lPath+jar.getName()));
            }
            libs.add(new URL(cPath));

            urlClassLoader = new URLClassLoader(libs.toArray(new URL[libs.size()]), Thread.currentThread().getContextClassLoader());

            List<Class<?>> classes = new ArrayList<Class<?>>();
            File classDir = new File(classPath);
            listClasses(classes,classDir);

            if(classes.size() == 0){
                this.getLog().info("has no classes load");
                return;
            }
            HmlParser.generate(targetFile, classes, "");
            this.getLog().info("generate api doc success!");
        } catch (IOException e) {
            e.printStackTrace();
            this.getLog().error(e.toString());
        }
    }

    private void listClasses(List<Class<?>> clazz,File classDir){
        File[] files = classDir.listFiles();
        for(File file: files){
            if(file.isDirectory()){
                listClasses(clazz,file);
            }else{
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                String className = file.getPath().replaceAll("\\\\","/")
                        .replaceAll(classPath.replaceAll("\\\\","/"),"")
                        .replaceAll("/",".")
                        .replaceAll("\\.class","")
                        .substring(1);
                if(className.startsWith(basePackage)){
                    try {
                        clazz.add(Class.forName(className,true,urlClassLoader));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
