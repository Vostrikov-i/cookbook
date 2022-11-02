package com.cookbook.math;


/**
 * NotThreadSafe
 * it's wrapper for 2 dimensions function
 *
 * **/
public class FunctionWrapper {

    private final MathFunction func;
    private final double min;
    private final double max;
    private static final double min_accuracy = 1e-15;


    private FunctionWrapper(MathFunction func, double min, double max) {
        this.func = func;
        this.min = min;
        this.max = max;
    }

    //static factory
    public static FunctionWrapper newInstance(MathFunction func, double min_limit, double max_limit) {
        if (min_limit>=max_limit) {
            throw new IllegalArgumentException("Bad values for min-max parameters");
        }

        return new FunctionWrapper(func, min_limit, max_limit);
    }


    public CalculationResult getIntegral(int maxIteration, double accuracy, int taskNum, int startPointNum) {
        Integral calcIntegral = Integral.newInstance(this.func, this.min, this.max, maxIteration);
        calcIntegral.calculate(startPointNum, accuracy, taskNum);
        while (!calcIntegral.isCalculated()) {}
        return calcIntegral.getCalculationResult();
    }

    // get function derivative at point x
    private double dx(double x, double delta) {
        return Math.abs(func.getF(x+delta) - func.getF(x))/delta;
    }

    //get Lipschitz constant
    private double getL(double min, double max, double delta) {
        double L = 0.0;
        double tmp = 0.0;
        int points = 1000;
        double h = (max-min)/points;

        for(double curr_point=min + h; curr_point<=max; curr_point +=h) {
          tmp = dx(curr_point, delta);
          if(tmp > L) L = tmp;
        }
        return L;
    }

    private double getIntersectionPoint(double min, double max, double L) {
        return (func.getF(min) - func.getF(max))/(2*L) + (max+min)/2;
    }


    /*
    * broken line method
    *
    * */
    public CalculationResult getMinimum(double accuracy) {
        if (accuracy < min_accuracy) {
            throw  new ArithmeticException("Bad value for accuracy");
        }
        double delta = accuracy;
        double x_L = min;
        double x_M = max;
        double x_i, x_iL, x_iM, x_iP0, x_iP1 = 0.0;
        double L = getL(x_L, x_M, delta);
        //calc first intersection point
        x_i = getIntersectionPoint(x_L,x_M,L);
        x_iL = getIntersectionPoint(x_L,x_i,L);
        x_iM = getIntersectionPoint(x_i,x_M,L);
        x_iP0 = getIntersectionPoint(x_L, x_M, L);
        double y_0 = func.getF(x_iP0); //calc value;
        double y_1; //y for next step
        double minimun_value = y_0; // save current minimum value
        boolean isInit = true;
        double err = Math.abs(y_0);

        while(isInit || err > accuracy) {
            x_i = getIntersectionPoint(x_L,x_M,L);
            x_iL = getIntersectionPoint(x_L,x_i,L);
            x_iM = getIntersectionPoint(x_i,x_M,L);
            if(func.getF(x_iL) <= func.getF(x_iM)) {
                x_M = x_i;
            } else {
                x_L = x_i;
            }
            y_1 = func.getF(getIntersectionPoint(x_L, x_M, L));
            err = Math.abs(y_1 - y_0);
            y_0 = y_1;
            // may be minimum found in first iteration
            if(minimun_value > y_1) minimun_value = y_1;
            if(isInit) isInit = false; //reset init flag after first iteration
        }

        return new CalculationResult(true, minimun_value);
    }

}
