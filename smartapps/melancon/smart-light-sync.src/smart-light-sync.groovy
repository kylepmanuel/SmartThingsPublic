/**
 *  Smart Light Sync
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
definition(
    name: "Smart Light Sync",
    namespace: "melancon",
    author: "Michael Melancon",
    description: "Keeps a group of smart bulbs and optionally a smart switch synchronized.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select the smart bulbs that should be kept in sync.") {
		input "smartBulbs", "capability.switchLevel", title: "Choose the smart bulbs", multiple: true, required: true        
	}
    section("Select the smart switch that controls power to the bulbs.") {
		input "smartSwitch", "capability.switch", title: "Choose the smart switch", multiple: false, required: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	if (smartSwitch) {
		subscribe(smartSwitch, "switch", powerHandler);
    }
	subscribe(smartBulbs, "switch", triggerHandler);    
    subscribe(smartBulbs, "level", triggerHandler);
    subscribe(smartBulbs, "color", triggerHandler);
    subscribe(smartBulbs, "colorTemperature", triggerHandler);
}

def powerHandler(evt) {
	if (evt.device.currentSwitch == "on") {
    	log.debug "${evt.device} powered on; synchronizing to previous state."
        def sourceState = state.sourceState
        sourceState.switch = "on"             
        log.debug "${evt.device} triggered synchronizing from $sourceState."
        sync(null, sourceState)
    }
    else {
    	log.debug "${evt.device} powered off;"
        sync(null, [switch:"off", hue:23, saturation:56, level:100])
    }
}

def triggerHandler(evt) {
	if (smartSwitch.currentSwitch == "on") {
    	def sourceState = getSourceState(evt.device)
    	log.debug "${evt.device} triggered synchronizing from $sourceState."
    	sync(evt.device.id, sourceState)
    }
    else if (evt.value == "on") {
    	smartSwitch.on()        
    }
}

def getSourceState(source) {
    def sourceState = [switch: source.currentSwitch]
    if (source.hasCapability("Color Control")) {
        sourceState.hue = source.currentHue
        sourceState.saturation = source.currentSaturation
    }
    if (source.hasCapability("Color Temperature")) {
        sourceState.colorTemperature = source.currentColorTemperature
    }  
    sourceState.level = source.currentLevel
    sourceState
}
                                                                           
def sync(sourceId, sourceState) {
	if (sourceState.switch == "on") {
    	state.sourceState = sourceState
    }
    smartBulbs.each {
    	if (it.id != sourceId) {            
            if (it.hasCapability("Color Control")) {
                it.setColor(sourceState)
            }
            else {
                if (sourceState.switch == "on") {
                    it.on()
                    it.setLevel(sourceState.level)
                }
                else {
                    it.setLevel(sourceState.level)
                    it.off()
                }
            }
            if (it.hasCapability("Color Temperature") && sourceState.colorTemperature) {
                it.setColorTemperature(sourceState.colorTemperature)
            }
        }
    }
}
