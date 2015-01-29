package math;

/**
 * This class contains some methods related to Laguerre polynomials
 *
 * The Laguerre expansion is defined with respect to the L_2([0, \infty[) 
 * scalar product (f, g) = \int_0^\infty f^*(x) g(x) dx, so that the 
 * orthonormal basis functions are 
 * phi_n(x) = (exp(-x / (2 scale)) / \sqrt(scale)) L_n(x / scale),
 * where scale is a positive scale parameter.
 * 
 * @author Ralph Kretschmer
 * @version 0.1, 21.02.2009
 */
public class Laguerre extends AbstractExpansion {
    
    /**
     * Coefficient arrays for Laguerre polynomials L_n and their derivatives
     * 
     * The derivatives are included here for performance reasons. See
     * checkDerivatives() for a consistency check.
     * 
     * Structure: lPoly[order][derivative][power] is the coefficient of x^power 
     * of the derivative-th derivative of L_order, i. e. the innermost array 
     * holds the polynomial coefficients. 
     * 
     * Example: L_2(x) =  1 - 2 x + (1 / 2) x^2, thus lPoly[2][0][0] = 1, 
     * lPoly[2][0][1] = -2, lPoly[2][0][2] = 1 / 2.
     * 
     * Note that those arrays that are set to null are never used, i. e. it is
     * not necessary to filter then out explicitly.
     */
    private final double[][][] lPoly = {
        {
            // L_0(x) = 1
            {1.0, 0, 0, 0, 0, 0, 0},
            // 1st derivative of L_0    
            null,
            // 2nd derivative of L_0
            null,
            // 3rd derivative of L_0
            null,
            // 4th derivative of L_0
            null,
            // 5th derivative of L_0
            null,
            // 6th derivative of L_0
            null
        },
        {
            // L_1(x) = 1 - x
            { 1.0, -1.0, 0, 0, 0, 0, 0},
            // 1st derivative of L_1    
            {-1.0,    0, 0, 0, 0, 0, 0},
            // 2nd derivative of L_1
            null,
            // 3rd derivative of L_1
            null,
            // 4th derivative of L_1
            null,
            // 5th derivative of L_1
            null,
            // 6th derivative of L_1
            null
        },
        {
            // L_2(x) = 1 - 2 x + (1 / 2) x^2
            { 1.0, -2.0, 1.0 / 2.0, 0, 0, 0, 0},
            // 1st derivative of L_2
            {-2.0,  1.0,         0, 0, 0, 0, 0},
            // 2nd derivative of L_2
            { 1.0,    0,         0, 0, 0, 0, 0},
            // 3rd derivative of L_2
            null,
            // 4th derivative of L_2
            null,
            // 5th derivative of L_2
            null,
            // 6th derivative of L_2
            null
        },
        {
            // L_3(x) = 1 - 3 x + (3 / 2) x^2 - (1 / 6) x^3
            { 1.0, -3.0, 3.0 / 2.0, -1.0 / 6.0, 0, 0, 0},
            // 1st derivative of L_3
            {-3.0,  3.0, -1.0 / 2.0,         0, 0, 0, 0},
            // 2nd derivative of L_3
            { 3.0, -1.0,         0,          0, 0, 0, 0},
            // 3rd derivative of L_3
            {-1.0,    0,         0,          0, 0, 0, 0},
            // 4th derivative of L_3
            null,
            // 5th derivative of L_3
            null,
            // 6th derivative of L_3
            null
        },
        {
            // L_4(x) = 1 - 4 x + 3 x^2 - (2 / 3) x^3 + (1 / 24) x^4,
            { 1.0, -4.0,       3.0, -2.0 / 3.0, 1.0 / 24.0, 0, 0},
            // 1st derivative of L_4
            {-4.0,  6.0,      -2.0,  1.0 / 6.0,          0, 0, 0},
            // 2nd derivative of L_4
            { 6.0, -4.0, 1.0 / 2.0,          0,          0, 0, 0},
            // 3rd derivative of L_4
            {-4.0,  1.0,         0,          0,          0, 0, 0},
            // 4th derivative of L_4
            { 1.0,    0,         0,          0,          0, 0, 0},
            // 5th derivative of L_4
            null,
            // 6th derivative of L_4
            null
        },
        {
            // L_5(x) = 1 - 5 x + 5 x^2 - (5 / 3) x^3 + (5 / 24) x^4 
            //     - (1 / 120) x^5
            {  1.0,  -5.0,        5.0, -5.0 / 3.0,  5.0 / 24.0, -1.0 / 120.0, 0},
            // 1st derivative of L_5
            { -5.0,  10.0,       -5.0,  5.0 / 6.0, -1.0 / 24.0,            0, 0},
            // 2nd derivative of L_5
            { 10.0, -10.0,  5.0 / 2.0, -1.0 / 6.0,           0,            0, 0},
            // 3rd derivative of L_5
            {-10.0,   5.0, -1.0 / 2.0,          0,           0,            0, 0},
            // 4th derivative of L_5
            {  5.0,  -1.0,          0,          0,           0,            0, 0},
            // 5th derivative of L_5
            { -1.0,     0,          0,          0,           0,            0, 0},
            // 6th derivative of L_5
            null
        },
        {
            // L_6(x) = 1 - 6 x + (15 / 2) x^2 - (10 / 3) x^3 + (5 / 8) x^4 
            //     - (1 / 20) x^5 + (1 / 720) x^6
            {   1.0,  -6.0, 15.0 / 2.0, -10.0 / 3.0,   5.0 / 8.0, -1.0 / 20.0, 1.0 / 720.0},
            // 1st derivative of L_6
            {  -6.0,  15.0,      -10.0,   5.0 / 2.0,  -1.0 / 4.0, 1.0 / 120.0,           0},
            // 2nd derivative of L_6
            {  15.0, -20.0, 15.0 / 2.0,        -1.0,  1.0 / 24.0,           0,           0},
            // 3rd derivative of L_6
            { -20.0,  15.0,       -3.0,   1.0 / 6.0,           0,           0,           0},
            // 4th derivative of L_6
            {  15.0,  -6.0,  1.0 / 2.0,           0,           0,           0,           0},
            // 5th derivative of L_6
            {  -6.0,   1.0,          0,           0,           0,           0,           0},
            // 6th derivative of L_6
            {   1.0,     0,          0,           0,           0,           0,           0}
        }
    };
    
