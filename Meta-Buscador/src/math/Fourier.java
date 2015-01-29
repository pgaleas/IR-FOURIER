package math;

/**
 * Fourier expansion
 * 
 * This class contains methods to calculate a sine-cosine Fourier expansion and 
 * related things for a function that is defined in the interval [0, L] (L 
 * integer).
 * 
 * The expansion functions used are orthonormal with respect to the scalar 
 * product of L_2([0, L]).
 * 
 * Here the length-scale parameter is the document length L.
 * 
 * Let a_0, a_1, ... be the cosine coefficients and b_1, b_2, ... the sine
 * coefficients. Then the array coeffs is given by coeffs[0] = a_0, 
 * coeffs[1] = b_1, coeffs[2] = a_1, coeffs[3] = b_2, and so on. 
 * 
 * ****************************************************************************
 * This class is modeled after FourierExpansionGeneral.java and 
 * FourierExpansion2.java and should therefore be correct.
 * ****************************************************************************
 * TODO RK: Check all formulas
 * 
 * @author Ralph Kretschmer
 * @version 0.1, 21.02.2009
 */
public class Fourier extends AbstractExpansion {
    
    /**
     * For the Fourier expansion the length scale is the document length
     */
    @Override
    public boolean hasVariableScale() {
	return false;
    }
    
    /**
     * Protected constructor
     */
    public Fourier() {
    	maxOrder = 30;
    }    
    
    /**
     * Calculates maxOrder + 1 Fourier coefficients a_0, b_1, a_1, b_2, a_2, ... 
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
            // Zeroth order
            int numberWords = wordPositions.length;
            coeffs[0] = (double)numberWords / Math.sqrt(scale);

            // Higher orders
            for (int n = 1; n <= maxOrder; n++) {
        	// nEff assumes values 1, 1, 2, 2, ...
        	int nEff = (n + 1) / 2;
        	if (n % 2 == 0) {
        	    // n even: cosine coefficients
        	    double a_coeff =  1 / (nEff * Math.PI) * Math.sqrt(scale / 2);
        	    double a_sumCoeff = 0;
        	    for (int i = 0; i < numberWords; i++) {
        		a_sumCoeff += Math.sin(2 * nEff * Math.PI * wordPositions[i] 
                            / scale);
        		a_sumCoeff -= Math.sin(2 * nEff * Math.PI * (wordPositions[i]-1) 
        		    / scale);
        	    }
        	    coeffs[n] = a_coeff * a_sumCoeff;
        	} else {
        	    // n odd: sine coefficients
        	    double b_coeff = -1 / (nEff * Math.PI) * Math.sqrt(scale / 2);
        	    double b_sumCoeff = 0;
        	    for (int i = 0; i < numberWords; i++) {
        		b_sumCoeff += Math.cos(2 * nEff * Math.PI * wordPositions[i] 
        		    / scale);
        		b_sumCoeff -= Math.cos(2 * nEff * Math.PI * (wordPositions[i]-1) 
        		    / scale);
        	    }
        	    coeffs[n] = b_coeff * b_sumCoeff;
            	}
            }
        }
        
        return coeffs;
    }
    
    /**
     * Calculates the Fourier expanded function
     * 
     * @see AbstractExpansion.getExpandedFunction()
     * 
     * TODO RK: Changed. This has to be checked very carefully.
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
            // Constant
            yValues[i] = coeffs[0] / Math.sqrt((double)documentLength);
            // Sum
            for (int n = 1; n <= order; n++) {
        	// nEff assumes values 1, 1, 2, 2, ...
        	int nEff = (n + 1) / 2;
        	if (n % 2 == 0) {
        	    // n even: cosine components
        	    yValues[i] += coeffs[n] * Math.sqrt(2 / (double)documentLength)
        	    	    * Math.cos(2 * Math.PI * nEff * xValues[i] 
        	    	    / (double)documentLength);
        	} else {
        	    // n odd: sine components
        	    yValues[i] += coeffs[n] * Math.sqrt(2 / (double)documentLength)
    			    * Math.sin(2 * Math.PI * nEff * xValues[i] 
    			    / (double)documentLength);
        	}
            }
        }
        
        double[][] results = {xValues, yValues};
        
    	return results;
    }
}
