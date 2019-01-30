package io.kurumi.ntt.cli;

import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.ext.*;
import java.io.*;
import java.util.*;
import org.apache.commons.cli.*;

public abstract class CliUI {

    protected abstract  String cmdLineSyntax();

    protected abstract void applyCommands(HashMap<String,Options> commands);
    protected abstract void onCommand(UserData userData, DataObject obj, String commandName, CommandLine cmd);

    private HashMap<String,Options> commands = new HashMap<>(); { applyCommands(commands); };

    private CommandLineParser parser = new DefaultParser();

    public Options getOptions(String commandName) {

        return commands.get(commandName);

    }

    public String printHelp() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HelpFormatter formatter = new HelpFormatter();

        PrintWriter printer = new PrintWriter(out);

        printer.print(cmdLineSyntax() + " usage :");

        for (Map.Entry<String,Options> cmd : commands.entrySet()) {

            printer.println();

            formatter.printHelp(printer, cmd.getKey(), cmd.getValue());

        }

        return out.toString();

    }

    public String printHelp(String commandName) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new HelpFormatter().printHelp(new PrintWriter(out), commandName, getOptions(commandName));

        return out.toString();

    }

    public void process(UserData userData, DataObject obj) {

        String commandName = MsgExt.getCommandName(obj.msg());

        String[] args = MsgExt.getCommandParms(obj.msg());
        
        if (args == null) args = new String[0];

        try {

            CommandLine cmd = parser.parse(getOptions(commandName), args , true);

            onCommand(userData, obj, commandName, cmd);

        } catch (ParseException e) {

            obj.send(printHelp(commandName));

        }

    }

}
