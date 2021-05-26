# Picnet Binding
![LogoSinthesi](doc/LogoSinthesi.png)

_The picnet binding  can be used to interact with the Sinthesi picnet home automation enviroment._ __
_The binding allows the user to connect to a PN MAS device (version 7 and up recommended, for previus model a PN TCP/IP module may be required) and, along with the program on the PN MAS itself allows the user to freely control all aspects of their home automation systems._


## Supported Things
_This binding supports all of Sinthesi control units (PN MAS, PN EASY, PN LIGHTON and PN REC) that have an onboard ethernet port or that are connected to your local network via a PN TCP/IP module_

## Thing Configuration

_The thing has the following parameters_ 

|Name           |Type  |Description                                                                                         |Accepted value                                                             |
|---------------|------|----------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
|Ip             |String|The ip address of the desired control unit                                                          |Any well formed ip address or host name (only supported by version 8 and up|
|Port           |String|The port used to communicate, usually 7001 or 4001 for PN TCP/IP devices                            |Any usable port number                                                     |
|pollingInterval|Int   |The interval in which the control unit is polled in milliseconds for new statuses, default is 100 ms|Any integer value                                                          |


## Channels

_In this binding there are no pre-defined channel, and it's up to the user to define the used channels._ __
_Each item must have a defined channel with the following syntax_

| item          | definition                                        |
|---------------|---------------------------------------------------|
| Switch        | Type#AddrBBit-trgAddr-trgBit-onValue-offValue-SW  |
| Contact       | Type#AddrBBit-CT                                  |
| Number        | Type#Addr-NB                                      |
| Rollershutter | Type#AddrBBit-upAddr-upBit-downAddr-downBit-RS    |
| Dimmer        | Type#Addr-DM                                      |

### Switch 
_Type: The address type whic can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables_ __
_Addr: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables_ __
_Bit: The bit for the ON/OFF state, both input output abd virtual have 16 bit for ON/OFF states_ __
_trgAddr: The address of the virtual variable that triggers the switch_ __
_trgBit: The bit of the virtual variable that triggers the switch_ __
_onValue: The value sent after an on command_ __
_offValue: The value sent after an off command_ __
_SW: item identifier for the switch_ __

_Note: At this moment, the only supported value for the switch onValue and offValue is 1 on both the on and off value, it's suggested that the trigger variable gets set by the binding and then is set back to 0_ __
_by the control unit with a dedicated macro (shown below)_ 

### Contact
_Type: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables_ __
_Addr: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables_ __
_Bit: The bit for the ON/OFF state, both input output abd virtual have 16 bit for ON/OFF states_ __
_CT: item identifier for the contact_ 

### Number
_Type: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables_ __
_Addr: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables_ __
_NB: item identifier for the number_ 

### Rollershutter
_Type: The address type which can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables_ __
_Addr: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables_ __
_upAddr: The address of the variables that moves the roller shutter up_ __
_upBit: The bit of the variable that moves the roller shutter up_ __
_downAddr: The address of the variables that moves the roller shutter down_ __
_downBit: The bit of the variable that moves the roller shutter down_ __
_RS: item identifier for the rollershutter_ 

### Dimmer
_Type: The address type whic can be Output (O) and Input (I) for modules and Virtual (V) for virtual variables_ __
_Addr: The address of the module for Input (I) or Output (O) or the address of the virtual variable (V), this value must be between 1 and 254 for Input and Output and between 1 and 2500 for Virtual variables_
_DM: item identifier for the rollershutter_

## Full Example

### Thing
_In this file we define both the thing we use and the channel the thing uses_ __
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
_In this file we define the items we will use to build our page_ __
```
Switch Item_Name_1 "Item label 1" {channel="picnet:pnmas:1:O#1B1-1-4-1-1-SW"}
Contact Item_Name_2 "Item label 2" {channel="picnet:pnmas:1:O#1B1-CT"}
Number Item_Name_3 "Item label 3" {channel="picnet:pnmas:1:V#2-NB"}
Rollershutter Item_Name_4 "Item label 4" {channel="picnet:pnmas:1:V#20B3-1100-1-1100-2-RS"}
Dimmer Item_Name_5 "Item label 5" {channel="picnet:pnmas:1:V#16-DM"}
```

## Pns macros
_Macro used in the pns to support the openhab switch_

|arg               |description                                                     |
|------------------|----------------------------------------------------------------|
|   p_virtualePC   | Virtual variable used by an openhab switch item                |
|  p_virtualeMemo  | Memory virtual variable used to save the state of p_virtualePC |

_The AutoResetFlagPC keep the value of p_virtualePC at the value set by the item for one program cycle on the control unit._ __ 
_This macro must be used for all items which toggle an item on or off using a virtual variable_



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