    /**
     * Protected constructor
     */
    protected Laguerre() {
	maxOrder = 6;
    }
    
    /**
     * The Laguerre expansion has a variable length scale
     */
    @Override
    public boolean hasVariableScale() {
	return true;
    }
    
    /**
     * Calculates a value of a Laguerre polynomial or its derivative
     * 
     * @param order Order of Laguerre polynomial
     * @param derivative Derivative
     * @param x Value for which function is evaluated
     * @return Value of Laguerre polynomial or its derivative at point x
     */
    private double getLaguerre(int order, int derivative, double x) {
        double value = 0;
        
        if (lPoly[order][derivative] != null) {
            double[] pCoeffs = lPoly[order][derivative];
            value += pCoeffs[0];
            // Powers of x
            double xPower = 1;
            for (int power = 1; power < pCoeffs.length; power++) {
                xPower *= x;
                value += pCoeffs[power] * xPower; 
            }
        }
        
        return value;
    }
    
    /**
     * Calculates coefficients c_0, c_1, ..., c_maxOrder of an expansion in 
     * terms of Laguerre polynomials
     * 
     * @see AbstractExpansion.calculateCoeffs()
     */
    @Override
    public double[] calculateCoeffs() {
        coeffs = new double[maxOrder + 1];
        // Initialization
        for (int n = 0; n <= maxOrder; n++) {
            coeffs[n] = 0;
        }
        
        if (wordPositions != null) {
            int numberWords = wordPositions.length;
            
            for (int n = 0; n <= maxOrder; n++) {
                for (int i = 0; i < numberWords; i++) {
                    coeffs[n] += getCoeff(n, wordPositions[i]);
                }
            }            
        }
        
        return coeffs;
    }
    
    /**
     * Returns the Laguerre expansion coefficient for one word position
     * 
     * This method contains the actual calculation of expansion coefficients. 
     * For simplicity, it is only applicable for one word position.
     * 
     * @param order Order of expansion
     * @param wordPosition One position of a word 
     * @return Expansion coefficient
     */
    private double getCoeff(int order, int wordPosition) {
        double coeff = 0;
        double power2 = 1.0;
        
        for (int derivative = 0; derivative <= order; derivative++) {
            coeff += power2 
                    * (Math.exp(1 / (2.0 * scale)) 
                    * getLaguerre(order, derivative, (wordPosition - 1) / scale)
                    - getLaguerre(order, derivative, wordPosition / scale));
            power2 *= 2.0;
        }
        
        coeff *= 2.0 * Math.sqrt(scale) * Math.exp(- wordPosition / (2.0 * scale));
        
        return coeff;
    }
    
    /**
     * Calculates the Laguerre expanded function
     *
     * This method calculates the x and y values for the Laguerre expansion,
     * y = \sum c_n phi_n(x).
     * 
     * @see AbstractExpansion.getExpandedFunction()
     */
    @Override
    public double[][] getExpandedFunction(int documentLength, int order) {
        // 201 points are calculated
        double spacing = 0.005 * documentLength;

        // Array with x values
        double[] xValues = new double[201];
        for (int i = 0; i <= 200; i++) {
            xValues[i] = spacing * i;
        }

        // Array with y values
        double[] yValues = new double[xValues.length];
        for (int i = 0; i < xValues.length; i++) {
            for (int n = 0; n <= order; n++) {
                yValues[i] += coeffs[n] * Math.exp(- xValues[i] / (2.0 * scale)) 
                    	* getLaguerre(n, 0, xValues[i] / scale) / Math.sqrt(scale);
            }
        }

        double[][] results = {xValues, yValues};

        return results;
    }
    
    /**
     * Returns a string representation of a polynomial
     * 
     * @param order The order of the polynomial
     */
    public String toString(int order) {
        String polyString = String.valueOf(lPoly[order][0][0]);
        
        for (int power = 1; power < lPoly[order][0].length; power++) {
            double coeff = lPoly[order][0][power];
            if (coeff != 0.0) {
                polyString += (coeff > 0 ? " + " : " - ") 
                    	+ String.valueOf(Math.abs(coeff)) 
                    	+ " * x^" + String.valueOf(power);
            }
        }
        
        return polyString;
    }    
}
