Conductor {
    var <advance, <type, <name, <index;
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
    
    modalityFunc { |modalityDevice, deviceKey, button|
        type = "modality";
        modalityDevice.elAt(deviceKey, [button]).do { |sl|
            sl.action = { |el|
                if (el.value == 1) { this.nextFunc; };
            };
        };
    }
    
    midiNoteFunc { |key, midiNote|
        type = "midiNote";
        midiDefKey = key;
        MIDIdef.noteOn(key, { |vel, num|
            if (num == midiNote) { this.nextFunc; };
        });
    }
    
    midiCCFunc { |key, midiCC|
        type = "midiCC";
        midiDefKey = key;
        MIDIdef.cc(key, { |val|
            if (val == 127) { this.nextFunc; };
        }, midiCC);
    }
    
    getData {
        "advance: %".format(advance).postln;
        "state: %".format(advance.test).postln;
        "name: %".format(name).postln;
        "type: %".format(type).postln;
    }
    
    wait {
        advance.wait;
    }
    
    name_ { |argName|
        name = argName;
    }
}
