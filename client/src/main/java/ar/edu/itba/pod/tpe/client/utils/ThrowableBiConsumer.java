package ar.edu.itba.pod.tpe.client.utils;

@FunctionalInterface
public interface ThrowableBiConsumer<T, U, E extends Exception> {

        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @throws E the exception
         */
        void accept(T t, U u) throws E;
}
