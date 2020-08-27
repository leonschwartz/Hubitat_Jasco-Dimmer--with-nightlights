/**
 *  Copyright 2020 Leon Schwartz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Based on code from Davin Dameron and Jason Bottjen.
 */
 
metadata {
	definition (name: "Jasco Z-Wave Plus Dimmer and Nightlight (LS)", namespace: "octadox", author: "Leon Schwartz") {
		capability "Actuator"
		capability "PushableButton"
		//capability "DoubleTapableButton"
		capability "Configuration"
		capability "Refresh"
		capability "Polling"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Light"
		
		//Commands (functions) that the dimmer can handle
		command "toggleNightlight"
        command "turnOffNightlight"
        command "turnOnNightlight"
        command "low"
		command "medium"
		command "high"
		command "levelUp"
		command "levelDown"
		
		fingerprint mfr:"0063", prod:"4944", model:"3038", ver: "5.26", deviceJoinName: "GE Z-Wave Plus Wall Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3038", ver: "5.27", deviceJoinName: "GE Z-Wave Plus Wall Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3038", ver: "5.28", deviceJoinName: "GE Z-Wave Plus Wall Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3038", ver: "5.29", deviceJoinName: "GE Z-Wave Plus Wall Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3039", ver: "5.19", deviceJoinName: "GE Z-Wave Plus 1000W Wall Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3130", ver: "5.21", deviceJoinName: "GE Z-Wave Plus Toggle Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3135", ver: "5.26", deviceJoinName: "Jasco Z-Wave Plus Wall Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3136", ver: "5.21", deviceJoinName: "Jasco Z-Wave Plus 1000W Wall Dimmer"
		fingerprint mfr:"0063", prod:"4944", model:"3137", ver: "5.20", deviceJoinName: "Jasco Z-Wave Plus Toggle Dimmer"
		fingerprint mfr:"0039", prod:"4944", model:"3235", ver: "5.54", deviceJoinName: "Honeywell Z-Wave Plus Dimmer"
	}

	//Setup preferences for switch/dimmer
	preferences {
		input(type: "paragraph", element: "paragraph", title: "Dimmer General Settings", description: "")
		input("inverted", "enum", title: "Dimmer Buttons Direction", multiple: false, options: ["0" : "Normal (default)", "1" : "Inverted"], required: false, displayDuringSetup: true)
		input("indicator", "enum", options: [
			"0": "When Off",
			"1": "When On",
			"3": "Always On",
			"2": "Never On"], title: "Indicator", defaultValue:"0",required:true, displayDuringSetup: true)
		input("switchMode", "enum", options: [
			"0": "Dimmer",
			"1": "Switch"], title: "Switch Mode", defaultVale:"0",required:true, displayDuringSetup: true)
		input(type: "paragraph", element: "paragraph", title: "Dimmer Nightlight Settings", description: "")
		input("nightlightDimLevel", "number", title: "Dim level to use as nightlight (%):", required: false, displayDuringSetup: true, defaultValue: 2)
		input(type: "paragraph", element: "paragraph", title: "Dimmer Timing Settings", description: "")
		input(name: "zwaveSteps", type: "number", range: "1..99", title: "zWave Dim Steps (1-99)", displayDuringSetup:true, required:true)
		input(name: "zwaveDelay", type: "number", range: "1..255", title: "zWave Delay (1-255)", displayDuringSetup:true, required:true)
		input(name: "manualSteps", type: "number", range: "1..99", title: "Manual Dim Steps (1-99)", displayDuringSetup:true, required:true)
		input(name: "manualDelay", type: "number", range: "1..255", title: "Manual Delay (1-255)", displayDuringSetup:true, required:true)
		input(name: "allSteps", type: "number", range: "1..99", title: "All Dim Steps (1-99)", displayDuringSetup:true, required:true)
		input(name: "allDelay", type: "number", range: "1..255", title: "All Delay (1-255)", displayDuringSetup:true, required:true)
		input(name: "minimumDim", type: "number", range: "1..99", title: "Minimum dimmer setting (default is 1)", displayDuringSetup:true, required:false, defaultValue: 1)
		input(name: "maximumDim", type: "number", range: "1..100", title: "Maximum dimmer setting (default is 100)", displayDuringSetup:true, required:false, defaultValue: 100)
		input(name: "presetLevel", type: "number", range: "1..100", title: "Always turn on to this (default is 100)", displayDuringSetup:true, required:false, defaultValue: 100)
		input(name: "allowZwaveRamp", type: "enum", options: [
			"0" : "False",
			"1" : "True"], title: "Allow ramp delays in zWave?", displayDuringSetup:true, required: false)
		input(type: "paragraph", element: "paragraph", title: "", description: "Logging")
		input(name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false)	
		input(name: "logDesc", type: "bool", title: "Enable descriptionText logging", defaultValue: true)	 
   	}
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Parse
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
def parse(String description) {
    def result = null
	if (description != "updated") {
		if (logEnable) log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x56: 1, 0x70: 2, 0x72: 2, 0x85: 2])

		if (logEnable) log.debug "cmd: $cmd"
		
		if (cmd) {
			result = zwaveEvent(cmd)
        }
	}
    if (!result) { if (logEnable) log.debug "Parse returned ${result} for $description" }
    else {if (logEnable) log.debug "Parse returned ${result}"}
	
	return result
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Z-Wave Messages
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
def zwaveEvent(hubitat.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	if (logEnable) log.debug "zwaveEvent(): CRC-16 Encapsulation Command received: ${cmd}"
	
	def newVersion = 1
	
	// SwitchMultilevel = 38 decimal
	// Configuration = 112 decimal
	// Manufacturer Specific = 114 decimal
	// Association = 133 decimal
	if (cmd.commandClass == 38) {newVersion = 3}
	if (cmd.commandClass == 112) {newVersion = 2}
	if (cmd.commandClass == 114) {newVersion = 2}								 
	if (cmd.commandClass == 133) {newVersion = 2}		
	
	def encapsulatedCommand = zwave.getCommand(cmd.commandClass, cmd.command, cmd.data, newVersion)
	if (encapsulatedCommand) {
       zwaveEvent(encapsulatedCommand)
   } else {
       log.warn "Unable to extract CRC16 command from ${cmd}"
   }
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    if (logEnable) log.debug "---BASIC REPORT V1--- ${device.displayName} sent ${cmd}"
	if (logEnable) log.debug "This report does nothing in this driver, and shouldn't have been called..."
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    if (logEnable) log.debug "---BASIC SET V1--- ${device.displayName} sent ${cmd}"
	def result = []
	
/*	if (cmd.value == 255) {
		if (logEnable) log.debug "Double Up Triggered"
		if (logDesc) log.info "$device.displayName had Doubletap up (button 1) [physical]"
		result << createEvent([name: "doubleTapped", value: 1, descriptionText: "$device.displayName had Doubletap up (button 1) [physical]", type: "physical", isStateChange: true])
    }
	else if (cmd.value == 0) {
		if (logEnable) log.debug "Double Down Triggered"
		if (logDesc) log.info "$device.displayName had Doubletap down (button 2) [physical]"
		result << createEvent([name: "doubleTapped", value: 2, descriptionText: "$device.displayName had Doubletap down (button 2) [physical]", type: "physical", isStateChange: true])
    }
*/
    return result
}

def zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    if (logEnable) log.debug "---ASSOCIATION REPORT V2--- ${device.displayName} sent groupingIdentifier: ${cmd.groupingIdentifier} maxNodesSupported: ${cmd.maxNodesSupported} nodeId: ${cmd.nodeId} reportsToFollow: ${cmd.reportsToFollow}"
/*    if (cmd.groupingIdentifier == 3) {
    	if (cmd.nodeId.contains(zwaveHubNodeId)) {
        	sendEvent(name: "numberOfButtons", value: 2, displayed: false)
        }
        else {
        	sendEvent(name: "numberOfButtons", value: 0, displayed: false)
			zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
			zwave.associationV2.associationGet(groupingIdentifier: 3).format()
        }
    }
*/
}


def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
	if (logEnable) log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} sent ${cmd}"
    	def config = cmd.scaledConfigurationValue.toInteger()
    	def result = []
	def name = ""
    	def value = ""
    	def reportValue = cmd.configurationValue[0]
    	switch (cmd.parameterNumber) {
    		case 3:
    			name = "indicator"
    	        	value = reportValue == 1 ? "when on" : reportValue == 2 ? "never" : reportValue == 3 ? "always" : "when off"
            	break
    	    	case 4:
    	    	    	name = "inverted"
    	    	    	value = reportValue == 1 ? "true" : "false"
            	break
            	case 6:
			name = "allowZwaveRamp"
			value = reportValue == 1 ? "true" : "false"
		break
		case 7:
			name = "zwaveSteps"
			value = reportValue
		break
		case 8:
			name = "zwaveDelay"
			value = reportValue
		break
		case 9:
			name = "manualSteps"
			value = reportValue
		break
		case 10:
			name = "manualDelay"
			value = reportValue
		break
		case 11:
			name = "allSteps"
			value = reportValue
		break
		case 12:
			name = "allDelay"
			value = reportValue
		break
		case 16:
			name = "switchMode"
			value = reportValue == 1 ? "switch" : "dimmer"
		break
		case 30:
			name = "minimumDim"
			value = reportValue
		break
		case 31:
			name = "maximumDim"
			value = reportValue
		break
		case 32:
			name = "presetLevel"
			value = reportValue
		break
	    	default:
	        break
    	}
	result << createEvent([name: name, value: value, displayed: false])
	return result
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	if (cmd.value == 0) {
		switchEvent(false)
	} else if (cmd.value == 255) {
		switchEvent(true)
	} else {
		log.debug "Bad switch value $cmd.value"
	}
}

