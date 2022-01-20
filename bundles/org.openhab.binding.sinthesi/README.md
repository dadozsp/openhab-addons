# Picnet Binding

The sinthesi binding  can be used to interact with the Sinthesi picnet home automation environment.
The binding allows the user to connect to a PN MAS device (version 7 and up is recommended) via the Sinthesi Asynchronous Packet Protocol.


## Supported Things
This binding supports all of Sinthesi control units (PN MAS, PN EASY, PN LIGHTON and PN REC) that have an onboard ethernet port or that are connected to your local network via a PN TCP/IP module.

## Thing Configuration

The PN MAS thing has the following parameters.

|Name           |Type  |Description                                                                                         |Accepted value                                                             |
|---------------|------|----------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
|Ip             |String|The ip address of the desired control unit                                                          |Any well formed ip address or host name (only supported by version 8 and up|
|Port           |String|The port used to communicate, usually 7001 or 4001 for PN TCP/IP devices                            |Any usable port number                                                     |
|pollingInterval|Int   |The interval in which the control unit is polled in milliseconds for new statuses, default is 100 ms|Any integer value                                                          |


## Channels

In this binding there are no pre-defined channel, and it's up to the user to define the used channels.
Each item must have a defined channel with the following syntax.

| item          | definition                                                                        |
|---------------|-----------------------------------------------------------------------------------|
| Switch        | **Type**#**Addr**B**Bit**-**trgAddr**-**trgBit**-**onValue**-**offValue**-**SW**  |
| Contact       | **Type**#**Addr**B**Bit**-**CT**                                                  |
| Number        | **Type**#**Addr**-**NB**                                                          |
| Rollershutter | **Type**#**Addr**B**Bit**-**upAddr**-**upBit**-**downAddr**-**downBit**-**RS**    |
| Dimmer        | **Type**#**Addr**-**DM**                                                          |

### Switch
**Type**: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables.<br/>
**Addr**: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables.<br/>
**Bit**: The bit for the ON/OFF state, both input output abd virtual have 16 bit for ON/OFF states.<br/>
**trgAddr**: The address of the virtual variable that triggers the switch.<br/>
**trgBit**: The bit of the virtual variable that triggers the switch.<br/>
**onValue**: The value sent after an on command.<br/>
**offValue**: The value sent after an off command.<br/>
**SW**: item identifier for the switch.<br/>

**Note**: At this moment, the only supported value for the switch onValue and offValue is 1 (other value will be added in a future update) on both the on and off value, it's suggested that the trigger variable gets set by the binding and then is set back to 0 by the control unit with a dedicated macro (shown below)

### Contact
**Type**: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables.<br/>
**Addr**: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables.<br/>
**Bit**: The bit for the ON/OFF state, both input output abd virtual have 16 bit for ON/OFF states.<br/>
**CT**: item identifier for the contact.

### Number
**Type**: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables.<br/>
**Addr**: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables. <br/>
**NB**: item identifier for the number. <br/>

### Rollershutter
**Type**: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables. <br/>
**Addr**: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables. <br/>
**upAddr**: The address of the variables that moves the roller shutter up.<br/>
**upBit**: The bit of the variable that moves the roller shutter up.<br/>
**downAddr**: The address of the variables that moves the roller shutter down.<br/>
**downBit**: The bit of the variable that moves the roller shutter down.<br/>
**RS**: item identifier for the rollershutter.

### Dimmer
**Type**: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables.<br/>
**Addr**: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables.<br/>
**DM**: item identifier for the rollershutter.<br/>

## Full Example

### Thing
In this file we define both the thing we use and the channel the thing uses.
```
picnet:pnmas:1 [ip="192.168.1.113, port="7001", pollInterval=100] {
    Channels:
        Switch : O#1B1-1-4-1-1-SW
        Contact : O#1B1-CT
        Number : V#2-NB
        Rollershutter : V#20B3-1100-1-1100-2-RS
        Dimmer : V#16-DM
}
```

### Items
In this file we define the items we will use to build our page.
```
Switch Item_Name_1 "Item label 1" {channel="picnet:pnmas:1:O#1B1-1-4-1-1-SW"}
Contact Item_Name_2 "Item label 2" {channel="picnet:pnmas:1:O#1B1-CT"}
Number Item_Name_3 "Item label 3" {channel="picnet:pnmas:1:V#2-NB"}
Rollershutter Item_Name_4 "Item label 4" {channel="picnet:pnmas:1:V#20B3-1100-1-1100-2-RS"}
Dimmer Item_Name_5 "Item label 5" {channel="picnet:pnmas:1:V#16-DM"}
```

## Pns macros
Macro used in the pns to support the openhab switch.

|arg               |description                                                     |
|------------------|----------------------------------------------------------------|
|   p_virtualePC   | Virtual variable used by an openhab switch item                |
|  p_virtualeMemo  | Memory virtual variable used to save the state of p_virtualePC |

The AutoResetFlagPC keep the value of p_virtualePC at the value set by the item for one program cycle on the control unit.<br/>
This macro must be used for all items which toggle an item on or off using a virtual variable

```
MACRO AutoResetFlagPC(p_virtualePC, p_virtualeMemo)
{
    IF[p_virtualePC != p_virtualeMemo]
        p_virtualeMemo = p_virtualePC;
    ELSE
        p_virtualeMemo = 0;
        p_virtualePC = 0;
    ENDIF
}
```
