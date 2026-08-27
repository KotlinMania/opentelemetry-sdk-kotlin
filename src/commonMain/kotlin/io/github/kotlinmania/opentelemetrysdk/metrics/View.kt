// port-lint: source metrics/view.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

/**
 * Used to customize the metrics that are output by the SDK.
 */
public fun interface View {
    /**
     * Defines how data should be collected for certain instruments.
     *
     * Return [Stream] to use for matching [Instrument]s,
     * otherwise if there is no match, return `null`.
     */
    public fun matchInst(inst: Instrument): Stream?
}
