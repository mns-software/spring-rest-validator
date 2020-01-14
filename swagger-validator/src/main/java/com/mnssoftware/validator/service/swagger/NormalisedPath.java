package com.mnssoftware.validator.service.swagger;

import java.util.List;

/**
 * A normalised representation of an API path.
 * <p>
 * Normalised paths are devoid of path prefixes and contain a normalised starting/ending
 * slash to make comparisons easier.
 *
 * @author msilcox
 */
public interface NormalisedPath {

  /**
   * @return The path parts from the normalised path
   */
  List<String> parts();

  /**
   * @param index position of part
   * @return The path part at the given index
   * @throws IndexOutOfBoundsException if the provided index is not a valid index
   */
  String part(int index);

  /**
   * @param index position of part
   * @return Whether the path part at the given index is a path param (e.g. "/my/{param}/")
   * @throws IndexOutOfBoundsException if the provided index is not a valid index
   */
  boolean isParam(int index);

  /**
   * @param index position of part
   * @return The parameter name of the path part at the given index, or <code>null</code>
   * @throws IndexOutOfBoundsException if the provided index is not a valid index
   */
  String paramName(int index);

  /**
   * @return The original, un-normalised path string
   */
  String original();

  /**
   * @return The normalised path string, with prefixes removed and a standard treatment for leading/trailing slashed.
   */
  String normalised();
}