def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "---MANUFACTURER SPECIFIC REPORT V2--- ${device.displayName} sent ${cmd}"
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
    state.manufacturer=cmd.manufacturerName
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)	
    sendEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (logEnable) log.debug "---VERSION REPORT V1--- ${device.displayName} is running firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(hubitat.zwave.commands.hailv1.Hail cmd) {
	if (logEnable) log.debug "Hail command received..."
	if (logEnable) log.debug "This does nothing in this driver, and shouldn't have been called..."
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	dimmerEvent(cmd.value)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
	dimmerEvent(cmd.value)
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    log.warn "${device.displayName} received unhandled command: ${cmd}"
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Driver Commands / Functions
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

private static int getCommandDelayMs() { 1000 }
private static int getDefaultLevelIncrement() { 10 }

private dimmerEvent (short level)
{
    if (logEnable) log.debug "---SwitchMultilevelReport V3---  ${device.displayName} sent ${level}"
    if (logEnable) log.debug "And state for  ${device.displayName} is ${state.level}"
	def result = null
	def newType
	
    if (level != state.level)
    {
	// check state.bin variable to see if event is digital or physical
	if (state.bin == -1)
	{
		newType = "digital"
	}
	else
	{
		newType = "physical"
	}
	
	// Reset state.bin variable
	state.bin = 0
    if (level == 0) {
			//If we're in night light mode and we're trying to turn off, then go back to nightlight level.
			if (state.localNightlightOn) {
				short dimmingDuration = durationSeconds == null ? 255 : secondsToDuration(durationSeconds as int)
				//Get the nightlight level
				level = toDisplayLevel(nightlightDimLevel as short)

				// Update state.level
				state.level = level

				//Set proper state
				state.isNightlightOn = true
				state.OnButtonName = "night"

				//Create events to update the app.
				//Update the button name and the level to NIGHT
				sendEvent(name: "switch", value: state.OnButtonName, descriptionText: "$device.displayName was turned on [$newType]", type: "$newType", isStateChange: true)
				result = createEvent(name: "level", value: toDisplayLevel(level), unit: "%")

				//Update the switch to go back to NIGHT mode.
				result = [result, response([zwave.switchMultilevelV2.switchMultilevelSet(value: toZwaveLevel(level), dimmingDuration: dimmingDuration).format(), "delay 5000",
					  zwave.switchMultilevelV1.switchMultilevelGet().format()])]
		} else {
			// Update state.level
			state.level = level
			result = [createEvent(name: "level", value: 0, unit: "%"), switchEvent(false)]
		}
	} 
	else if (level > 0 && level <= 100) {
		//If we're trying to turn on and the level is higher than the nightlight level
		if (level > nightlightDimLevel) {
			state.isNightlightOn = false
			state.OnButtonName = "on"
			//Update the button nbame to ON, since we're above night level
			sendEvent(name: "switch", value: state.OnButtonName, descriptionText: "$device.displayName was turned on [$newType]", type: "$newType", isStateChange: true)
		}
		// Update state.level
		state.level = level

		//Encode update event to update level to the right level (regardless of whether we're in night or normal mode)
		result = createEvent(name: "level", value: toDisplayLevel(level), unit: "%")

		//Force a switch check.
		if (device.currentValue("switch") != state.OnButtonName ) {
			// Don't blindly trust level. Explicitly request on/off status.
			result = [result, response(zwave.switchBinaryV1.switchBinaryGet().format())]
		}
	} else {
		log.debug "Bad dimming level $level"
	}
    }
}


