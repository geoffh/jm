package jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;

import java.util.logging.Logger;

public class MediaAction extends ActionCallback {
    private final ActionCallback mCallback;
    private boolean mComplete = false;
    private final Object mLock = new Object();
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    private int mDebug = 0;
    public MediaAction( ActionCallback inCallback, int inDebug ) {
        super( inCallback.getActionInvocation() );
        mCallback = inCallback;
        mDebug = 1;
    }
    public MediaAction( ActionCallback inCallback ) {
        super( inCallback.getActionInvocation() );
        mCallback = inCallback;
    }

    @Override
    public void run() {
        try {
            //if ( mDebug == 1 )
             //doit();
           // else
             super.run();
        } finally {
            setComplete();
        }
    }

    /*
    void doit() {
        Service service = actionInvocation.getAction().getService();

        // Local execution
        if (service instanceof LocalService) {
            LocalService localService = (LocalService)service;

            // Executor validates input inside the execute() call immediately
            Action theAction = actionInvocation.getAction();
            ActionExecutor theExecutor = localService.getExecutor(theAction);
            MethodActionExecutor m = ( MethodActionExecutor )theExecutor;
            m.execute(actionInvocation);
            System.out.println( "Map " + actionInvocation.getOutputMap() );
            Object o = actionInvocation.getOutput( "MinValue" );
            //localService.getExecutor(actionInvocation.getAction()).execute(actionInvocation);

            if (actionInvocation.getFailure() != null) {
                failure(actionInvocation, null);
            } else {
                success(actionInvocation);
            }

        // Remote execution
        } else if (service instanceof RemoteService){

            if (getControlPoint()  == null) {
                throw new IllegalStateException("Callback must be executed through ControlPoint");
            }

            RemoteService remoteService = (RemoteService)service;

            // Figure out the remote URL where we'd like to send the action request to
            URL controLURL = remoteService.getDevice().normalizeURI(remoteService.getControlURI());

            // Do it
            SendingAction prot = getControlPoint().getProtocolFactory().createSendingAction(actionInvocation, controLURL);
            prot.run();

            IncomingActionResponseMessage response = prot.getOutputMessage();

            if (response == null) {
                failure(actionInvocation, null);
            } else if (response.getOperation().isFailed()) {
                failure(actionInvocation, response.getOperation());
            } else {
                success(actionInvocation);
            }
        }
    }

    public boolean isUseOutputArgumentAccessors(MethodActionExecutor m, ActionInvocation<LocalService> actionInvocation) {
        for (ActionArgument argument : actionInvocation.getAction().getOutputArguments()) {
            // If there is one output argument for which we have an accessor, all arguments need accessors
            if (m.getOutputArgumentAccessors().get(argument) != null) {
                return true;
            }
        }
        return false;
    }

    public     Object[] createInputArgumentValues(ActionInvocation<LocalService> actionInvocation, Method method) throws ActionException {

        LocalService service = actionInvocation.getAction().getService();

        Object[] values = new Object[actionInvocation.getAction().getInputArguments().size()];
        int i = 0;
        for (ActionArgument<LocalService> argument : actionInvocation.getAction().getInputArguments()) {

            Class methodParameterType = method.getParameterTypes()[i];

            ActionArgumentValue<LocalService> inputValue = actionInvocation.getInput(argument);

            // If it's a primitive argument, we need a value
            if (methodParameterType.isPrimitive() && (inputValue == null || inputValue.toString().length() == 0))
                throw new ActionException(
                        ErrorCode.ARGUMENT_VALUE_INVALID,
                        "Primitive action method argument '" + argument.getName() + "' requires input value, can't be null or empty string"
                );

            // It's not primitive and we have no value, that's fine too
            if (inputValue == null) {
                values[i++] = null;
                continue;
            }

            // If it's not null, maybe it was a string-convertible type, if so, try to instantiate it
            String inputCallValueString = inputValue.toString();
            // Empty string means null and we can't instantiate Enums!
            if (inputCallValueString.length() > 0 && service.isStringConvertibleType(methodParameterType) && !methodParameterType.isEnum()) {
                try {
                    Constructor<String> ctor = methodParameterType.getConstructor(String.class);
                    mLogger.finer("Creating new input argument value instance with String.class constructor of type: " + methodParameterType);
                    Object o = ctor.newInstance(inputCallValueString);
                    values[i++] = o;
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                    throw new ActionException(
                            ErrorCode.ARGUMENT_VALUE_INVALID, "Can't convert input argment string to desired type of '" + argument.getName() + "': " + ex
                    );
                }
            } else {
                // Or if it wasn't, just use the value without any conversion
                values[i++] = inputValue.getValue();
            }
        }
        return values;
    }*/

    @Override
    public void failure( ActionInvocation inActionInvocation, UpnpResponse inResponse, String inDefaultMsg ) {
        mLogger.finest( "MediaAction failure: " +
                        "\n Invocation: " + inActionInvocation +
                        "\n Response: " + inResponse +
                        "\n Message: " + inDefaultMsg );
        mCallback.failure( inActionInvocation, inResponse, inDefaultMsg );
    }


    @Override
    public void success( ActionInvocation inActionInvocation ) {
        mCallback.success( inActionInvocation );
    }
    
    public void waitForComplete() {
        while ( true ) {
            synchronized( mLock ) {
                if ( mComplete ) {
                    return;
                }
                try {
                    mLock.wait();
                } catch ( InterruptedException theException ) {
                    mLogger.throwing( "MediaAction", "waitForComplete", theException );
                }
            }
        }
    }

    private void setComplete() {
        synchronized( mLock ) {
            mComplete = true;
            mLock.notify();
        }
    }
}
