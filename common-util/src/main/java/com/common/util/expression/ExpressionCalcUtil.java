package com.common.util.expression;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class ExpressionCalcUtil {
    public static void main(String[] args) {
        Binding binding = new Binding();
        binding.setVariable("实际值", 80);
        binding.setVariable("目标值", 100);
        binding.setVariable("权重", 0.3);
        String expression = """
                完成率 = 目标值 == 0 ? 0 : (目标值 > 0? 实际值 / 目标值 : (实际值 - 目标值) / Math.abs(目标值))
                得分 = 完成率 < 0.6 ? 0 : (目标值 > 0 ? 实际值 / 目标值 * 权重 : (实际值 - 目标值) / Math.abs(目标值) * 权重)
                得分 > 0.5? 0.5 : 得分
                """;
        GroovyShell shell = new GroovyShell(binding);
        Object result = shell.evaluate(expression);
        System.out.println("得分结果: " + result);
    }
}
