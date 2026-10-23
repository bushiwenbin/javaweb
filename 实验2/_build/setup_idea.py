# -*- coding: utf-8 -*-
"""为 实验2/ctts 生成 IntelliJ IDEA 工程配置，并预注册 JDK 17，做到「打开即可运行」。"""
import os
import subprocess

IDEA_HOME = r"C:\dev\idea"
JDK_HOME = r"C:\dev\jdk-17"
PROJ = r"C:\Users\asus\OneDrive\Desktop\javaweb\实验2\ctts"
IDEA_CFG = os.path.join(os.environ["APPDATA"], "JetBrains", "IntelliJIdea2025.3")

IDEA_DIR = os.path.join(PROJ, ".idea")
os.makedirs(IDEA_DIR, exist_ok=True)
os.makedirs(os.path.join(IDEA_DIR, "runConfigurations"), exist_ok=True)
os.makedirs(os.path.join(IDEA_CFG, "options"), exist_ok=True)

JDK_URL = "C:/dev/jdk-17"


def w(path, text):
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)
    print("written:", path)


# ---------------------------------------------------------------- 工程配置
w(os.path.join(IDEA_DIR, "misc.xml"), """<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ExternalStorageConfigurationManager" enabled="true" />
  <component name="MavenProjectsManager">
    <option name="originalFiles">
      <list>
        <option value="$PROJECT_DIR$/pom.xml" />
      </list>
    </option>
  </component>
  <component name="ProjectRootManager" version="2" languageLevel="JDK_17" default="true"
             project-jdk-name="17" project-jdk-type="JavaSDK">
    <output url="file://$PROJECT_DIR$/out" />
  </component>
</project>
""")

w(os.path.join(IDEA_DIR, "compiler.xml"), """<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="CompilerConfiguration">
    <bytecodeTargetLevel target="17" />
  </component>
</project>
""")

w(os.path.join(IDEA_DIR, "encodings.xml"), """<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="Encoding" defaultCharsetForPropertiesFiles="UTF-8">
    <file url="PROJECT" charset="UTF-8" />
  </component>
</project>
""")

w(os.path.join(IDEA_DIR, "jarRepositories.xml"), """<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="RemoteRepositoriesConfiguration">
    <remote-repository>
      <option name="id" value="aliyun-public" />
      <option name="name" value="aliyun-public" />
      <option name="url" value="https://maven.aliyun.com/repository/public" />
    </remote-repository>
    <remote-repository>
      <option name="id" value="central" />
      <option name="name" value="Maven Central repository" />
      <option name="url" value="https://repo1.maven.org/maven2" />
    </remote-repository>
    <remote-repository>
      <option name="id" value="jboss.community" />
      <option name="name" value="JBoss Community repository" />
      <option name="url" value="https://repository.jboss.org/nexus/content/repositories/public/" />
    </remote-repository>
  </component>
</project>
""")

w(os.path.join(IDEA_DIR, ".gitignore"), """workspace.xml
shelf/
httpRequests/
dataSources/
dataSources.local.xml
""")

w(os.path.join(IDEA_DIR, "runConfigurations", "CttsApplication.xml"),
  """<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="CttsApplication（端口 8080）" type="Application"
                 factoryName="Application">
    <option name="MAIN_CLASS_NAME" value="com.gzu.ctts.CttsApplication" />
    <module name="ctts" />
    <option name="VM_PARAMETERS"
            value="-Dfile.encoding=UTF-8" />
    <option name="PROGRAM_PARAMETERS" value="--server.port=8080" />
    <option name="WORKING_DIRECTORY" value="$PROJECT_DIR$" />
    <method v="2">
      <option name="Make" enabled="true" />
    </method>
  </configuration>
</component>
""")

# ---------------------------------------------------------------- 预注册 JDK 17
mods = subprocess.run([os.path.join(JDK_HOME, "bin", "java.exe"), "--list-modules"],
                      capture_output=True, text=True).stdout
modules = [m.split("@")[0].strip() for m in mods.splitlines() if m.strip()]

class_roots = "\n".join(
    '        <root type="simple" url="jrt://%s!/%s" />' % (JDK_URL, m) for m in modules)
src_roots = "\n".join(
    '        <root type="simple" url="jar://%s/lib/src.zip!/%s" />' % (JDK_URL, m)
    for m in modules)

jdk_table = """<application>
  <component name="ProjectJdkTable">
    <jdk version="2">
      <name value="17" />
      <type value="JavaSDK" />
      <version value="java version &quot;17.0.20.1&quot;" />
      <homePath value="C:\\dev\\jdk-17" />
      <roots>
        <annotationsPath>
          <root type="composite">
            <root type="simple" url="jar://$APPLICATION_HOME_DIR$/plugins/java/lib/resources/jdkAnnotations.jar!/" />
          </root>
        </annotationsPath>
        <classPath>
          <root type="composite">
%s
          </root>
        </classPath>
        <javadocPath>
          <root type="composite" />
        </javadocPath>
        <sourcePath>
          <root type="composite">
%s
          </root>
        </sourcePath>
      </roots>
      <additional />
    </jdk>
  </component>
</application>
""" % (class_roots, src_roots)

w(os.path.join(IDEA_CFG, "options", "jdk.table.xml"), jdk_table)
print("modules:", len(modules))