//Handle switch events
private switchEvent(boolean on) {
	//Do not allow turn-off events if in nightlight mode, set to nightlight instead.
	if (!on && state.localNightlightOn) {
		state.isNightlightOn = true
		state.OnButtonName = "night"
		setLevel(nightlightDimLevel)	  
	} else {
		createEvent(name: "switch", value: on ? state.OnButtonName : "off")
	}
}

//Turn the switch on.
def on() {
	def fadeOnTime = device.currentValue("zwaveDelay")
	def allowRamp = device.currentValue("allowZwaveRamp")
	//Preset Level does not work on these Jasco dimmers, so this will always result in full-on
	def presetLevel = device.currentValue("presetLevel")
	//If allowRamp is not set then go with fadeOnTime, and if that is null, then go with the minimum (1)
	short duration = allowRamp == null || allowRamp == 1 ? fadeOnTime == null ? 1 : fadeOnTime : 0
	log.debug "allowRamp:{$allowRamp}"
	log.debug "fadeonTime:{$duration}"
	short level = presetLevel == null || presetLevel == 0 ? 0xFF : toZwaveLevel(presetLevel as short)
	setLevel(level, duration)
}

//Turn the switch off.
def off() {
	def fadeOffTime = device.currentValue("zwaveDelay")
	def allowRamp = device.currentValue("allowZwaveRamp")
	//If allowRamp is not set then go with fadeOnTime, and if that is null, then go with the minimum (1)
	short duration = allowRamp == null || allowRamp == 1 ? fadeOnTime == null ? 1 : fadeOnTime : 0
	//log.debug "allowRamp:{$allowRamp}"
	//log.debug "fadeonTime:{$duration}"
	//If there's a local nightlight on, then instead of turning off, go to that value.
	if ( state.localNightlightOn ) {
		dimTheLight()
	}
	//Otherwise, turn off.
	else {
		setLevel(0, duration)
	}
}

//Set the dimmer to the level provided
def setLevel(value, durationSeconds) {
	short level = toDisplayLevel(value as short)
	short dimmingDuration = durationSeconds == null ? 1 : secondsToDuration(durationSeconds as int)
    //If we are not turning off
	if (level > 0) {
		//If level is higher than nightlightDimLevel, then do not say NIGHT for the light anymore.
	if (level > nightlightDimLevel) {
		state.isNightlightOn = false
		state.OnButtonName = "on"
	}
    //Update the display in the app (Night/On)
	sendEvent(name: "switch", value: state.OnButtonName)	  	
	} else {
		//Update display in the app (Off)
	sendEvent(name: "switch", value: "off")
	}
	//Send the event for the level we are going to, as well.
	sendEvent(name: "level", value: level, unit: "%")
	//dimmingDuration = 1
	//log.debug "LLLLLL>>dimming dur: $dimmingDuration"
	//Send the event to the device, as well.
	delayBetween([zwave.switchMultilevelV2.switchMultilevelSet(value: toZwaveLevel(level), dimmingDuration: dimmingDuration).format(),
			   zwave.switchMultilevelV1.switchMultilevelGet().format()], durationToSeconds(dimmingDuration) * 1000 + getCommandDelayMs())
}

def setLevel(value) {
	if (logEnable) log.debug "setLevel($value)"
	setLevel(value, 0)
}

def refresh() {
	log.info "refresh() is called"
	
	def cmds = []
	// Get current config parameter values
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 3).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 4).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 6).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 7).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 8).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 9).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 10).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 11).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 12).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 30).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 31).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 32).format()

	if (getDataValue("MSR") == null) {
		cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(cmds,500)
}

def installed() {
	configure()
}

def updated() {
    log.info "updated..."
    log.info "debug logging is: ${logEnable == true}"
    log.info "description logging is: ${txtEnable == true}"
    if (logEnable) runIn(1800,logsOff)

    //sendEvent(name: "numberOfButtons", value: 2)
	
    if (state.lastUpdated && now() <= state.lastUpdated + 3000) return
    state.lastUpdated = now()

    def cmds = []
    def nodes = []
	
	// Set defaults for null
	if (indicator==null) {
		indicator = 0
	}	
	if (inverted==null) {
		inverted = 0
	}
	if (switchMode==null) {
		switchMode = 0
	}
	if (allowZwaveRamp==null) {
		allowZwaveRamp = 0
	}
	
	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: indicator.toInteger(), parameterNumber: 3, size: 1).format())
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	
	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: inverted.toInteger(), parameterNumber: 4, size: 1).format())
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
	
	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: allowZwaveRamp.toInteger(), parameterNumber: 6, size: 1).format())
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 6).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: zwaveSteps, parameterNumber: 7, size: 1))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: zwaveDelay, parameterNumber: 8, size: 2))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: manualSteps, parameterNumber: 9, size: 1))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: manualDelay, parameterNumber: 10, size: 2))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: allSteps, parameterNumber: 11, size: 1))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 11).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: allDelay, parameterNumber: 12, size: 2))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: switchMode.toInteger(), parameterNumber: 16, size: 1).format())
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 16).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: minimumDim, parameterNumber: 30, size: 1))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 30).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: maximumDim, parameterNumber: 31, size: 1))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 31).format()

	cmds << secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: presetLevel, parameterNumber: 32, size: 1))
