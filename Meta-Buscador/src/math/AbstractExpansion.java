package math;

import exception.IllegalExpansionException;

/**
 * Abstract class for all concrete expansions, e. g. Fourier, Legendre etc.
 *
 * Notes:
 * * It is assumed that the expanded function (the word distribution) assumes 
 *   only the values 0 and 1.
 * * This class contains the static factory method createExpansion() to create 
 *   concrete expansions.
 *
 * @author Ralph Kretschmer
 * @version 0.1, 21.02.2009
 */
public abstract class AbstractExpansion {
    
    /**
     * Array describing the word distribution.
     */
    protected int[] wordPositions = null;
    
    /**
     * Maximum allowed order of expansion
     * 
     * This property only depends on the concrete expansion. It has to be set 
     * in the constructor of concrete expansions.
     */
    protected int maxOrder;
    
    /**
     * Length-scale parameter
     * 
     * This parameter may either be constant (e. g. the document length in the
     * case of a Fourier expansion) or variable (e. g. in the case of a
     * Laguerre expansion).
     * 
     * Note that scale should be larger than 0.
     */
    protected double scale = 1;
    
    /**
     * Array holding expansion coefficients
     */
    protected double[] coeffs;
    
    /**
     * Protected constructor
     * 
     * Concrete expansions should only be created using the factory method
     * createExpansion() (see below). This means that concrete expansions 
     * should have a protected constructor. This constructor has to set the
     * default value of the property maxOrder.
     * 
     * @see createExpansion()
     */
    protected AbstractExpansion() {}
    
    /**
     * Sets the word distribution to be expanded
     * 
     * @param wordPositions Array describing the word distribution. Structure:
     *  {1, 3, 5} if the words are at positions 1, 3 and 5. May be null.
     */
    public void setWordPositions(int[] wordPositions) {
    	this.wordPositions = wordPositions;
    }
    
    /**
     * Set the maximal order of the expansion
     *
     * @param order  The expansion order 
     */
    public void setMaxOrder(int maxOrder) {
    	this.maxOrder = maxOrder;
    }
    
    public int getMaxOrder() {
    	return maxOrder;
    }
    
    public int[] getWordPositions() {
    	return wordPositions;
    }
    
    public int getWordNumber() {
		if (wordPositions == null) {
		    return 0;
		} else {
		    return wordPositions.length;
		}
    }
        
    /**
     * Sets the length-scale parameter
     * 
     * @param scale
     */
    public void setScale(double scale) {
    	this.scale = scale;
    }
    
    public double getScale() {
    	return scale;
    }
    
    /**
     * Whether or not the expansion has a variable length scale
     * 
     * This method has to be overridden for expansions with variable length
     * scale.
     */
    public boolean hasVariableScale() {
    	return false;
    }
    
    /**
     * Calculates coefficients c_0, c_1, ..., c_maxOrder of an expansion of the
     * word distribution in wordPositions in terms of certain expansion 
     * functions. The results are stored in the class variable coeffs (for 
     * later use) and returned.
     * 
     * @return Array containing the expansion coefficients
     */ 
    public abstract double[] calculateCoeffs();
    
    /**
     * Returns the previously calculated expansion coefficients
     * 
     * @return The expansion coefficients
     */
    public double[] getCoeffs() {
    	return coeffs;
    }
    
    /**
     * Calculates the expanded function
     *
     * This method calculates the x and y values for the expansion,
     * y = \sum c_n phi_n(x). It uses the expansion coefficients previously 
     * stored in the member variable coeffs.
     *
     * The x values are equally spaced in the whole interval 
     * [0, documentLength].
     *
     * @param documentLength Length of the document (maximum x value)
     * @param order Order up to which the expansion is calculated (i. e. the 
     *  coefficients c_0 up to c_order are taken into account)
     * @return Array containing arrays for the x and y values
     */
    public abstract double[][] getExpandedFunction(int documentLength, int order);
    
    /**
     * Factory method to create concrete expansions
     * 
     * Every concrete expansion has to be listed here.
     * 
     * @param expansionName
     * @return A concrete expansion
     * @throws IllegalExpansionException
     */
    public static AbstractExpansion createExpansion(String expansionName) 
    	    throws IllegalExpansionException {
				if (expansionName.equals("Fourier")) {
				    return new Fourier();
				} else if (expansionName.equals("Legendre")) {
				    return new Legendre();    
				} else if (expansionName.equals("Laguerre")) {
				    return new Laguerre();
				} else {
				    throw new IllegalExpansionException();
				}
    }
}
