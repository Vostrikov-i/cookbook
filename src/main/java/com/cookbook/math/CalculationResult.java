package com.cookbook.math;


/*
* This class presents object for store calculation result
* success is true if there's no error in calculation
*
*
* */

public class CalculationResult {

    private final boolean success;
    private final double result;

    public CalculationResult(boolean success, double result) {
        this.result = result;
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "CalculationResult{" +
                "success=" + success +
                ", result=" + result +
                '}';
    }
}
