package org.mockito.verification;

import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.InOrderImpl;
import org.mockito.internal.verification.InOrderWrapper;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.VerificationOverTimeImpl;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.internal.verification.api.VerificationInOrderMode;

public class VerificationWrapperInOrderWrapper implements VerificationMode {
    private final VerificationMode delegate;

    public VerificationWrapperInOrderWrapper(VerificationWrapper<?> verificationWrapper, InOrderImpl inOrder) {
        VerificationMode verificationMode = verificationWrapper.wrappedVerification;

        VerificationMode inOrderWrappedVerificationMode = wrapInOrder(verificationWrapper, verificationMode, inOrder);

        delegate = verificationWrapper.copySelfWithNewVerificationMode(inOrderWrappedVerificationMode);
    }

    @Override
    public void verify(VerificationData data) {
        delegate.verify(data);
    }

    @Override
    public VerificationMode description(String description) {
        return VerificationModeFactory.description(this, description);
    }

    private VerificationMode wrapInOrder(VerificationWrapper<?> verificationWrapper, VerificationMode verificationMode, InOrderImpl inOrder) {
        if (verificationMode instanceof VerificationInOrderMode) {
            final VerificationInOrderMode verificationInOrderMode = (VerificationInOrderMode)verificationMode;
            return new InOrderWrapper(verificationInOrderMode, inOrder);
        }

        if (verificationMode instanceof VerificationOverTimeImpl) {
            final VerificationOverTimeImpl verificationOverTime = (VerificationOverTimeImpl)verificationMode;
            if (verificationOverTime.isReturnOnSuccess()) {
                return new VerificationOverTimeImpl(verificationOverTime.getPollingPeriodMillis(),
                        verificationOverTime.getTimer().duration(),
                        wrapInOrder(verificationWrapper, verificationOverTime.getDelegate(), inOrder),
                        verificationOverTime.isReturnOnSuccess());
            }
        }

        throw new MockitoException(verificationMode.getClass().getSimpleName() +
                " is not implemented to work with InOrder wrapped inside a " +
                verificationWrapper.getClass().getSimpleName());
    }
}
