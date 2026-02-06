(
    ~new_advance = {
            ~advance = Condition.new;
            ~advance.test = false;

            k.elAt(\tr, [\fwd]).do{|sl| 
                sl.action = {|el|
                    if (el.value == 1) {
                        ~advance.test = true;
                        ~advance.signal;
                        ~advance.test = false;
                        "next".postln;
                    };
                };
            };
        };
)

65.asAscii

MIDIIn.connectAll

Conductor

(
    ProtoDef(\conductor).clear;

    ProtoDef(\conductor){
            ~init =  {
                arg self;
                self.advance = Condition.new;
                self.advance.test = false;
                self.type = Nil;
                self.name = "MyComposition";
                self.index = 0;
            };

            ~label = {
                arg self, message;
                var index, auto;
                index = (self.index + 65).asAscii;
                auto = self.name ++ " -- section: %".format(index);
                message = message ?? auto;
                message.postln;
                self.index = self.index + 1;
            };

            ~nextFunc = {
                arg self;
                self.advance.test = true;
                self.advance.signal;
                self.advance.test = false;
            };

            ~modalityFunc = {
                |self, modalityDevice, deviceKey, button|
                self.type = "modality";
                modalityDevice.elAt(deviceKey, [button]).do{|sl| 
                    sl.action = {|el| 
                        if (el.value == 1) { self.nextFunc.();};
                    };
                };
            };

            ~midiNoteFunc = {|self, key, midiNote|
                self.type = "midiNote";
                MIDIdef.noteOn(key, { |vel, num|
                    if(num == midiNote){ self.nextFunc.(); };
                });
            };

            ~midiCCFunc = {
                |self, key, midiCC|
                self.type = "midiCC";
                MIDIdef.cc(key, { |val|
                    if(val == 127) { self.nextFunc.(); };
                }, midiCC);
            };

            ~printData = { |self|
                "advance: %".format(self.advance).postln;
                "state: %".format(self.advance.test).postln;
                "song: %".format(self.name).postln;
                "type: %".format(self.type).postln;
            };

            ~wait = {|self|
                self.advance.wait;
            };

            ~title = {
                |self, name| self.name = name
            };
    };

    a = Nil;
    a = Prot(\conductor);
    // a.modalityFunc(k, \tr, \stop);
    a.modalityFunc(k, \tr, \fwd);
    a.midiCCFunc('hello', 0);

    x = {
        a.title("Witch");

        a.label;
        "yep".postln;
        a.wait;
        a.label;
        "nope".postln;
        a.wait;
    }.fork(t);
)

(

    a = Conductor.new("Witch");
    MIDIIn.connectAll;
    a.midiCCFunc(0);
    a.modalityFunc(k, \tr, \fwd);

    x = {
        a.label;
        "yep".postln;
        a.wait;

        a.label;
        "nope".postln;
        a.wait;

        a.label("Custom label");
        "nope".postln;
        a.wait;
    }.fork(t);

    a.getData
)

k.gui

modality

MIDIdef.all

MIDIdef.freeAll;

k.elAt(\sl, 0).action = { arg el; el.value.postln; }
k.elAt(\sl, 0).action = Nil