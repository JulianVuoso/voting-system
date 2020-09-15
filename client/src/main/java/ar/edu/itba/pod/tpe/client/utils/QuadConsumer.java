package ar.edu.itba.pod.tpe.client.utils;

@FunctionalInterface
public interface QuadConsumer<W, X, Y, Z> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param w the first input argument
     * @param x the second input argument
     * @param y the third input argument
     * @param z the fourth input argument
     */
    void accept(W w, X x, Y y, Z z);
}
