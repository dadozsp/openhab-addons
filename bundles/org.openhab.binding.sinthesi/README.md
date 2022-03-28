# Sinthesi Binding

The sinthesi binding  can be used to interact with the Sinthesi picnet home automation environment.
The binding allows the user to connect to a PN MAS device (version 7 and up is recommended) via the Sinthesi Asynchronous Packet Protocol (Sapp).


## Supported Things

This binding supports all of Sinthesi control units (PN MAS, PN EASY, PN LIGHTON and PN REC) that have an onboard ethernet port or that are connected to your network via a PN TCP/IP module.

## Thing Configuration

The PN MAS thing has the following parameters.

| Name            | Type   | Description                                                                                          | Accepted value                                                               |
|-----------------|--------|------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| Ip              | String | The ip address of the desired control unit                                                           | Any well formed ip address or host name (only supported by version 8 and up) |
| Port            | String | The port used to communicate, usually 7001 or 4001 for PN TCP/IP devices                             | Any usable port number                                                       |
| pollingInterval | Int    | The interval in which the control unit is polled in milliseconds for new statuses, default is 100 ms | Any integer value                                                            |


## Channels

In this binding there are no pre-defined channel, and it's up to the user to define the used  based on the used resources.
Each item must have a defined channel with the following syntax.

| item          | definition                                                               |
|---------------|--------------------------------------------------------------------------|
| Switch        | [read="Type:Addr:Bit", write="Type:Addr:Bit", onValue=1/0, offValue=1/0] |
| Contact       | [read="Type:Addr:Bit"]                                                   |
| Number        | [read="Type:Addr"]                                                       |
| Rollershutter | [read="Type:Addr:Bit, up="Type:Addr:Bit", down="Type:Addr:Bit"]          |
| Dimmer        | [read="Type:Addr"]                                                       |

### Switch

**read**: Defines the input/output or virtual variables that represents the status of the switch, e.g. read="V:10:1"<br/>
**write**: Defines the virtual variable used to modify the switch status (only virtual variables can be used to write values from openhab to the sinthesi system. e.g. write="V:200:3"<br/>
**onValue**: The value sent when an ON command is received<br/>
**offValue**: The value sent when an OFF command is received<br/>

**Note**: At this moment the only supported value for the switch onValue and offValue is 1 (other value will be added in a future update) on both the on and off value, it's suggested that the trigger variable gets set by the binding and then is set back to 0 by the control unit with a dedicated macro (shown below)

### Contact

**read**: Defines the input/output or virtual variables that represents the status of the switch, e.g. read="V:10:1"<br/>

### Number

**read**: Defines the input/output or virtual variables that represents the status of the switch, e.g. read="V:10"<br/>

### Rollershutter

**read**: Defines the input/output or virtual variables that represents the status of the switch, e.g. read="V:10:1"<br/>
**up**: Defines the virtual variable used to open the rollershutter (only virtual variables can be used to write values from openhab to the sinthesi system.) e.g. up="V:200:3"<br/>
**down**: Defines the virtual variable used to close the rollershutter (only virtual variables can be used to write values from openhab to the sinthesi system.) e.g. up="V:200:3"<br/>
**onValue**: The value sent when an ON command is received<br/>
**offValue**: The value sent when an OFF command is received<br/>

### Dimmer

**read**: Defines the input/output or virtual variables that represents the status of the switch, e.g. read="V:10"<br/>

## Full Example

### Thing

In this file we define both the thing we use and the channel the thing uses.

```
sinthesi:pnmas:1 [ ip="192.168.123.137", port="7001", pollInterval=100 ] {
    Channels:
        Switch : sw1 [read="O:1:1", write="V:1000:1", onValue=1, offValue=1]
        Contact : ct1 [read="O:2:1"]
        Dimmer : dm1 [read="V:10"]
        Number : nb1 [read="V:30"]
        Rollershutter : rs1 [read="V:40:1", up="V:1000:1", down="V:1000:2"]
}
```

### Items

In this file we define the items we will use to build our page.

```
Switch switch1  "Switch 1"  {channel="sinthesi:pnmas:1:sw1"}
Contact contact1 "Contact 1" {channel="sinthesi:pnmas:1:ct1"}
Dimmer dimmer1 "Dimmer 1" {channel="sinthesi:pnmas:1:dm1"}
Number temp1 "Temp 1[%d]" {channel="sinthesi:pnmas:1:nb1"}
Rollershutter roller1 "Roller" {channel="sinthesi:pnmas:1:rs1"}
```

## Pns macros

Macro used in the pns to support the openhab switch.

| arg            | description                                                    |
|----------------|----------------------------------------------------------------|
| p_virtualePC   | Virtual variable used by an openhab switch item                |
| p_virtualeMemo | Memory virtual variable used to save the state of p_virtualePC |

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
