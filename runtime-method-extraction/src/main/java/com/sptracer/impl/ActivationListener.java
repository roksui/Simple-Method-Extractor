package com.sptracer.impl;

import com.sptracer.error.ErrorCapture;

public interface ActivationListener {

    /**
     * A callback for {@link AbstractSpan#activate()}
     *
     * @param span the {@link AbstractSpan} that is being activated
     * @throws Throwable if there was an error while calling this method
     */
    void beforeActivate(AbstractSpan<?> span) throws Throwable;

    /**
     * A callback for {@link ErrorCapture#activate()}
     *
     * @param error the {@link ErrorCapture} that is being activated
     * @throws Throwable if there was an error while calling this method
     */
    void beforeActivate(ErrorCapture error) throws Throwable;

    /**
     * A callback for {@link AbstractSpan#deactivate()}
     * <p>
     * Note: the corresponding span may already be {@link AbstractSpan#end() ended} and {@link AbstractSpan#resetState() recycled}.
     * That's why there is no {@link AbstractSpan} parameter.
     * </p>
     *
     * @param deactivatedSpan the context that has just been deactivated
     * @throws Throwable if there was an error while calling this method
     */
    void afterDeactivate(AbstractSpan<?> deactivatedSpan) throws Throwable;

    /**
     * A callback for {@link ErrorCapture#deactivate()}
     *
     * @param deactivatedError the error that has just been deactivated
     * @throws Throwable if there was an error while calling this method
     */
    void afterDeactivate(ErrorCapture deactivatedError) throws Throwable;
}
