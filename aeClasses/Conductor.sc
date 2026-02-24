Conductor {
    var <advance, <name, <clock, <quant, loopIndex, charIndex;
    var listenerType, modalityDevice, deviceKey, modalityButton, midiDefKey, currentLabel, targetSection, skipSectionBool;
    
    *new { |argName, argClock|
        var newAdvance = Condition.new;
        newAdvance.test = false;
        
        ^super.newCopyArgs(
            newAdvance,                      // advance
            argName ?? "Conductor",          // name
            argClock ?? TempoClock.default,  // clock
            0,                               // quant
            0,                               // loopIndex
            0,                               // charIndex
        );
    }

    label { |section|
        var autoLabel = (charIndex + 97).asAscii.asString ++ loopIndex.asString;
        section = section ?? autoLabel;
        currentLabel = section.asSymbol;
        
        (name ++ " -- section: " ++ section).postln;

        charIndex = (charIndex + 1) % 26;
        if (charIndex == 0) { loopIndex = loopIndex + 1 };
    }
    
    nextFunc {
        var signalVars = {
            advance.test = true;
            advance.signal;
            advance.test = false;
        };
        
        if (quant > 0) { 
            clock.schedAbs(clock.nextTimeOnGrid(quant), signalVars);
        } { 
            signalVars.value;
        };
    }

    wait {
        if (skipSectionBool) {
            if (currentLabel == targetSection) {
                skipSectionBool = false;
                targetSection = nil;
                advance.wait;
            } {
                "Skipping section: %".format(currentLabel).postln;
            };
        } {
            advance.wait;
        };
    }

    skipTo { |sectionName|
        targetSection = sectionName.asSymbol;
        skipSectionBool = true;
    }

    rampTempo { |targetTempo, dur = 4, curve = \lin, step = 0.01|
        var current = clock.tempo;
        var numSteps = ((targetTempo - current) / step).abs.ceil;
        var stepDur = dur / numSteps;
        var env = Env([current, targetTempo], [1], curve);
        
        fork {
            numSteps.do { |i|
                var progress = i / (numSteps - 1);
                clock.tempo = env.at(progress);
                stepDur.wait;
            };
            clock.tempo = targetTempo;
        }.play(SystemClock);
    }

    //Listeners
    clearListeners {
        if (listenerType == "modality" && modalityDevice.notNil) {
            modalityDevice.elAt(deviceKey, modalityButton).action = nil;
        };
        if ((listenerType == "midiNote" || listenerType == "midiCC") && midiDefKey.notNil) {
            MIDIdef(midiDefKey).free;
            midiDefKey = nil;
        };
    }
    
    modalityListener { |argModalityDevice, argDeviceKey, button|
        this.clearListeners;
        listenerType = "modality";
        modalityDevice = argModalityDevice;
        deviceKey = argDeviceKey;
        modalityButton = button;
        modalityDevice.elAt(deviceKey, button).action = { |el|
            if (el.value == 1) { this.nextFunc; };
        };
    }
    
    midiNoteListener { |midiNote|
        this.clearListeners;
        listenerType = "midiNote";
        midiDefKey = ("midiNote_" ++ midiNote).asSymbol;
        MIDIdef.noteOn(midiDefKey, { |vel, num|
            if (num == midiNote) { this.nextFunc; };
        }).permanent_(true);
    }
    
    midiCCListener { |midiCC|
        this.clearListeners;
        listenerType = "midiCC";
        midiDefKey = ("midiCC_" ++ midiCC).asSymbol;
        MIDIdef.cc(midiDefKey, { |val|
            if (val == 127) { this.nextFunc; };
        }, midiCC).permanent_(true);
    }
    
    //setters
    name_ { |argName| name = argName; }
    
    quant_ { |argQuant| quant = argQuant; }
    
    clock_ { |argClock| clock = argClock; }

    debug { |caller|
        var header = caller ?? this.class.name;
        
        postf("\n=== % ===\n", header);
        this.slotsDo { |name, val, idx|
            postf("  % [%]: %\n", name, idx, val);
        };
    }
}