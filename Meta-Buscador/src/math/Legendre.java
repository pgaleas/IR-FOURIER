package math;

/**
 * This class contains some methods related to shifted Legendre polynomials
 * 
 * The shifted polynomials are P*_n(x) = P_n(2 x - 1), where P_n(x) are usual
 * Legendre polynomials.
 * 
 * The Legendre expansion is defined with respect to the L_2([0, L]) scalar 
 * product (f, g) = \int_0^L f^*(x) g(x) dx, so that the orthonormal basis 
 * functions are
 * phi_n(x) = \sqrt{(2 n + 1) / L} P*_n(x / L).
 * 
 * @author Ralph Kretschmer
 * @version 0.1, 26.03.2009
 *
 */
public class Legendre extends AbstractExpansion {
    
    /**
     * Coefficient arrays for shifted Legendre polynomials P*_n
     * 
     * Structure: lPoly[order][power] is the coefficient of x^power of 
     * P*_order, i. e. the innermost array holds the polynomial coefficients. 
     * 
     * Example: P*_2(x) = 1 - 6 x + 6 x^2, thus lPoly[2][0] = 1, 
     * lPoly[2][1] = -6, lPoly[2][2] = 6.
     */
    private final double[][] lPoly = {
	// P*_0(x) = 1 
	{ 1.0,   0,      0,       0,      0,       0,     0},
	// P*_1(x) = -1 + 2 x
	{-1.0,   2.0,    0,       0,      0,       0,     0},
	// P*_2(x) = 1 - 6 x + 6 x^2
	{ 1.0,  -6.0,    6.0,     0,      0,       0,     0},
	// P*_3(x) = -1 + 12 x - 30 x^2 + 20 x^3
	{-1.0,  12.0,  -30.0,    20.0,    0,       0,     0},
	// P*_4(x) = 1 - 20 x + 90 x^2 - 140 x^3 + 70 x^4
	{ 1.0, -20.0,   90.0,  -140.0,   70.0,     0,     0},
	// P*_5(x) = -1 + 30 x - 210 x^2 + 560 x^3 - 630 x^4 + 252 x^5
	{-1.0,  30.0, -210.0,   560.0, -630.0,   252.0,   0},
	// P*_6(x) = 1 - 42 y + 420 y^2 - 1680 y^3 + 3150 y^4 - 2772 y^5 + 924 y^6
	{ 1.0, -42.0,  420.0, -1680.0, 3150.0, -2772.0, 924.0}
    };
    
    /**
     * Protected constructor
     */
    protected Legendre() {
	maxOrder = 6;
    }
    
    /**
     * For the Legendre expansion the length scale is the document length
     */
    @Override
    public boolean hasVariableScale() {
	return false;
    }
    
    /**
     * Calculates a value of a shifted Legendre polynomial
     * 
     * @param order Order of Legendre polynomial
     * @param x Value for which function is evaluated
     * @return Value of shifted Legendre polynomial at point x
     */
    private double getLegendre(int order, double x) {
        double value = 0;
        
        double[] pCoeffs = lPoly[order];
        value += pCoeffs[0];
        // Powers of x
        double xPower = 1;
        for (int power = 1; power < pCoeffs.length; power++) {
            xPower *= x;
            value += pCoeffs[power] * xPower; 
        }
        
        return value;
    }

    /**
     * Calculates coefficients c_0, c_1, ..., c_maxOrder of an expansion in 
     * terms of shifted Legendre polynomials
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
        	// Summation over all powers of polynomial
        	for (int i = 0; i <= n; i++) {
        	    double partialSum = 0;
        	    for (int j = 0; j < numberWords; j++) {
        		double power1 = 1, power2 = 1;
        		// Powers
        		for (int k = 1; k <= i + 1; k++) {
        		    power1 *= (double)wordPositions[j] / scale;
        		    power2 *= (double)(wordPositions[j] - 1) / scale;
        		}
        		partialSum += power1 - power2;
        	    }
        	    partialSum *= lPoly[n][i] / (i + 1);
        	    coeffs[n] += partialSum;
        	}
        	coeffs[n] *= Math.sqrt((double)(2 * n + 1) * scale);
            }
        }
                
        return coeffs;
    }
    
    /**
     * Calculates the Legendre expanded function
     *
     * This method calculates the x and y values for the Legendre expansion,
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
                yValues[i] += coeffs[n]  * getLegendre(n, xValues[i] / scale) 
                    	* Math.sqrt((double)(2 * n + 1) / scale);
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
        String polyString = String.valueOf(lPoly[order][0]);
        
        for (int power = 1; power < lPoly[order].length; power++) {
            double coeff = lPoly[order][power];
            if (coeff != 0.0) {
                polyString += (coeff > 0 ? " + " : " - ") 
                    	+ String.valueOf(Math.abs(coeff)) 
                    	+ " * x^" + String.valueOf(power);
            }
        }
        
        return polyString;
    }
}
