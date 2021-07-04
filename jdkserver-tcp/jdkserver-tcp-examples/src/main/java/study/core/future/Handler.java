package study.core.future;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public interface Handler<E> {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param event 任意对象.
   * @author admin
   */
  void handle(E event);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param event 任意对象.
   * @author admin
   */
  default void handleSsl(E event) {}
  ;
}
