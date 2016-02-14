Make sure you have a gpg key installed and uploaded to a keyserver
such as https://sks-keyservers.net/i/

Make sure your sonatype credentials are in your maven settings.xml:

```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
   <servers>
     <server>
       <id>sonatype-nexus-staging</id>
       <username>raboof</username>
       <password>*********</password>
     </server>
   </servers>

   <pluginGroups></pluginGroups>
   <proxies></proxies>
   <mirrors></mirrors>
   <profiles></profiles>
</settings>
```

Then:

 * mvn release:prepare
 * mvn release:perform
 * git push --tags
 * finalize the release as described at http://central.sonatype.org/pages/releasing-the-deployment.html