//	cmds << zwave.configurationV1.configurationGet(parameterNumber: 32).format()

	delayBetween(cmds, 1000)
}

def configure() {
    log.info "configure triggered"
    state.bin = -1
    if (state.level == "") {state.level = 99}
	def cmds = []
	// Get current config parameter values
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 3).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 4).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 6).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 7).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 8).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 9).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 10).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 11).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 12).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 16).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 30).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 31).format()
	cmds << zwave.configurationV2.configurationGet(parameterNumber: 32).format()
	delayBetween(cmds,500)
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def low() {
	setLevel(10)
}

def medium() {
	setLevel(50)
}

def high() {
	setLevel(100)
}

def levelUp() {
	setLevel(device.currentValue("level") + (levelIncrement ?: defaultLevelIncrement))
}

def levelDown() {
	setLevel(device.currentValue("level") - (levelIncrement ?: defaultLevelIncrement))
}

private short toDisplayLevel(short level) {
	level = Math.max(0, Math.min(100, level))
	(level == (short) 99) ? 100 : level
}

private short toZwaveLevel(short level) {
	Math.max(0, Math.min(99, level))
}

private int durationToSeconds(short duration) {
	if (duration >= 0 && duration <= 127) {
		duration
	} else if (duration >= 128 && duration <= 254) {
		(duration - 127) * 60
	} else if (duration == 255) {
		2   // factory default
	} else {
		log.error "Bad duration $duration"
		0
	}
}

private short secondsToDuration(int seconds) {
	if (seconds >= 0 && seconds <= 127) {
		seconds
	} else if (seconds >= 128 && seconds <= 127 * 60) {
		127 + Math.round(seconds / 60)
	} else {
		log.error "Bad seconds $seconds"
		255
	}
}

def nightlightDim()
{
	state.isNightlightOn = true
	//If already on, do not touch for now, when turned off, it will do it's thing.
	if (device.currentValue("switch") != "on")
	{
		localNightlightDim()
	}
}

def turnOnNightlight()
{
    localNightlightDim()
}

def toggleNightlight()
{
    if (state.isNightlightOn)
    {
        turnOffNightlight()
    }
    else {
        localNightlightDim()
    }
}
def localNightlightDim()
{
	state.isNightlightOn = true
	state.localNightlightOn = true
	if (device.currentValue("switch") != "on") {
		dimTheLight()
	}
}

def dimTheLight()
{
	state.OnButtonName = "night"
	setLevel(nightlightDimLevel)
}

def turnOffNightlight() {
	//If switch is higher than nightlight, don't turn it off. NOTE: THIS DOES NOT WORK.
	if ( state.isNightlightOn) {
		localResetLevel()
		setLevel(0)
	} else {
		localResetLevel()
	}
}

def localResetLevel()
{
	//no need to dim up and then down for this type of switch, so just turn it off instead of this: resetLevel(99)
	state.isNightlightOn = false
	state.isNightlightLimit = false
	state.localNightlightOn = false
	state.OnButtonName = "on"
	setLevel(0)
/*if ( device.currentValue("level") < 95 )
	{
	delayBetween ([
		delayBetween ([	zwave.basicV1.basicSet(value: 99).format(),
					zwave.basicV1.basicSet(value: 0x00).format()], 1),	
		zwave.switchMultilevelV1.switchMultilevelGet().format()], 15)
	}
*/
}
def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

String secureCmd(cmd) {
    if (getDataValue("zwaveSecurePairingComplete") == "true" && getDataValue("S2") == null) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
		return secure(cmd)
    }	
}

String secure(String cmd){
    return zwaveSecureEncap(cmd)
}

String secure(hubitat.zwave.Command cmd){
    return zwaveSecureEncap(cmd)
}
