package com.hhy.server.command;

import com.hhy.common.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandLineUtil {

    public static List<CommandLine.Option> parse(String[] args) {
        if (args == null || args.length == 0) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.stream(args)
                .filter(arg -> arg.startsWith(StringUtil.ARG_PREFIX))
                .map(arg -> arg.substring(StringUtil.ARG_PREFIX.length()))
                .map(arg -> {
                    boolean assignSymbol = true;
                    int bound = arg.indexOf('=');
                    if (bound == -1) {
                        assignSymbol = false;
                        bound = arg.length();
                    }
                    return new CommandLine.Option(arg.substring(0, bound),
                            arg.substring(assignSymbol ? bound + 1 : bound),
                            assignSymbol);
                }).collect(Collectors.toList());
    }
}
