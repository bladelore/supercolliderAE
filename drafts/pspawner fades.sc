(
// fade between two Pdefs
Pspawner({| sp |
    Pdef(\p1).fadeTime = 16;
    'a'.postln;
    a = sp.par(
        Pdef(\p1,
            Pbind(
                \instrument, \default,
                \dur, 1/2
            )
        )
    );

    sp.wait(4);
    Pdef(\p1,
        Pbind(
            \instrument, \default,
            \dur, 1/2,
            \degree, 3
        )
    )
    
    sp.wait(4);
    //can also use pbindef
    Pbindef(\p1, \degree, 3).quant = 0.0;

}).play(t);
)

(
    SynthDef(\driftingSines_mono, {
        var sig;
        var lfo = LFTri.ar(\lfoFreq.kr(0.01), 1).linlin(-1, 1, 0, 1);
        var freq = \freq.kr(404);
        var freqs = ([freq, freq/2, freq/5, freq/7] + LFNoise2.kr(0.1 ! 4, \pitchDev.kr(15)));
        var amps = ([0.4, 0.3, 0.2, 0.5] + LFNoise2.kr(0.1 ! 4, 0.1));
        var q = 0.8;
    
        sig = Blip.ar(freqs, \numHarm.kr(0)) * amps;
        sig = BLowPass4.ar(sig, (5000 * lfo), q);
        sig = Splay.ar(sig);
        sig = LeakDC.ar(sig).sanitize;
    
        sig = sig * 0.25;
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;



Pdef(\player).fadeTime = 4;

(
//fade between two pdefs with pmono (must specify dur key)
Pdef(\player,
    Pspawner({| sp |
        var pat;
        Pdef(\p1).fadeTime = 8;

        'a'.postln;
        pat = sp.par(
            Pdef(\p1,
                Pmono(\driftingSines_mono_test, \dur, 0.1)
            )
        );

        sp.wait(8);
        pat = Pdef(\p1,
            Pmono(\driftingSines_mono_test, \freq, 404, \dur, 0.1)
        )    
    })
    )
).play(t);

(
//fade between two pdefs with pmono (must specify dur key)
Pdef(\player,
    Pspawner({| sp |
        var pat;
        Pdef(\p1).fadeTime = 8;

        'a'.postln;
        pat = sp.par(
            Pdef(\p1,
                Pmono(\driftingSines_mono_test, \dur, 0.1)
            )
        );

        sp.wait(8);
        pat = Pdef(\p1,
            Pmono(\driftingSines_mono_test, \freq, 404, \dur, 0.1)
        )    
    })
    )
).play(t);

(
//fade between two pdefs with pmono (must specify dur key)
Pdef(\player,
    Pspawner({| sp |
        
        Pdef(\p1);

        'a'.postln;
        a = sp.par(
            Pdef(\p1,
                Pmono(\driftingSines_mono, \dur, 0.1)
            )
        );

        sp.wait(8);
        a = Pdef(\p1,
            Pmono(\driftingSines_mono, \freq, 404, \dur, 0.1)
        )    
    })
    )
).play(t);

Pspawner({| sp |
    Pdef(\p1);

    'a'.postln;
    a = sp.par(
        Pdef(\p1,
            Pmono(\driftingSines_mono, \dur, 0.1)
        )
    );

    sp.wait(8);
    a = Pdef(\p1,
        Pmono(\driftingSines_mono, \freq, 404, \dur, 0.1)
    )    
}).play(t);
)



(
    // fade between two Pdefs
    Pspawner({| sp |
        Pdef(\p1).fadeTime = 16;
        'a'.postln;
        a = sp.par(
            Pdef(\p1,
                Pbind(
                    \instrument, \default,
                    \dur, 1/2
                )
            )
        );
    
        sp.wait(4);
        Pbindef(\p1, \degree, 3).quant = 0.0;

    }).play(t);
    )