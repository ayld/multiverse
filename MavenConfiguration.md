# Using Multiverse in your Maven project #

## Adding Multiverse to an existing project ##

In order to use Multiverse in your Maven project, add the following dependency:

```
<dependency>
  <groupId>org.multiverse</groupId>
  <artifactId>multiverse-core</artifactId>        
  <version>0.3-SNAPSHOT</version>
</dependency>
```

You will need to add the project's Maven repositories to your POM:

```
<repository>
  <id>multiverse-releases</id>
  <url>http://multiverse.googlecode.com/svn/maven-repository/releases</url>
</repository>
<snapshotRepository>
  <id>multiverse-snapshots</id>
  <url>http://multiverse.googlecode.com/svn/maven-repository/snapshots</url>
</snapshotRepository>
```

## Creating a new Multiverse project ##

The quickest way to make the Multiverse archetypes available locally is to [check out Multiverse](http://code.google.com/p/multiverse/source/checkout) and run

`mvn install`

from the `multiverse-project-archetype` directory. This will build the archetype and place it in your local catalogue.

In order to create a project based _on_ the archetype, navigate to the directory in which you wish the project to be created and run

`mvn archetype:generate`

Parameters:
| Name | Description | Default | Example |
|:-----|:------------|:--------|:--------|
| **artifactId** | The Maven project artifactId. |         | stm-project |
| **groupId** | The Maven project groupId. |         | org.multiverse |
| **package** | The Java base package of the project's classes. | _same as_ `groupId` |         |
| **version** | The Maven project version. | 1.0-SNAPSHOT |         |

## Downloading the JARs manually ##

To download the jar manually, check the <a href='http://code.google.com/p/multiverse/wiki/Download'>download page</a>.