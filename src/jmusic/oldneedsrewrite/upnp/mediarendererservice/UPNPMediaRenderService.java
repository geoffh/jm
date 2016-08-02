package jmusic.oldneedsrewrite.upnp.mediarendererservice;

import jmusic.oldneedsrewrite.player.Player;
import jmusic.oldneedsrewrite.util.VolumeRange;
import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpInputArgument;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;
import org.teleal.cling.binding.annotations.UpnpStateVariables;
import org.teleal.cling.model.types.ErrorCode;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.model.Channel;
import org.teleal.cling.support.model.PresetName;
import org.teleal.cling.support.model.VolumeDBRange;
import org.teleal.cling.support.renderingcontrol.RenderingControlException;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

@UpnpService(
    serviceId = @UpnpServiceId("RenderingControl"),
    serviceType = @UpnpServiceType(value = "RenderingControl", version = 1),
    stringConvertibleTypes = LastChange.class
)
@UpnpStateVariables({
    @UpnpStateVariable(
        name = "PresetNameList",
        sendEvents = false,
        datatype = "string"),
    @UpnpStateVariable(
        name = "Mute",
        sendEvents = false,
        datatype = "boolean"),
    @UpnpStateVariable(
        name = "Volume",
        sendEvents = false,
        datatype = "ui2",
        allowedValueMinimum = 0,
        allowedValueMaximum = 100),
    @UpnpStateVariable(
        name = "VolumeDB",
        sendEvents = false,
        datatype = "i4"),
    @UpnpStateVariable(
        name = "Loudness",
        sendEvents = false,
        datatype = "boolean"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_Channel",
        sendEvents = false,
        allowedValuesEnum = Channel.class),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_PresetName",
        sendEvents = false,
        allowedValuesEnum = PresetName.class),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_InstanceID",
        sendEvents = false,
        datatype = "ui4")

})

public class UPNPMediaRenderService {
    @UpnpStateVariable(eventMaximumRateMilliseconds = 200)
    final private LastChange lastChange;
    private final Logger mLogger = Logger.getLogger( UPNPMediaRenderService.class.getName() );
    final protected PropertyChangeSupport propertyChangeSupport;
    private boolean mMute = false;

    public UPNPMediaRenderService() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = new LastChange(new RenderingControlLastChangeParser());
    }

    public UPNPMediaRenderService( LastChange lastChange ) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = lastChange;
    }

    public UPNPMediaRenderService( PropertyChangeSupport propertyChangeSupport ) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = new LastChange(new RenderingControlLastChangeParser());
    }

    public UPNPMediaRenderService( PropertyChangeSupport propertyChangeSupport, LastChange lastChange ) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = lastChange;
    }

    public LastChange getLastChange() {
        return lastChange;
    }

    public void fireLastChange() {
        getLastChange().fire(getPropertyChangeSupport());
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public static UnsignedIntegerFourBytes getDefaultInstanceID() {
        return new UnsignedIntegerFourBytes(0);
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentPresetNameList", stateVariable = "PresetNameList"))
    public String listPresets(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId) throws RenderingControlException {
        return PresetName.FactoryDefault.toString();
    }

    @UpnpAction
    public void selectPreset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                             @UpnpInputArgument(name = "PresetName") String presetName) throws RenderingControlException {
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentMute", stateVariable = "Mute"))
    public boolean getMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                           @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return mMute;
    }

    @UpnpAction
    public void setMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                        @UpnpInputArgument(name = "Channel") String channelName,
                        @UpnpInputArgument(name = "DesiredMute", stateVariable = "Mute") boolean desiredMute) throws RenderingControlException {
        mMute = desiredMute;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentVolume", stateVariable = "Volume"))
    public UnsignedIntegerTwoBytes getVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                             @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return new UnsignedIntegerTwoBytes( 0 );
    }

    @UpnpAction
    public void setVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                          @UpnpInputArgument(name = "Channel") String channelName,
                          @UpnpInputArgument(name = "DesiredVolume", stateVariable = "Volume") UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {}

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentVolume", stateVariable = "VolumeDB"))
    public Integer getVolumeDB(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                               @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return 0;
    }

    @UpnpAction
    public void setVolumeDB(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                            @UpnpInputArgument(name = "Channel") String channelName,
                            @UpnpInputArgument(name = "DesiredVolume", stateVariable = "VolumeDB") Integer desiredVolumeDB) throws RenderingControlException {
        Player.getInstance().setVolumeDB( desiredVolumeDB );
    }


    @UpnpAction(out = {
        @UpnpOutputArgument(name = "MinValue", stateVariable = "VolumeDB", getterName = "getMinValue"),
        @UpnpOutputArgument(name = "MaxValue", stateVariable = "VolumeDB", getterName = "getMaxValue")
    })
    public VolumeRange getVolumeDBRange(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                        @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        VolumeDBRange theDBRange = Player.getInstance().getVolumeDBRange();
        return new VolumeRange( theDBRange.getMinValue(), theDBRange.getMaxValue());
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentLoudness", stateVariable = "Loudness"))
    public boolean getLoudness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                               @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return false;
    }

    @UpnpAction
    public void setLoudness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                            @UpnpInputArgument(name = "Channel") String channelName,
                            @UpnpInputArgument(name = "DesiredLoudness", stateVariable = "Loudness") boolean desiredLoudness) throws RenderingControlException {}

    protected Channel getChannel(String channelName) throws RenderingControlException {
        try {
            return Channel.valueOf(channelName);
        } catch (IllegalArgumentException ex) {
            throw new RenderingControlException( ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }
}