package io.kurumi.ntt.fragment.mods;

import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.util.List;
import java.util.Map;

public class NModule {

    public String id;
    public String name;
    public String version;
    public Integer versionCode;
    public String author;
    public List<String> dependencies;
    public List<String> libs;
    public String main;
    public Map<String, String> cmds;
    public List<String> actions;

    public transient File modPath;
    public transient Scriptable env;
    public transient ModuleException error;

    public String format() {

        return "[ " + name + " - " + version + " ]";

    }

    public String info() {

        String info = "作者 : " + author;

        if (!cmds.isEmpty()) {

            info += "\n\n所有命令 :\n";

            for (Map.Entry<String, String> cmd : cmds.entrySet()) {

                info += "\n/" + cmd.getKey() + " : " + cmd.getValue();

            }

        }

        return info;

    }

}
