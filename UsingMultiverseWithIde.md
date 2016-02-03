

# Introduction #

Multiverse it relies on instrumentation (bytecode manipulation) and that is why a Java agent needs to be used. This page describes how to set up IntelliJ, Eclipse and Netbeans for Multiverse and also how to set it up from the commandline and ANT.

## Download ##
The newest Multiverse jar containing the JavaAgent can be downloaded from the following <a href='http://code.google.com/p/multiverse/wiki/Download'>page</a>.

## Commandline ##

Use the following snippet to run an application from the commandline:

```
java -javaagent:/somerootdir/multiverse-alpha-0.3-SNAPSHOT-jar-with-dependencies.jar your.Main
```

For more information about the parameters, check the [the Java application launcher](http://java.sun.com/javase/6/docs/technotes/tools/windows/java.html) page.

## ANT ##

Use the following snippet to run an application using Multiverse and ANT:
```
<target name="run">
   <java classname="your.Main" fork="true">
       <jvmarg value="-javaagent:/to/multiverse/multiverse-alpha-0.3-SNAPSHOT-jar-with-dependencies.jar"/>

      <classpath>
            ... your classpath
      </classpath>
   </java>
</target>
```

## IntelliJ IDEA ##

todo

## Eclipse ##

todo

## Netbeans ##

todo