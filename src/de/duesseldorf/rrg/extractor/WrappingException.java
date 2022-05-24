package de.duesseldorf.rrg.extractor;

/**
 * thrown when trying to perform wrapping during extraction that does not work.
 * E.G. when the wrapping subtrees (subtrees below the ddaughter) can not be
 * appended because there are none in the RRGParseTree for the requested item.
 *
 * @author david
 */
public class WrappingException extends Exception {
}
