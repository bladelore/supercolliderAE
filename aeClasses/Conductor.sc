Conductor {
    var <advance, <name, <clock, <quant, loopIndex, charIndex;
    var currentLabel, skipSectionBool, targetSection, cleanupFunc;

    *new { |name, clock|
        var advance = Condition.new;
        advance.test = false;
        
        ^super.newCopyArgs(advance, name ?? "Conductor", clock ?? TempoClock.default).init;
    }

    init {
        loopIndex = 0;
        charIndex = 0;
        quant = 0;
        skipSectionBool = false;
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

    targetSection_ { |sectionName|
        if (sectionName.isNil) {
            targetSection = nil;
            skipSectionBool = false;
        } {
            targetSection = sectionName.asSymbol;
            skipSectionBool = true;
            ("Skipping to: " ++ sectionName).postln;
        };
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
    listen { |config|
        var channel;
        
        channel = config[\chan] ?? 0;

        this.clearListeners;

        switch(config[\type],
            \midiNote, {
                var key = ("midiNote_" ++ config[\note]).asSymbol;
                MIDIdef.noteOn(key, { |vel, num|
                    if (num == config[\note]) { this.nextFunc; };
                }, chan: channel).permanent_(true);

                cleanupFunc = { MIDIdef(key).free };
            },
            \midiCC, {
                var key = ("midiCC_" ++ config[\cc]).asSymbol;
                MIDIdef.cc(key, { |val|
                    if (val == 127) { this.nextFunc; };
                }, config[\cc], chan: channel).permanent_(true);

                cleanupFunc = { MIDIdef(key).free };
            },
            \modality, {
                config[\device].elAt(config[\key], config[\button]).action = { |el|
                    if (el.value == 1) { this.nextFunc; };
                };

                cleanupFunc = {
                    config[\device].elAt(config[\key], config[\button]).action = nil;
                };
            }
        );
    }

    clearListeners {
        cleanupFunc !? { cleanupFunc.value };
        cleanupFunc = nil;
    }
    
    //setters
    name_ { |argName| name = argName; }
    
    quant_ { |argQuant| quant = argQuant; }
    
    clock_ { |argClock| clock = argClock; }

    debug { |caller|
        var header = caller ?? this.class.name;
        
        postf("\n=== % ===\n", header);
        this.slotsDo { |name, val, idx|
            postf("  %: %: %\n", idx, name, val);
        };
    }
}