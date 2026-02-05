// midiMsgType: \noteOn
MIDIIn.connectAll; MIDIFunc.trace;
(
~descOutput = (
    idInfo: ~idInfo, // still around from above
    protocol: \midi,
    elementsDesc: (
        elements: [
            (
                key: 'bt',
                type: 'button',
                midiMsgType: \noteOn,
                midiChan: 0,
                midiNum: 2,
                spec: \midiVel,
                ioType: \inout
            )
        ]
    )
)
);

m.device.midiOut.dump
m.device.destination

m = MKtl( \testMIDI, ~descOutput );
m.rebuild( ~descOutput ); // updating it

// turn on tracing of midi input, so we see incoming messages
MIDIFunc.trace

m.elAt( \bt ).value_( 0 ); // see note above on noteOn message with velocity zero; may show up in the trace as a noteOff
m.elAt( \bt ).value_( 1 );

m.gui; // and press the button for it


(
    ~descOutput = (
        idInfo: ~idInfo, // still around from above
        protocol: \virtual,
        elementsDesc: (
            elements: (0..20).collect { |ccNum|
                (
                    key: "cc" ++ ccNum,
                    type: 'control',
                    midiMsgType: \control,
                    midiChan: 0,
                    midiNum: ccNum,
                    spec: \midiCC,
                    ioType: \inout
                )
            }
        )
    )
);
    
m = MKtl(\testMIDI, ~descOutput);
m.rebuild(~descOutput); // updating it

// turn on tracing of midi input, so we see incoming messages
MIDIFunc.trace;