package exception;

/**
 * Exception thrown if a number is outside its allowed range
 *
 * @author Ralph Kretschmer
 * @version 0.1, 22.02.2009
 */
public class NumberOutOfRangeException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getMessage() {
	return "Number out of range.";
    }
}
