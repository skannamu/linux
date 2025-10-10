package com.skannamu.server.command;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalcCommand implements ICommand {

    @Override
    public String getName() {
        return "calc";
    }
    @Override
    public String getUsage() {
        return "Usage: calc <expression>\n" +
                "Performs a basic mathematical calculation (addition, subtraction, multiplication, division).\n" +
                "Example: calc 5*6*7-1";
    }
    @Override
    public String execute(ServerPlayerEntity player, List<String> optionsList, String remainingArgument) {
        if (remainingArgument.isBlank()) {
            return "Error: Usage: calc <expression>. Type 'calc -h' for help.";
        }
        String expression = remainingArgument.replaceAll("\\s+", "");
        Pattern numberPattern = Pattern.compile("[-+]?\\d+(\\.\\d+)?");
        Matcher numberMatcher = numberPattern.matcher(expression);
        Pattern operatorPattern = Pattern.compile("[+\\-*/]");
        Matcher operatorMatcher = operatorPattern.matcher(expression);

        if (!numberMatcher.find()) {
            return "Error: No numbers found in expression.";
        }
        try {
            double result = Double.parseDouble(numberMatcher.group());
            while (operatorMatcher.find() && numberMatcher.find(operatorMatcher.end())) {
                String operator = operatorMatcher.group();
                double nextNum = Double.parseDouble(numberMatcher.group());

                switch (operator) {
                    case "+": result += nextNum; break;
                    case "-": result -= nextNum; break;
                    case "*": result *= nextNum; break;
                    case "/":
                        if (nextNum == 0) return "Error: Division by zero.";
                        result /= nextNum;
                        break;
                    default: return "Error: Unsupported operator found: " + operator;
                }
            }
            return "Result: " + String.format("%.2f", result).replaceAll("\\.00$", "");
        } catch (Exception e) {
            return "Error: Failed to parse expression or invalid format. Check input.";
        }
    }
}