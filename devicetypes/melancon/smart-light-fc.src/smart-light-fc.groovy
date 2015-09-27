/**
*  Smart Light - FC
*
*  Copyright 2015 Michael Melancon
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*/
metadata {
	definition (name: "Smart Light - FC", namespace: "melancon", author: "Michael Melancon") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Color Control"
        capability "Color Temperature"

		command "sync"
		command "resetColor"
      
        // color control mode capability
        command "setColorControlMode", ["string"]
        attribute "colorControlMode", "enum", ["color control", "color temperature control"]
	}
 	tiles(scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.multi-light-bulb-on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.multi-light-bulb-off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.multi-light-bulb-on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.multi-light-bulb-off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}

        standardTile("sync", "device.level", width: 2, height: 2, decoration: "flat", inactiveLabel: false) {
            state "default", label:"Sync", action:"sync", icon:"st.secondary.refresh-icon"
        }
    }

	main(["switch"])

	details( ["switch", "sync"])

	preferences()
}

def parse(description) {
}

def on() {
	log.debug "Requesting 'on'"
	sendEvent(name: "switch", value: "on")
	parent.on(this)
}

def off() {
	log.debug "Requesting 'off'"
	sendEvent(name: "switch", value: "off")
	parent.off(this)
}

def setLevel(percent) {
	log.debug "Requesting 'setLevel($percent)'"
	sendEvent(name: "level", value: percent)
	parent.setLevel(this, percent)
}

def setColorTemperature(mirek) {
	log.debug "Requesting 'setColorTemperature($mirek)'"
	sendEvent(name: "colorTemperature", value: mirek)
	parent.setColorTemperature(this, mirek)
}

def setSaturation(percent) {
	log.debug "Requesting 'setSaturation($percent)'"
	sendEvent(name: "saturation", value: percent)
	parent.setSaturation(this, percent)
}

def setHue(percent) {
	log.debug "Requesting 'setHue($percent)'"
	sendEvent(name: "hue", value: percent)
	parent.setHue(this, percent)
}

def setColor(color) {
	color.remove("level")
	log.debug "Requesting 'setColor($color)'"
    // this does not seem right it breaks the capability contract, but it's the only way the color controlTile will function properly.
    // the following line should be: sendEvent(name: "color", value: color)
	sendEvent(name: "color", value: color.hex)
    if (color.hue) sendEvent(name: "hue", value: color.hue)
    if (color.saturation) sendEvent(name: "saturation", value: color.saturation)
    parent.setColor(this, color)
}

def resetColor() {
	log.debug "Executing 'resetColor'"
	setColor([saturation: 0, hue: 0, hex: '#ffffff'])
}

def sync() {
	log.debug "Executing 'sync'"
}
