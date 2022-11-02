package com.cookbook.math;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// package private class
class Integral {

    private final MathFunction func;
    private final double min;
    private final double max;
    private final long maxIterationCount;
    private double len;
    private volatile CalculationResult calculationResult;
    private volatile boolean isCalculated;


    private Integral(MathFunction func, double min, double max, long maxIterationCount) {
        this.func = func;
        this.min = min;
        this.max = max;
        this.len = max - min;
        this.maxIterationCount = maxIterationCount;

    }

    /**
     * Static factory
     * @param func - function for integral calculating
     * @param min - minimum integration limit
     * @param max - maximum integration limit
     * @param maxIterationCount - max iteration count. For preventing endless cycle
     * @return new Integral object
     * @throws IllegalArgumentException
     */
    public static Integral newInstance(MathFunction func, double min, double max, long maxIterationCount) throws IllegalArgumentException{
        if (min>=max) {
            throw new IllegalArgumentException("Bad values for min-max parameters");
        }
        if(maxIterationCount <=0) {
            throw new IllegalArgumentException("Bad value for max iteration count");
        }
        return new Integral(func, min, max, maxIterationCount);
    }

    /**
     * Reset isCalculated flag and return calculated result
     * It's use internal synchronize strategy
     * @return CalculationResult
     */
    public CalculationResult getCalculationResult() {
        this.isCalculated = false;
        return calculationResult;
    }

    /**
     * If you need to get an actual calculating result you should wait while this flag is becomes true
     * If you call
     * @return true - if integral calculating is end
     */
    public boolean isCalculated() {
        return isCalculated;
    }

    private void setCalculationResult(boolean success, double result) {
        this.calculationResult = new CalculationResult(success, result);
        this.isCalculated = true;
    }

    /**
     * Calculate integral use The Trapezoidal Rule
     * After calculating store new CalculationResult object at this.calculationResult and set this.isCalculated
     * If you call getCalculationResult() this.isCalculated will be reset
     * If you don't call getCalculationResult() you won't get new calculation result
     * @param startNumPoint count of numbers to start calculate integral
     * @param accuracy calculation accuracy. Be careful, iteration count can't be more that this.maxIterationCount
     * @param taskNum - count of CompletableFuture object for calculating. It's depends on the available CPU count for your system
     *
     * It's use internal synchronize strategy
     */
    public void calculate(int startNumPoint, double accuracy, int taskNum){

        if(taskNum <=0) {
            throw new IllegalArgumentException("Task num must be positive");
        }

        if (!this.isCalculated) {
            int numPointToThread = startNumPoint / taskNum;
            List<CompletableFuture<CalculationResult>> calcFutureList = new ArrayList<>();
            int num_futures = taskNum;
            double min = this.min;
            double max = this.min + (len / num_futures);
            int accuracy_coeff = (int) Math.log10(taskNum);
            double threadAccuracy = taskNum > 1 ? accuracy * 0.1 * (Math.pow(0.1, (double) accuracy_coeff)) : accuracy;
            for (int i = 0; i < num_futures - 1; i++) {

                calcFutureList.add(calcIntegralFuture(min, max, this.func, numPointToThread, threadAccuracy));
                min = max;
                max += (len / num_futures);
            }

            calcFutureList.add(calcIntegralFuture(min, this.max, this.func, startNumPoint - (num_futures - 1) * numPointToThread, threadAccuracy));

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    calcFutureList.toArray(new CompletableFuture[0])
            );

            CompletableFuture<List<CalculationResult>> allResults = allFutures.thenApply(v -> {
                return calcFutureList.stream().map(calcFuture -> calcFuture.join()).collect(Collectors.toList());
            });
            try {
                List<CalculationResult> resultList = allResults.get();
                double integral_value = 0.0;
                boolean success = true;
                for (CalculationResult result : resultList) {
                    if (result.isSuccess()) {
                        integral_value += result.getResult();
                    } else {
                        success = false;
                        break;
                    }
                }
                success = !Double.isNaN(integral_value);
                this.setCalculationResult(success, integral_value);

            } catch (InterruptedException | ExecutionException | IllegalArgumentException ex) {
                this.setCalculationResult(false, 0.0);
            }
        }
    }

    private CompletableFuture<CalculationResult> calcIntegralFuture(double min, double max, MathFunction func, int segNum, double accuracy) {
        return CompletableFuture.supplyAsync(() -> calculateIntegral(min, max, func, segNum, accuracy));
    }

    private CalculationResult calculateIntegral(double min, double max, MathFunction func, int segNum, double accuracy){

        int segments = segNum;
        double dx = (max-min)/segNum*1.0;
        double old_result = 0.0;
        double result = 0.5*(func.getF(min) + func.getF(max));
        boolean success = true;
        long iteration_count = 0;
        try {

            for (int i = 1; i < segNum; i++) {
                result += func.getF(min + i * dx);
            }
            result = result * dx;
            double err = Math.max(1, Math.abs(result));

            while (err > Math.abs(accuracy * result)) {
                old_result = result;
                result = 0.5 * (result + calculateRectangle(min, max, func, segments, 0.5));
                segments = segments * 2;
                err = Math.abs(result - old_result);
                ++iteration_count;
                if (iteration_count > this.maxIterationCount) {
                    throw new ArithmeticException("Maximum iteration count");
                }
            }
        } catch (Exception ex) {
            success = false;
            result = 0.0;
        }
        success = !Double.isNaN(result);
        System.out.println(min + " , " + max + " " + result);
        return new CalculationResult(success, result);
    }

    private double calculateRectangle(double min, double max, MathFunction func, int segNum, double rectPointPosition) {
        double dx = (max-min)/segNum*1.0;
        double result = 0.0;
        boolean success = true;
        double start_point = min + rectPointPosition * dx;
        for(int i =0; i<segNum; i++) {
            result+=func.getF(start_point + i*dx);
        }
        result = result*dx;
        return result;
    }
}
