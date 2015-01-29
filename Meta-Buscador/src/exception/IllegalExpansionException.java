package exception;

/**
 * Exception thrown if expansion is not implemented
 *
 * @author Ralph Kretschmer
 * @version 0.1, 21.02.2009
 */
public class IllegalExpansionException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getMessage() {
	return "Expansion not implemented.";
    }
}
