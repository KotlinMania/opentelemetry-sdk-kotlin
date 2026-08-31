// port-lint: source opentelemetry_sdk/src/metrics/view.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

/**
 * Used to customize the metrics that are output by the SDK.
 *
 * Here are some examples when a [View] might be needed:
 *
 * * Customize which Instruments are to be processed/ignored. For example, an
 *   instrumented library can provide both temperature and humidity, but the
 *   application developer might only want temperature.
 * * Customize the aggregation - if the default aggregation associated with the
 *   [Instrument] does not meet the needs of the user. For example, an HTTP client
 *   library might expose HTTP client request duration as Histogram by default,
 *   but the application developer might only want the total count of outgoing
 *   requests.
 * * Customize which attribute(s) are to be reported on metrics. For example,
 *   an HTTP server library might expose HTTP verb (e.g. GET, POST) and HTTP
 *   status code (e.g. 200, 301, 404). The application developer might only care
 *   about HTTP status code (e.g. reporting the total count of HTTP requests for
 *   each HTTP status code). There could also be extreme scenarios in which the
 *   application developer does not need any attributes (e.g. just get the total
 *   count of all incoming requests).
 */
public interface View {
    /**
     * Defines how data should be collected for certain instruments.
     *
     * Return [Stream] to use for matching [Instrument]s,
     * otherwise if there is no match, return `null`.
     */
    public fun matchInst(inst: Instrument): Stream?

    public companion object {
        /**
         * Creates a [View] from a lambda function.
         */
        public inline operator fun invoke(crossinline f: (Instrument) -> Stream?): View =
            object : View {
                override fun matchInst(inst: Instrument): Stream? = f(inst)
            }
    }
}
