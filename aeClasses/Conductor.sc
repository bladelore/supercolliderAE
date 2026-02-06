Conductor {
    var <advance, <type, <name, <index;
    var modalityDevice, deviceKey, modalityButton;
    var midiDefKey;
    
    *new { |name|
        ^super.new.init(name);
    }
    
    init { |argName|
        advance = Condition.new;
        advance.test = false;
        type = nil;
        name = argName ?? "MyComposition";
        index = 0;
    }
    
    label { |message|
        var indexChar;
        indexChar = (index + 65).asAscii;
        message = message ?? indexChar;
        message = name ++ " -- section: %".format(message);
        message.postln;
        index = index + 1;
    }
    
    nextFunc {
        advance.test = true;
        advance.signal;
        advance.test = false;
    }
    
    clearListeners {
        if (type == "modality" and: { modalityDevice.notNil }) {
            modalityDevice.elAt(deviceKey, modalityButton).action = nil;
        };
        if (midiDefKey.notNil) {
            MIDIdef(midiDefKey).free;
            midiDefKey = nil;
        };
    }
    
    modalityListener { |argModalityDevice, argDeviceKey, button|
        this.clearListeners;
        type = "modality";
        modalityDevice = argModalityDevice;
        deviceKey = argDeviceKey;
        modalityButton = button;
        modalityDevice.elAt(deviceKey, button).action = { |el|
            if (el.value == 1) { this.nextFunc; };
        };
    }
        
    midiNoteListener { |midiNote|
        this.clearListeners;
        type = "midiNote";
        midiDefKey = (name ++ "_midiNote_" ++ midiNote).asSymbol;
        
        MIDIdef.noteOn(midiDefKey, { |vel, num|
            if (num == midiNote) { this.nextFunc; };
        });
    }
    
    midiCCListener { |midiCC|
        this.clearListeners;
        type = "midiCC";
        midiDefKey = (name ++ "_midiCC_" ++ midiCC).asSymbol;
        
        MIDIdef.cc(midiDefKey, { |val|
            if (val == 127) { this.nextFunc; };
        }, midiCC);
    }
    
    wait {
        advance.wait;
    }
    
    name_ { |argName|
        name = argName;
    }
    
    free {
        this.clearListeners;
    }

    getData {
        "advance: %".format(advance).postln;
        "state: %".format(advance.test).postln;
        "name: %".format(name).postln;
        "type: %".format(type).postln;
    }
}